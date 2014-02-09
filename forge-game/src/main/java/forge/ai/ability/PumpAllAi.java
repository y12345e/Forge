package forge.ai.ability;

import com.google.common.base.Predicate;
import forge.ai.ComputerUtil;
import forge.ai.ComputerUtilCard;
import forge.ai.ComputerUtilCombat;
import forge.game.Game;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.combat.Combat;
import forge.game.combat.CombatUtil;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;
import forge.game.zone.ZoneType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PumpAllAi extends PumpAiBase {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(final Player ai, final SpellAbility sa) {
        String valid = "";
        final Card source = sa.getSourceCard();
        final Game game = ai.getGame();
        final Combat combat = game.getCombat();

        final int power = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("NumAtt"), sa);
        final int defense = AbilityUtils.calculateAmount(sa.getSourceCard(), sa.getParam("NumDef"), sa);
        final List<String> keywords = sa.hasParam("KW") ? Arrays.asList(sa.getParam("KW").split(" & ")) : new ArrayList<String>();

        final PhaseType phase = game.getPhaseHandler().getPhase();

        if (ComputerUtil.preventRunAwayActivations(sa)) {
            return false;
        }

        if (sa.hasParam("ValidCards")) {
            valid = sa.getParam("ValidCards");
        }

        final Player opp = ai.getOpponent();
        List<Card> comp = CardLists.getValidCards(ai.getCardsIn(ZoneType.Battlefield), valid, source.getController(), source);
        List<Card> human = CardLists.getValidCards(opp.getCardsIn(ZoneType.Battlefield), valid, source.getController(), source);

        final TargetRestrictions tgt = sa.getTargetRestrictions();
        if (tgt != null && sa.canTarget(opp) && sa.hasParam("IsCurse")) {
            sa.resetTargets();
            sa.getTargets().add(opp);
            comp = new ArrayList<Card>();
        }

        if (sa.hasParam("IsCurse")) {
            if (defense < 0) { // try to destroy creatures
                comp = CardLists.filter(comp, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        if (c.getNetDefense() <= -defense) {
                            return true; // can kill indestructible creatures
                        }
                        return ((ComputerUtilCombat.getDamageToKill(c) <= -defense) && !c.hasKeyword("Indestructible"));
                    }
                }); // leaves all creatures that will be destroyed
                human = CardLists.filter(human, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        if (c.getNetDefense() <= -defense) {
                            return true; // can kill indestructible creatures
                        }
                        return ((ComputerUtilCombat.getDamageToKill(c) <= -defense) && !c.hasKeyword("Indestructible"));
                    }
                }); // leaves all creatures that will be destroyed
            } // -X/-X end
            else if (power < 0) { // -X/-0
                if (phase.isAfter(PhaseType.COMBAT_DECLARE_BLOCKERS)
                        || phase.isBefore(PhaseType.COMBAT_DECLARE_ATTACKERS)
                        || game.getPhaseHandler().isPlayerTurn(sa.getActivatingPlayer())
                        || game.getPhaseHandler().isPreventCombatDamageThisTurn()) {
                    return false;
                }
                int totalPower = 0;
                for (Card c : human) {
                    if (combat == null || !combat.isAttacking(c)) {
                        continue;
                    }
                    totalPower += Math.min(c.getNetAttack(), power * -1);
                    if (phase == PhaseType.COMBAT_DECLARE_BLOCKERS && combat.isUnblocked(c)) {
                        if (ComputerUtilCombat.lifeInDanger(sa.getActivatingPlayer(), combat)) {
                            return true;
                        }
                        totalPower += Math.min(c.getNetAttack(), power * -1);
                    }
                    if (totalPower >= power * -2) {
                        return true;
                    }
                }
                return false;
            } // -X/-0 end
            
            if (comp.isEmpty() && ComputerUtil.activateForCost(sa, ai)) {
            	return true;
            }

            // evaluate both lists and pass only if human creatures are more
            // valuable
            if ((ComputerUtilCard.evaluateCreatureList(comp) + 200) >= ComputerUtilCard.evaluateCreatureList(human)) {
                return false;
            }
            return true;
        } // end Curse

        // don't use non curse PumpAll after Combat_Begin until AI is improved
        if (phase.isBefore(PhaseType.MAIN1)) {
            return false;
        } else if (phase.isAfter(PhaseType.COMBAT_DECLARE_ATTACKERS)) {
        	return ComputerUtil.activateForCost(sa, ai);
        }

        // only count creatures that can attack
        comp = CardLists.filter(comp, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                if (power <= 0 && !containsUsefulKeyword(ai, keywords, c, sa, power)) {
                    return false;
                }
                if (phase == PhaseType.COMBAT_DECLARE_ATTACKERS && combat.isAttacking(c)) {
                    return true;
                }
                if (phase.isBefore(PhaseType.COMBAT_DECLARE_ATTACKERS) && CombatUtil.canAttack(c, opp)) {
                    return true;
                }
                return false;
            }
        });

        if (comp.size() <= human.size()) {
            return false;
        }
        if (comp.size() <= 1 && (phase.isBefore(PhaseType.COMBAT_DECLARE_ATTACKERS) || sa.isSpell())) {
            return false;
        }

        return true;
    } // pumpAllCanPlayAI()

    @Override
    public boolean chkAIDrawback(SpellAbility sa, Player aiPlayer) {
        return true;
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#doTriggerAINoCost(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility, boolean)
     */
    @Override
    protected boolean doTriggerAINoCost(Player aiPlayer, SpellAbility sa, boolean mandatory) {
        return true;
    }

}
