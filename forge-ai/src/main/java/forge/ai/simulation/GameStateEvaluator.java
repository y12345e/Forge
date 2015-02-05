package forge.ai.simulation;

import forge.ai.ComputerUtilCard;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

public class GameStateEvaluator {

    public int getScoreForGameState(Game game, Player aiPlayer) {
        if (game.isGameOver()) {
            return game.getOutcome().getWinningPlayer() == aiPlayer ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
        int score = 0;
        // TODO: more than 2 players
        int myCards = 0;
        int theirCards = 0;
        for (Card c : game.getCardsIn(ZoneType.Hand)) {
            if (c.getController() == aiPlayer) {
                myCards++;
            } else {
                theirCards++;
            }
        }
        GameSimulator.debugPrint("My cards in hand: " + myCards);
        GameSimulator.debugPrint("Their cards in hand: " + theirCards);
        score += 3 * myCards - 3 * theirCards;
        for (Card c : game.getCardsIn(ZoneType.Battlefield)) {
            int value;
            // Simply way to have the AI hold-off on playing creatures in MAIN1 if they give no other benefits,
            // so that mana is saved for other effects.
            if (c.isSick() && c.getController() == aiPlayer && game.getPhaseHandler().getPhase() == PhaseType.MAIN1) {
                value = 0;
            } else {
                value = evalCard(c);
            }
            String str = c.getName();
            if (c.isCreature()) {
                str += " " + c.getNetPower() + "/" + c.getNetToughness();
            }
            if (c.getController() == aiPlayer) {
                GameSimulator.debugPrint("  Battlefield: " + str + " = " + value);
                score += value;
            } else {
                GameSimulator.debugPrint("  Battlefield: " + str + " = -" + value);
                score -= value;
            }
            String nonAbilityText = c.getNonAbilityText();
            if (!nonAbilityText.isEmpty()) {
                GameSimulator.debugPrint("    "+nonAbilityText.replaceAll("CARDNAME", c.getName()));
            }
        }
        GameSimulator.debugPrint("  My life: " + aiPlayer.getLife());
        score += aiPlayer.getLife();
        int opponentIndex = 1;
        int opponentLife = 0;
        for (Player opponent : game.getPlayers()) {
            if (opponent != aiPlayer) {
                GameSimulator.debugPrint("  Opponent " + opponentIndex + " life: -" + opponent.getLife());
                opponentLife += opponent.getLife();
                opponentIndex++;
            }
        }
        score -= opponentLife / (game.getPlayers().size() - 1);
        GameSimulator.debugPrint("Score = " + score);
        return score;
    }

    private static int evalCard(Card c) {
        // TODO: These should be based on other considerations - e.g. in relation to opponents state.
        if (c.isCreature()) {
            return ComputerUtilCard.evaluateCreature(c);
        } else if (c.isLand()) {
            return 100;
        } else if (c.isEnchantingCard()) {
            // TODO: Should provide value in whatever it's enchanting?
            // Else the computer would think that casting a Lifelink enchantment
            // on something that already has lifelink is a net win.
            return 0;
        } else {
            // e.g. a 5 CMC permanent results in 200, whereas a 5/5 creature is ~225
            return 50 + 30 * c.getCMC();
        }
    }
}