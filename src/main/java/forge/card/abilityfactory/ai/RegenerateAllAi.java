package forge.card.abilityfactory.ai;

import java.util.ArrayList;
import java.util.List;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.Singletons;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.abilityfactory.SpellAiLogic;
import forge.card.cost.Cost;
import forge.card.cost.CostUtil;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.game.phase.CombatUtil;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class RegenerateAllAi extends SpellAiLogic {
    
    @Override
    public boolean canPlayAI(Player ai, java.util.Map<String,String> params, SpellAbility sa) {
        final Card hostCard = sa.getAbilityFactory().getHostCard();
        boolean chance = false;
        final Cost abCost = sa.getAbilityFactory().getAbCost();
        if (abCost != null) {
            // AI currently disabled for these costs
            if (!CostUtil.checkSacrificeCost(ai, abCost, hostCard)) {
                return false;
            }

            if (!CostUtil.checkCreatureSacrificeCost(ai, abCost, hostCard)) {
                return false;
            }

            if (!CostUtil.checkLifeCost(ai, abCost, hostCard, 4, null)) {
                return false;
            }
        }

        // filter AIs battlefield by what I can target
        String valid = "";

        if (params.containsKey("ValidCards")) {
            valid = params.get("ValidCards");
        }

        List<Card> list = Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield);
        list = CardLists.getValidCards(list, valid.split(","), hostCard.getController(), hostCard);
        list = CardLists.filter(list, CardPredicates.isController(ai));

        if (list.size() == 0) {
            return false;
        }

        int numSaved = 0;
        if (Singletons.getModel().getGame().getStack().size() > 0) {
            final ArrayList<Object> objects = AbilityFactory.predictThreatenedObjects(sa.getActivatingPlayer(),sa.getAbilityFactory());

            for (final Card c : list) {
                if (objects.contains(c) && c.getShield() == 0) {
                    numSaved++;
                }
            }
        } else {
            if (Singletons.getModel().getGame().getPhaseHandler().is(PhaseType.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)) {
                final List<Card> combatants = CardLists.filter(list, CardPredicates.Presets.CREATURES);

                for (final Card c : combatants) {
                    if (c.getShield() == 0 && CombatUtil.combatantWouldBeDestroyed(c)) {
                        numSaved++;
                    }
                }
            }
        }

        if (numSaved > 1) {
            chance = true;
        }

        final AbilitySub subAb = sa.getSubAbility();
        if (subAb != null) {
            chance &= subAb.chkAIDrawback();
        }

        return chance;
    }

    @Override
    public boolean chkAIDrawback(java.util.Map<String,String> params, SpellAbility sa, Player aiPlayer) {
        return true;
    }

    @Override
    public boolean doTriggerAINoCost(Player aiPlayer, java.util.Map<String,String> params, SpellAbility sa, boolean mandatory) {
        boolean chance = true;

        final AbilitySub subAb = sa.getSubAbility();
        if (subAb != null) {
            chance &= subAb.doTrigger(mandatory);
        }

        return chance;
    }

}