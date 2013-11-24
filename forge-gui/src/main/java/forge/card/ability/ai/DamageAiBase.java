package forge.card.ability.ai;

import java.util.List;

import com.google.common.collect.Iterables;

import forge.card.ability.SpellAbilityAi;
import forge.card.spellability.SpellAbility;
import forge.game.Game;
import forge.game.ai.ComputerUtilCombat;
import forge.game.card.Card;
import forge.game.card.CardPredicates;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public abstract class DamageAiBase extends SpellAbilityAi {
    protected boolean shouldTgtP(final Player comp, final SpellAbility sa, final int d, final boolean noPrevention) {
        int restDamage = d;
        final Game game = comp.getGame();
        final Player enemy = comp.getOpponent();
        if (!sa.canTarget(enemy)) {
            return false;
        }
        if (sa.getTargets() != null && sa.getTargets().getTargets().contains(enemy)) {
            return false;
        }
        
        // burn Planeswalkers
        if (Iterables.any(enemy.getCardsIn(ZoneType.Battlefield), CardPredicates.Presets.PLANEWALKERS)) {
            return true;
        }

        if (!noPrevention) {
            restDamage = ComputerUtilCombat.predictDamageTo(enemy, restDamage, sa.getSourceCard(), false);
        } else {
            restDamage = enemy.staticReplaceDamage(restDamage, sa.getSourceCard(), false);
        }

        if (restDamage == 0) {
            return false;
        }

        if (!enemy.canLoseLife()) {
            return false;
        }

        final List<Card> hand = comp.getCardsIn(ZoneType.Hand);

        if (sa.isSpell()) {
            // If this is a spell, cast it instead of discarding
            if ((game.getPhaseHandler().is(PhaseType.END_OF_TURN) || game.getPhaseHandler().is(PhaseType.MAIN2))
                    && game.getPhaseHandler().isPlayerTurn(comp) && (hand.size() > comp.getMaxHandSize())) {
                return true;
            }
        }

        if ((enemy.getLife() - restDamage) < 5) {
            // drop the human to less than 5
            // life
            return true;
        }

        return false;
    }
}
