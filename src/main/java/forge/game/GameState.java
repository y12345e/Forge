/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import forge.Card;
import forge.CardLists;
import forge.CardPredicates.Presets;
import forge.ColorChanger;
import forge.GameAction;
import forge.GameLog;
import forge.StaticEffects;
import forge.card.replacement.ReplacementHandler;
import forge.card.trigger.TriggerHandler;
import forge.game.phase.Cleanup;
import forge.game.phase.Combat;
import forge.game.phase.EndOfCombat;
import forge.game.phase.EndOfTurn;
import forge.game.phase.PhaseHandler;
import forge.game.phase.Untap;
import forge.game.phase.Upkeep;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.MagicStack;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;

/**
 * Represents the state of a <i>single game</i> and is
 * "cleaned up" at each new game.
 */
public class GameState {
    private final List<Player> roPlayers;
    private final Cleanup cleanup = new Cleanup();
    private final EndOfTurn endOfTurn = new EndOfTurn();
    private final EndOfCombat endOfCombat = new EndOfCombat();
    private final Untap untap = new Untap();
    private final Upkeep upkeep = new Upkeep();
    private PhaseHandler phaseHandler = new PhaseHandler();
    private final MagicStack stack;
    private final StaticEffects staticEffects = new StaticEffects();
    private final TriggerHandler triggerHandler = new TriggerHandler();
    private final ReplacementHandler replacementHandler = new ReplacementHandler();
    private Combat combat = new Combat();
    private final GameLog gameLog = new GameLog();
    private final ColorChanger colorChanger = new ColorChanger();
    
    private boolean gameOver = false;

    private final Zone stackZone = new Zone(ZoneType.Stack);

    private long timestamp = 0;
    private int nTurn = 0;
    private final GameAction action;
    
    /**
     * Constructor.
     * @param players2 
     */
    public GameState(Iterable<LobbyPlayer> players2) { /* no more zones to map here */
        List<Player> players = new ArrayList<Player>();
        for(LobbyPlayer p : players2) {
            players.add(p.getIngamePlayer());
        }
        roPlayers = Collections.unmodifiableList(players);
        action = new GameAction(this);
        stack = new MagicStack(this);
    }

    /**
     * Gets the players.
     * 
     * @return the players
     */
    public final List<Player> getPlayers() {
        return roPlayers;
    }

    /**
     * Gets the cleanup step.
     * 
     * @return the cleanup step
     */
    public final Cleanup getCleanup() {
        return this.cleanup;
    }

    /**
     * Gets the end of turn.
     * 
     * @return the endOfTurn
     */
    public final EndOfTurn getEndOfTurn() {
        return this.endOfTurn;
    }

    /**
     * Gets the end of combat.
     * 
     * @return the endOfCombat
     */
    public final EndOfCombat getEndOfCombat() {
        return this.endOfCombat;
    }

    /**
     * Gets the upkeep.
     * 
     * @return the upkeep
     */
    public final Upkeep getUpkeep() {
        return this.upkeep;
    }

    /**
     * Gets the untap.
     * 
     * @return the upkeep
     */
    public final Untap getUntap() {
        return this.untap;
    }

    /**
     * Gets the phaseHandler.
     * 
     * @return the phaseHandler
     */
    public final PhaseHandler getPhaseHandler() {
        return this.phaseHandler;
    }

    /**
     * Gets the stack.
     * 
     * @return the stack
     */
    public final MagicStack getStack() {
        return this.stack;
    }

    /**
     * Gets the static effects.
     * 
     * @return the staticEffects
     */
    public final StaticEffects getStaticEffects() {
        return this.staticEffects;
    }

    /**
     * Gets the trigger handler.
     * 
     * @return the triggerHandler
     */
    public final TriggerHandler getTriggerHandler() {
        return this.triggerHandler;
    }

    /**
     * Gets the combat.
     * 
     * @return the combat
     */
    public final Combat getCombat() {
        return this.combat;
    }

    /**
     * Sets the combat.
     * 
     * @param combat0
     *            the combat to set
     */
    public final void setCombat(final Combat combat0) {
        this.combat = combat0;
    }

    /**
     * Gets the game log.
     * 
     * @return the game log
     */
    public final GameLog getGameLog() {
        return this.gameLog;
    }

    /**
     * Gets the stack zone.
     * 
     * @return the stackZone
     */
    public final Zone getStackZone() {
        return this.stackZone;
    }

    /**
     * Create and return the next timestamp.
     * 
     * @return the next timestamp
     */
    public final long getNextTimestamp() {
        this.setTimestamp(this.getTimestamp() + 1);
        return this.getTimestamp();
    }

    /**
     * Gets the timestamp.
     * 
     * @return the timestamp
     */
    public final long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Sets the timestamp.
     * 
     * @param timestamp0
     *            the timestamp to set
     */
    protected final void setTimestamp(final long timestamp0) {
        this.timestamp = timestamp0;
    }


    /**
     * @return the replacementHandler
     */
    public ReplacementHandler getReplacementHandler() {
        return replacementHandler;
    }

    /**
     * @return the gameOver
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * @param go the gameOver to set
     */
    public void setGameOver() {
        this.gameOver = true;
        for(Player p : roPlayers) {
            p.onGameOver();
        }
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public int getTurnNumber() {
        return nTurn;
    }

    /**
     * TODO: Write javadoc for this method.
     */
    public void notifyNextTurn() {
        nTurn++;
    }

    
    // THESE WERE MOVED HERE FROM AllZoneUtil 
    // They must once become non-static members of this class 
    
    public Zone getZoneOf(final Card c) {
        if (getStackZone().contains(c)) {
            return getStackZone();
        }
    
        for (final Player p : getPlayers()) {
            for (final ZoneType z : Player.ALL_ZONES) {
                final PlayerZone pz = p.getZone(z);
                if (pz.contains(c)) {
                    return pz;
                }
            }
        }
    
        return null;
    }

    public boolean isCardInZone(final Card c, final ZoneType zone) {
         if (zone.equals(ZoneType.Stack)) {
            if (getStackZone().contains(c)) {
                return true;
            }
        } else {
            for (final Player p : getPlayers()) {
                if (p.getZone(zone).contains(c)) {
                    return true;
                }
            }
        }
    
        return false;
    }

    public List<Card> getCardsIn(final ZoneType zone) {
        if (zone == ZoneType.Stack) {
            return getStackZone().getCards();
        } else {
            List<Card> cards = null;
            for (final Player p : getPlayers()) {
                if ( cards == null ) 
                    cards = p.getZone(zone).getCards();
                else
                    cards.addAll(p.getZone(zone).getCards());
            }
            return cards;
        }
    }

    public List<Card> getCardsIn(final Iterable<ZoneType> zones) {
        final List<Card> cards = new ArrayList<Card>();
        for (final ZoneType z : zones) {
            cards.addAll(getCardsIn(z));
        }
        return cards;
    }

    public List<Card> getLandsInPlay() {
        return CardLists.filter(getCardsIn(ZoneType.Battlefield), Presets.LANDS);
    }

    public boolean isCardExiled(final Card c) {
        return getCardsIn(ZoneType.Exile).contains(c);
    }

    
    public boolean isCardInPlay(final String cardName) {
        for (final Player p : getPlayers()) {
            if (p.isCardInPlay(cardName))
                return true;
        }
        return false;
    }

    public List<Card> getColoredCardsInPlay(final String color) {
        final List<Card> cards = new ArrayList<Card>();
        for(Player p : getPlayers()) {
            cards.addAll(p.getColoredCardsInPlay(color));
        }
        return cards;
    }

    public Card getCardState(final Card card) {
        for (final Card c : getCardsInGame()) {
            if (card.equals(c)) {
                return c;
            }
        }
    
        return card;
    }

    /**
     * <p>
     * compareTypeAmountInPlay.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @param type
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public static int compareTypeAmountInPlay(final Player player, final String type) {
        // returns the difference between player's
        final Player opponent = player.getOpponent();
        final List<Card> playerList = CardLists.getType(player.getCardsIn(ZoneType.Battlefield), type);
        final List<Card> opponentList = CardLists.getType(opponent.getCardsIn(ZoneType.Battlefield), type);
        return (playerList.size() - opponentList.size());
    }

    /**
     * <p>
     * compareTypeAmountInGraveyard.
     * </p>
     * 
     * @param player
     *            a {@link forge.game.player.Player} object.
     * @param type
     *            a {@link java.lang.String} object.
     * @return a int.
     */
    public static int compareTypeAmountInGraveyard(final Player player, final String type) {
        // returns the difference between player's
        final Player opponent = player.getOpponent();
        final List<Card> playerList = CardLists.getType(player.getCardsIn(ZoneType.Graveyard), type);
        final List<Card> opponentList = CardLists.getType(opponent.getCardsIn(ZoneType.Graveyard), type);
        return (playerList.size() - opponentList.size());
    }

    public List<Card> getCardsInGame() {
        final List<Card> all = new ArrayList<Card>();
        for (final Player player : getPlayers()) {
            all.addAll(player.getZone(ZoneType.Graveyard).getCards());
            all.addAll(player.getZone(ZoneType.Hand).getCards());
            all.addAll(player.getZone(ZoneType.Library).getCards());
            all.addAll(player.getZone(ZoneType.Battlefield).getCards(false));
            all.addAll(player.getZone(ZoneType.Exile).getCards());
        }
        all.addAll(getStackZone().getCards());
        return all;
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public ColorChanger getColorChanger() {
        return colorChanger;
    }

    public GameAction getAction() {
        return action;
    }
}
