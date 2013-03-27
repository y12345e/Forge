package forge.game.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import forge.Card;
import forge.FThreads;
import forge.GameEntity;
import forge.card.spellability.SpellAbility;
import forge.control.input.Input;
import forge.control.input.InputBlock;
import forge.control.input.InputCleanup;
import forge.control.input.InputPassPriority;
import forge.control.input.InputSelectCards;
import forge.control.input.InputSelectCardsFromList;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.game.GameState;
import forge.game.GameType;
import forge.game.phase.PhaseType;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.gui.GuiDialog;
import forge.gui.GuiUtils;
import forge.gui.match.CMatchUI;
import forge.item.CardPrinted;


/** 
 * A prototype for player controller class
 * 
 * Handles phase skips for now.
 */
public class PlayerControllerHuman extends PlayerController {

    private final Input defaultInput;
    private final Input blockInput;
    private final Input cleanupInput;
    private final HumanPlayer player;
    
    public final Input getDefaultInput() {
        return defaultInput;
    }

    public PlayerControllerHuman(GameState game0, HumanPlayer p) {
        super(game0);
        player = p;
        
        defaultInput = new InputPassPriority();
        blockInput = new InputBlock(getPlayer());
        cleanupInput = new InputCleanup(game);
    }

	@Override
    public boolean isUiSetToSkipPhase(final Player turn, final PhaseType phase) {
        return !CMatchUI.SINGLETON_INSTANCE.stopAtPhase(turn, phase);
    }

    /**
     * Uses GUI to learn which spell the player (human in our case) would like to play
     */
    public SpellAbility getAbilityToPlay(List<SpellAbility> abilities) {
        if (abilities.size() == 0) {
            return null;
        } else if (abilities.size() == 1) {
            return abilities.get(0);
        } else {
            return GuiChoose.oneOrNone("Choose", abilities); // some day network interaction will be here
        }
    }

    /** Input to use when player has to declare blockers */
    public Input getBlockInput() {
        return blockInput;
    }

    /**
     * @return the cleanupInput
     */
    public Input getCleanupInput() {
        return cleanupInput;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param c
     */
    public void playFromSuspend(Card c) {
        c.setSuspendCast(true);
        game.getActionPlay().playCardWithoutManaCost(c, c.getOwner());
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#playCascade(java.util.List, forge.Card)
     */
    @Override
    public boolean playCascade(Card cascadedCard, Card sourceCard) {

        final StringBuilder title = new StringBuilder();
        title.append(sourceCard.getName()).append(" - Cascade Ability");
        final StringBuilder question = new StringBuilder();
        question.append("Cast ").append(cascadedCard.getName());
        question.append(" without paying its mana cost?");


        boolean result = GuiDialog.confirm(cascadedCard, question.toString());
        if ( result )
            game.getActionPlay().playCardWithoutManaCost(cascadedCard, getPlayer());
        return result;
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#mayPlaySpellAbilityForFree(forge.card.spellability.SpellAbility)
     */
    @Override
    public void playSpellAbilityForFree(SpellAbility copySA) {
        game.getActionPlay().playSpellAbilityWithoutPayingManaCost(copySA);
    }

    /**
     * @return the player
     */
    @Override
    public Player getPlayer() {
        return player;
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#sideboard(forge.deck.Deck)
     */
    @Override
    public Deck sideboard(Deck deck, GameType gameType) {
        CardPool sideboard = deck.get(DeckSection.Sideboard);
        CardPool main = deck.get(DeckSection.Main);

        int deckMinSize = Math.min(main.countAll(), gameType.getDecksFormat().getMainRange().getMinimumInteger());
    
        CardPool newSb = new CardPool();
        List<CardPrinted> newMain = null;
        
        while (newMain == null || newMain.size() < deckMinSize) {
            if (newMain != null) {
                String errMsg = String.format("Too few cards in your main deck (minimum %d), please make modifications to your deck again.", deckMinSize);
                JOptionPane.showMessageDialog(null, errMsg, "Invalid deck", JOptionPane.ERROR_MESSAGE);
            }
            
            boolean isLimited = (gameType == GameType.Draft || gameType == GameType.Sealed);
            newMain = GuiChoose.sideboard(sideboard.toFlatList(), main.toFlatList(), isLimited);
        }
    
        newSb.clear();
        newSb.addAll(main);
        newSb.addAll(sideboard);
        for(CardPrinted c : newMain) {
            newSb.remove(c);
        }
    
        Deck res = (Deck)deck.copyTo(deck.getName());
        res.getMain().clear();
        res.getMain().add(newMain);
        CardPool resSb = res.getOrCreate(DeckSection.Sideboard);
        resSb.clear();
        resSb.addAll(newSb);
        return res;
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#assignCombatDamage()
     */
    @Override
    public Map<Card, Integer> assignCombatDamage(Card attacker, List<Card> blockers, int damageDealt, GameEntity defender) {
        Map<Card, Integer> map;
        if (defender != null && assignDamageAsIfNotBlocked(attacker)) {
            map = new HashMap<Card, Integer>();
            map.put(null, damageDealt);
        } else {
            if (attacker.hasKeyword("Trample") || (blockers.size() > 1)) {
                map = CMatchUI.SINGLETON_INSTANCE.getDamageToAssign(attacker, blockers, damageDealt, defender);
            } else {
                map = new HashMap<Card, Integer>();
                map.put(blockers.get(0), damageDealt);
            }
        }
        return map;
    }
    
    private final boolean assignDamageAsIfNotBlocked(Card attacker) {
        return attacker.hasKeyword("CARDNAME assigns its combat damage as though it weren't blocked.")
                || (attacker.hasKeyword("You may have CARDNAME assign its combat damage as though it weren't blocked.")
                && GuiDialog.confirm(attacker, "Do you want to assign its combat damage as though it weren't blocked?"));
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#announceRequirements(java.lang.String)
     */
    @Override
    public String announceRequirements(SpellAbility ability, String announce) {
        StringBuilder sb = new StringBuilder(ability.getSourceCard().getName());
        sb.append(" - How much will you announce for ");
        sb.append(announce);
        sb.append("?");
        return JOptionPane.showInputDialog(sb.toString());
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#choosePermanentsToSacrifice(java.util.List, int, forge.card.spellability.SpellAbility, boolean, boolean)
     */
    @Override
    public List<Card> choosePermanentsToSacrifice(List<Card> validTargets, int amount, SpellAbility sa, boolean destroy, boolean isOptional) {
        int max = Math.min(amount, validTargets.size());
        if (max == 0)
            return new ArrayList<Card>();

        InputSelectCards inp = new InputSelectCardsFromList(isOptional ? 0 : amount, max, validTargets);
        // TODO: Either compose a message here, or pass it as parameter from caller. 
        inp.setMessage("Select %d card(s) to sacrifice");
        
        FThreads.setInputAndWait(inp);
        if( inp.hasCancelled() )
            return new ArrayList<Card>();
        else return inp.getSelected(); 
    }

    @Override
    public Card chooseSingleCardForEffect(List<Card> options, SpellAbility sa, String title, boolean isOptional) {
        // Human is supposed to read the message and understand from it what to choose
        if ( isOptional )
            return GuiChoose.oneOrNone(title, options);
        else 
            return GuiChoose.one(title, options);
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#confirmAction(forge.card.spellability.SpellAbility, java.lang.String, java.lang.String)
     */
    @Override
    public boolean confirmAction(SpellAbility sa, String mode, String message) {
        return GuiDialog.confirm(sa.getSourceCard(), message);
    }

    @Override
    public boolean confirmStaticApplication(Card hostCard, GameEntity affected, String logic, String message) {
        return GuiDialog.confirm(hostCard, message);
    }

    @Override
    public boolean getWillPlayOnFirstTurn(String message) {
        final String[] possibleValues = { "Play", "Draw" };

        final Object playDraw = JOptionPane.showOptionDialog(null, message + "\n\nWould you like to play or draw?",
                "Play or Draw?", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                possibleValues, possibleValues[0]);

        return !playDraw.equals(1);
    }

    @Override
    public List<Card> orderBlockers(Card attacker, List<Card> blockers) {
        GuiUtils.setPanelSelection(attacker);
        return GuiChoose.order("Choose Blocking Order", "Damaged First", 0, blockers, null, attacker);
    }

    @Override
    public List<Card> orderAttackers(Card blocker, List<Card> attackers) {
        GuiUtils.setPanelSelection(blocker);
        return GuiChoose.order("Choose Blocking Order", "Damaged First", 0, attackers, null, blocker);
    }

    /* (non-Javadoc)
     * @see forge.game.player.PlayerController#reveal(java.lang.String, java.util.List, forge.game.zone.ZoneType, forge.game.player.Player)
     */
    @Override
    public void reveal(String string, List<Card> cards, ZoneType zone, Player owner) {
        String message = string;
        if ( StringUtils.isBlank(message) ) 
            message = String.format("Looking at %s's %s", owner, zone);
        GuiChoose.oneOrNone(message, cards);
    }

    @Override
    public ImmutablePair<List<Card>, List<Card>> arrangeForScry(List<Card> topN) {
        List<Card> toBottom = null;
        List<Card> toTop = null;
        
        if (topN.size() == 1) {
            if (willPutCardOnTop(topN.get(0)))
                toTop = topN;
            else 
                toBottom = topN;
        } else { 
            toBottom = GuiChoose.order("Select cards to be put on the bottom of your library", "Cards to put on the bottom", -1, topN, null, null);
            topN.removeAll(toBottom);
            if ( topN.isEmpty() )
                toTop = null;
            else if ( topN.size() == 1 )
                toTop = topN;
            else
                toTop = GuiChoose.order("Arrange cards to be put on top of your library", "Cards arranged", 0, topN, null, null);
        }
        return ImmutablePair.of(toTop, toBottom);
    }


    @Override
    public boolean willPutCardOnTop(Card c) {
        return GuiDialog.confirm(c, "Where will you put " + c.getName() + " in your library", new String[]{"Top", "Bottom"} );
    }

    @Override
    public List<Card> chooseCardsToDiscardFrom(Player p, SpellAbility sa, List<Card> valid, int minDiscard) {
        if ( p != getPlayer() ) {
            int cntToKeepInHand =  minDiscard == 0 ? -1 : valid.size() - minDiscard;
            return GuiChoose.order("Choose cards to Discard", "Discarded", cntToKeepInHand, valid, null, null);
        }

        int max = minDiscard == 0 ? Integer.MAX_VALUE : minDiscard;
        InputSelectCards inp = new InputSelectCardsFromList(minDiscard, max, valid);
        inp.setCancelWithSelectedAllowed(false);
        inp.setMessage("Discard %d cards");
        FThreads.setInputAndWait(inp);
        return inp.getSelected();
    }

    @Override
    public Card chooseCardToDredge(List<Card> dredgers) {
        if (GuiDialog.confirm(null, "Do you want to dredge?", false)) {
            return GuiChoose.oneOrNone("Select card to dredge", dredgers);
        }
        return null;
    }

    @Override
    public void playMiracle(SpellAbility miracle, Card card) {
        if (GuiDialog.confirm(card, card + " - Drawn. Play for Miracle Cost?")) {
            game.getActionPlay().playSpellAbility(miracle, getPlayer());
        }
    }

    @Override
    public void playMadness(SpellAbility madness) {
        if (GuiDialog.confirm(madness.getSourceCard(), madness.getSourceCard() + " - Discarded. Pay Madness Cost?")) {
            game.getActionPlay().playSpellAbility(madness, getPlayer());
        }
    }
}
