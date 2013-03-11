package forge.card.ability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.commons.lang3.StringUtils;

import forge.Card;
import forge.CardLists;
import forge.CardUtil;
import forge.Command;
import forge.Constant;
import forge.CounterType;
import forge.Singletons;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.mana.ManaCostBeingPaid;
import forge.card.spellability.Ability;
import forge.card.spellability.AbilityStatic;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.SpellAbility;
import forge.game.GameActionUtil;
import forge.game.GameState;
import forge.game.ai.ComputerUtil;
import forge.game.ai.ComputerUtilCost;
import forge.game.player.AIPlayer;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.util.Expressions;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class AbilityUtils {

    public static CounterType getCounterType(String name, SpellAbility sa) throws Exception {
        CounterType counterType;

        try {
            counterType = CounterType.getType(name);
        } catch (Exception e) {
            String type = sa.getSVar(name);
            if (type.equals("")) {
                type = sa.getSourceCard().getSVar(name);
            }

            if (type.equals("")) {
                throw new Exception("Counter type doesn't match, nor does an SVar exist with the type name.");
            }
            counterType = CounterType.getType(type);
        }

        return counterType;
    }

    // should the three getDefined functions be merged into one? Or better to
    // have separate?
    // If we only have one, each function needs to Cast the Object to the
    // appropriate type when using
    // But then we only need update one function at a time once the casting is
    // everywhere.
    // Probably will move to One function solution sometime in the future
    /**
     * <p>
     * getDefinedCards.
     * </p>
     * 
     * @param hostCard
     *            a {@link forge.Card} object.
     * @param def
     *            a {@link java.lang.String} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.util.ArrayList} object.
     */
    @SuppressWarnings("unchecked")
    public static List<Card> getDefinedCards(final Card hostCard, final String def, final SpellAbility sa) {
        final List<Card> cards = new ArrayList<Card>();
        final String defined = (def == null) ? "Self" : def; // default to Self

        Card c = null;

        if (defined.equals("Self")) {
            c = hostCard;
        }

        else if (defined.equals("OriginalHost")) {
            c = sa.getOriginalHost();
        }

        else if (defined.equals("EffectSource")) {
            if (hostCard.isType("Effect")) {
                c = AbilityUtils.findEffectRoot(hostCard);
            }
        }

        else if (defined.equals("Equipped")) {
            c = hostCard.getEquippingCard();
        }

        else if (defined.equals("Enchanted")) {
            c = hostCard.getEnchantingCard();
            if ((c == null) && (sa.getRootAbility() != null)
                    && (sa.getRootAbility().getPaidList("Sacrificed") != null)
                    && !sa.getRootAbility().getPaidList("Sacrificed").isEmpty()) {
                c = sa.getRootAbility().getPaidList("Sacrificed").get(0).getEnchantingCard();
            }
        }

        else if (defined.equals("TopOfLibrary")) {
            final List<Card> lib = hostCard.getController().getCardsIn(ZoneType.Library);
            if (lib.size() > 0) {
                c = lib.get(0);
            } else {
                // we don't want this to fall through and return the "Self"
                return cards;
            }
        }

        else if (defined.equals("Targeted")) {
            final SpellAbility saTargeting = sa.getSATargetingCard();
            if (saTargeting != null) {
                cards.addAll(saTargeting.getTarget().getTargetCards());
            }

        } else if (defined.equals("ParentTarget")) {
            final SpellAbility parent = sa.getParentTargetingCard();
            if (parent != null) {
                cards.addAll(parent.getTarget().getTargetCards());
            }

        } else if (defined.startsWith("Triggered") && (sa != null)) {
            final SpellAbility root = sa.getRootAbility();
            if (defined.contains("LKICopy")) { //TriggeredCardLKICopy
                final Object crd = root.getTriggeringObject(defined.substring(9, 13));
                if (crd instanceof Card) {
                    c = (Card) crd;
                }
            }
            else {
                final Object crd = root.getTriggeringObject(defined.substring(9));
                if (crd instanceof Card) {
                    c = Singletons.getModel().getGame().getCardState((Card) crd);
                } else if (crd instanceof List<?>) {
                    for (final Card cardItem : (List<Card>) crd) {
                        cards.add(cardItem);
                    }
                }
            }
        } else if (defined.startsWith("Replaced") && (sa != null)) {
            final SpellAbility root = sa.getRootAbility();
            final Object crd = root.getReplacingObject(defined.substring(8));
            if (crd instanceof Card) {
                c = Singletons.getModel().getGame().getCardState((Card) crd);
            } else if (crd instanceof List<?>) {
                for (final Card cardItem : (List<Card>) crd) {
                    cards.add(cardItem);
                }
            }
        } else if (defined.equals("Remembered")) {
            if (hostCard.getRemembered().isEmpty()) {
                final Card newCard = Singletons.getModel().getGame().getCardState(hostCard);
                for (final Object o : newCard.getRemembered()) {
                    if (o instanceof Card) {
                        cards.add(Singletons.getModel().getGame().getCardState((Card) o));
                    }
                }
            }

            for (final Object o : hostCard.getRemembered()) {
                if (o instanceof Card) {
                    cards.add(Singletons.getModel().getGame().getCardState((Card) o));
                }
            }
        } else if (defined.equals("Clones")) {
            for (final Card clone : hostCard.getClones()) {
                cards.add(Singletons.getModel().getGame().getCardState(clone));
            }
        } else if (defined.equals("Imprinted")) {
            for (final Card imprint : hostCard.getImprinted()) {
                cards.add(Singletons.getModel().getGame().getCardState(imprint));
            }
        } else if (defined.startsWith("ThisTurnEntered")) {
            final String[] workingCopy = defined.split("_");
            ZoneType destination, origin;
            String validFilter;

            destination = ZoneType.smartValueOf(workingCopy[1]);
            if (workingCopy[2].equals("from")) {
                origin = ZoneType.smartValueOf(workingCopy[3]);
                validFilter = workingCopy[4];
            } else {
                origin = null;
                validFilter = workingCopy[2];
            }
            for (final Card cl : CardUtil.getThisTurnEntered(destination, origin, validFilter, hostCard)) {
                cards.add(cl);
            }
        } else if (defined.equals("ChosenCard")) {
            for (final Card chosen : hostCard.getChosenCard()) {
                cards.add(Singletons.getModel().getGame().getCardState(chosen));
            }
        } else {
            List<Card> list = null;
            if (defined.startsWith("Sacrificed")) {
                list = sa.getRootAbility().getPaidList("Sacrificed");
            }

            else if (defined.startsWith("Discarded")) {
                list = sa.getRootAbility().getPaidList("Discarded");
            }

            else if (defined.startsWith("Exiled")) {
                list = sa.getRootAbility().getPaidList("Exiled");
            }

            else if (defined.startsWith("Tapped")) {
                list = sa.getRootAbility().getPaidList("Tapped");
            }

            else if (defined.startsWith("Valid ")) {
                String validDefined = defined.substring("Valid ".length());
                GameState game = Singletons.getModel().getGame();

                list = CardLists.getValidCards(game.getCardsIn(ZoneType.Battlefield), validDefined.split(","), hostCard.getController(), hostCard);
            }

            else if (defined.startsWith("ValidHand ")) {
                String validDefined = defined.substring("ValidHand ".length());
                GameState game = Singletons.getModel().getGame();

                list = CardLists.getValidCards(game.getCardsIn(ZoneType.Hand), validDefined.split(","), hostCard.getController(), hostCard);
            }

            else if (defined.startsWith("ValidAll ")) {
                String validDefined = defined.substring("ValidAll ".length());
                GameState game = Singletons.getModel().getGame();

                list = CardLists.getValidCards(game.getCardsInGame(), validDefined.split(","), hostCard.getController(), hostCard);
            }

            else {
                return cards;
            }

            cards.addAll(list);
        }

        if (c != null) {
            cards.add(c);
        }

        return cards;
    }

    private static Card findEffectRoot(Card startCard) {

        Card cc = startCard.getEffectSource();
        if (cc != null) {

            if (cc.isType("Effect")) {
                return findEffectRoot(cc);
            }
            return cc;
        }

        return null; //If this happens there is a card in the game that is not in any zone
    }

    // Utility functions used by the AFs
    /**
     * <p>
     * calculateAmount.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @param amount
     *            a {@link java.lang.String} object.
     * @param ability
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a int.
     */
    public static int calculateAmount(final Card card, String amount, final SpellAbility ability) {
        // amount can be anything, not just 'X' as long as sVar exists

        if (amount == null || amount.isEmpty()) {
            return 0;
        }

        // If Amount is -X, strip the minus sign before looking for an SVar of
        // that kind
        int multiplier = 1;
        if (amount.startsWith("-")) {
            multiplier = -1;
            amount = amount.substring(1);
        } else if (amount.startsWith("+")) {
            amount = amount.substring(1);
        }

        String svarval;
        if (ability != null) {

            svarval = ability.getSVar(amount);
            if (svarval.equals("")) {
                try {
                    Integer.parseInt(amount);
                }
                catch (NumberFormatException ignored) {
                    //If this is reached, amount wasn't an integer
                    //Print a warning to console to help debug if an ability is not stolen properly.
                    StringBuilder sb = new StringBuilder("WARNING:SVar fallback to Card (");
                    sb.append(card.getName()).append(") and Ability(").append(ability.toString()).append(")");
                    System.out.println(sb.toString());
                    svarval = card.getSVar(amount);
                }
            }
        } else {
            svarval = card.getSVar(amount);
        }

        if (!svarval.equals("")) {
            final String[] calcX = svarval.split("\\$");
            if ((calcX.length == 1) || calcX[1].equals("none")) {
                return 0;
            }

            if (calcX[0].startsWith("Count")) {
                return AbilityUtils.xCount(card, calcX[1], ability) * multiplier;
            } else if (calcX[0].startsWith("Number")) {
                return CardFactoryUtil.xCount(card, svarval) * multiplier;
            } else if (calcX[0].startsWith("SVar")) {
                final String[] l = calcX[1].split("/");
                final String[] m = CardFactoryUtil.parseMath(l);
                return CardFactoryUtil.doXMath(AbilityUtils.calculateAmount(card, l[0], ability), m, card)
                        * multiplier;
            } else if (calcX[0].startsWith("PlayerCount")) {
                final String hType = calcX[0].substring(11);
                final ArrayList<Player> players = new ArrayList<Player>();
                if (hType.equals("Players") || hType.equals("")) {
                    players.addAll(Singletons.getModel().getGame().getPlayers());
                    return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
                } else if (hType.equals("Opponents")) {
                    players.addAll(card.getController().getOpponents());
                    return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
                } else if (hType.equals("Other")) {
                    players.addAll(card.getController().getAllOtherPlayers());
                    return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
                } else if (hType.equals("Remembered")) {
                    for (final Object o : card.getRemembered()) {
                        if (o instanceof Player) {
                            players.add((Player) o);
                        }
                    }
                    return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
                } else if (hType.equals("NonActive")) {
                    players.addAll(Singletons.getModel().getGame().getPlayers());
                    players.remove(Singletons.getModel().getGame().getPhaseHandler().getPlayerTurn());
                    return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
                }
            } else if (calcX[0].startsWith("Remembered")) {
                // Add whole Remembered list to handlePaid
                final List<Card> list = new ArrayList<Card>();
                if (card.getRemembered().isEmpty()) {
                    final Card newCard = Singletons.getModel().getGame().getCardState(card);
                    for (final Object o : newCard.getRemembered()) {
                        if (o instanceof Card) {
                            list.add(Singletons.getModel().getGame().getCardState((Card) o));
                        }
                    }
                }

                if (calcX[0].endsWith("LKI")) { // last known information
                    for (final Object o : card.getRemembered()) {
                        if (o instanceof Card) {
                            list.add((Card) o);
                        }
                    }
                } else {
                    for (final Object o : card.getRemembered()) {
                        if (o instanceof Card) {
                            list.add(Singletons.getModel().getGame().getCardState((Card) o));
                        }
                    }
                }

                return CardFactoryUtil.handlePaid(list, calcX[1], card) * multiplier;
            } else if (calcX[0].startsWith("Imprinted")) {
                // Add whole Imprinted list to handlePaid
                final List<Card> list = new ArrayList<Card>();
                for (final Card c : card.getImprinted()) {
                    list.add(Singletons.getModel().getGame().getCardState(c));
                }

                return CardFactoryUtil.handlePaid(list, calcX[1], card) * multiplier;
            } else if (calcX[0].matches("Enchanted")) {
                // Add whole Enchanted list to handlePaid
                final List<Card> list = new ArrayList<Card>();
                if (card.isEnchanting()) {
                    Object o = card.getEnchanting();
                    if (o instanceof Card) {
                        list.add(Singletons.getModel().getGame().getCardState((Card) o));
                    }
                }
                return CardFactoryUtil.handlePaid(list, calcX[1], card) * multiplier;
            } else if (ability != null) {
                // Player attribute counting
                if (calcX[0].startsWith("TargetedPlayer")) {
                    final ArrayList<Player> players = new ArrayList<Player>();
                    final SpellAbility saTargeting = ability.getSATargetingPlayer();
                    if (null != saTargeting) {
                        players.addAll(saTargeting.getTarget().getTargetPlayers());
                    }
                    return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
                }
                if (calcX[0].startsWith("TargetedObjects")) {
                    final ArrayList<Object> objects = new ArrayList<Object>();
                    // Make list of all targeted objects starting with the root SpellAbility
                    SpellAbility loopSA = ability.getRootAbility();
                    while (loopSA != null) {
                        if (loopSA.getTarget() != null) {
                            objects.addAll(loopSA.getTarget().getTargets());
                        }
                        loopSA = loopSA.getSubAbility();
                    }
                    return CardFactoryUtil.objectXCount(objects, calcX[1], card) * multiplier;
                }
                if (calcX[0].startsWith("TargetedController")) {
                    final ArrayList<Player> players = new ArrayList<Player>();
                    final List<Card> list = getDefinedCards(card, "Targeted", ability);
                    final List<SpellAbility> sas = AbilityUtils.getDefinedSpellAbilities(card, "Targeted",
                            ability);

                    for (final Card c : list) {
                        final Player p = c.getController();
                        if (!players.contains(p)) {
                            players.add(p);
                        }
                    }
                    for (final SpellAbility s : sas) {
                        final Player p = s.getSourceCard().getController();
                        if (!players.contains(p)) {
                            players.add(p);
                        }
                    }
                    return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
                }
                if (calcX[0].startsWith("TargetedByTarget")) {
                    final List<Card> tgtList = new ArrayList<Card>();
                    final List<SpellAbility> saList = getDefinedSpellAbilities(card, "Targeted", ability);

                    for (final SpellAbility s : saList) {
                        tgtList.addAll(getDefinedCards(s.getSourceCard(), "Targeted", s));
                    }
                    return CardFactoryUtil.handlePaid(tgtList, calcX[1], card) * multiplier;
                }
                if (calcX[0].startsWith("TriggeredPlayer") || calcX[0].startsWith("TriggeredTarget")) {
                    final SpellAbility root = ability.getRootAbility();
                    Object o = root.getTriggeringObject(calcX[0].substring(9));
                    final List<Player> players = new ArrayList<Player>();
                    if (o instanceof Player) {
                        players.add((Player) o);
                    }
                    return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
                }
                // Added on 9/30/12 (ArsenalNut) - Ended up not using but might be useful in future
                /*
                if (calcX[0].startsWith("EnchantedController")) {
                    final ArrayList<Player> players = new ArrayList<Player>();
                    players.addAll(AbilityFactory.getDefinedPlayers(card, "EnchantedController", ability));
                    return CardFactoryUtil.playerXCount(players, calcX[1], card) * multiplier;
                }
                 */

                List<Card> list = new ArrayList<Card>();
                if (calcX[0].startsWith("Sacrificed")) {
                    list = ability.getRootAbility().getPaidList("Sacrificed");
                } else if (calcX[0].startsWith("Discarded")) {
                    final SpellAbility root = ability.getRootAbility();
                    list = root.getPaidList("Discarded");
                    if ((null == list) && root.isTrigger()) {
                        list = root.getSourceCard().getSpellPermanent().getPaidList("Discarded");
                    }
                } else if (calcX[0].startsWith("Exiled")) {
                    list = ability.getRootAbility().getPaidList("Exiled");
                } else if (calcX[0].startsWith("Tapped")) {
                    list = ability.getRootAbility().getPaidList("Tapped");
                } else if (calcX[0].startsWith("Revealed")) {
                    list = ability.getRootAbility().getPaidList("Revealed");
                } else if (calcX[0].startsWith("Targeted")) {
                    list = ability.findTargetedCards();
                } else if (calcX[0].startsWith("Triggered")) {
                    final SpellAbility root = ability.getRootAbility();
                    list = new ArrayList<Card>();
                    list.add((Card) root.getTriggeringObject(calcX[0].substring(9)));
                } else if (calcX[0].startsWith("TriggerCount")) {
                    // TriggerCount is similar to a regular Count, but just
                    // pulls Integer Values from Trigger objects
                    final SpellAbility root = ability.getRootAbility();
                    final String[] l = calcX[1].split("/");
                    final String[] m = CardFactoryUtil.parseMath(l);
                    final int count = (Integer) root.getTriggeringObject(l[0]);

                    return CardFactoryUtil.doXMath(count, m, card) * multiplier;
                } else if (calcX[0].startsWith("Replaced")) {
                    final SpellAbility root = ability.getRootAbility();
                    list = new ArrayList<Card>();
                    list.add((Card) root.getReplacingObject(calcX[0].substring(8)));
                } else if (calcX[0].startsWith("ReplaceCount")) {
                    // ReplaceCount is similar to a regular Count, but just
                    // pulls Integer Values from Replacement objects
                    final SpellAbility root = ability.getRootAbility();
                    final String[] l = calcX[1].split("/");
                    final String[] m = CardFactoryUtil.parseMath(l);
                    final int count = (Integer) root.getReplacingObject(l[0]);

                    return CardFactoryUtil.doXMath(count, m, card) * multiplier;
                } else {

                    return 0;
                }

                return CardFactoryUtil.handlePaid(list, calcX[1], card) * multiplier;

            } else {
                return 0;
            }
        }
        if (amount.equals("ChosenX") || amount.equals("ChosenY")) {
            // isn't made yet
            return 0;
        }
        // cost hasn't been paid yet
        if (amount.startsWith("Cost")) {
            return 0;
        }

        return Integer.parseInt(amount) * multiplier;
    }

    /**
     * <p>
     * getDefinedObjects.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @param def
     *            a {@link java.lang.String} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static ArrayList<Object> getDefinedObjects(final Card card, final String def, final SpellAbility sa) {
        final ArrayList<Object> objects = new ArrayList<Object>();
        final String defined = (def == null) ? "Self" : def;

        objects.addAll(AbilityUtils.getDefinedPlayers(card, defined, sa));
        objects.addAll(getDefinedCards(card, defined, sa));
        objects.addAll(AbilityUtils.getDefinedSpellAbilities(card, defined, sa));
        return objects;
    }

    /**
     * Filter list by type.
     * 
     * @param list
     *            a CardList
     * @param type
     *            a card type
     * @param sa
     *            a SpellAbility
     * @return a {@link forge.CardList} object.
     */
    public static List<Card> filterListByType(final List<Card> list, String type, final SpellAbility sa) {
        if (type == null) {
            return list;
        }

        // Filter List Can send a different Source card in for things like
        // Mishra and Lobotomy

        Card source = sa.getSourceCard();
        final Object o;
        if (type.startsWith("Triggered")) {
            if (type.contains("Card")) {
                o = sa.getTriggeringObject("Card");
            } else if (type.contains("Attacker")) {
                o = sa.getTriggeringObject("Attacker");
            } else if (type.contains("Blocker")) {
                o = sa.getTriggeringObject("Blocker");
            } else {
                o = sa.getTriggeringObject("Card");
            }

            if (!(o instanceof Card)) {
                return new ArrayList<Card>();
            }

            if (type.equals("Triggered") || (type.equals("TriggeredCard")) || (type.equals("TriggeredAttacker"))
                    || (type.equals("TriggeredBlocker"))) {
                type = "Card.Self";
            }

            source = (Card) (o);
            if (type.contains("TriggeredCard")) {
                type = type.replace("TriggeredCard", "Card");
            } else if (type.contains("TriggeredAttacker")) {
                type = type.replace("TriggeredAttacker", "Card");
            } else if (type.contains("TriggeredBlocker")) {
                type = type.replace("TriggeredBlocker", "Card");
            } else {
                type = type.replace("Triggered", "Card");
            }

        } else if (type.startsWith("Targeted")) {
            source = null;
            ArrayList<Card> tgts = sa.findTargetedCards();
            if (!tgts.isEmpty()) {
                source = tgts.get(0);
            }
            if (source == null) {
                return new ArrayList<Card>();
            }

            if (type.startsWith("TargetedCard")) {
                type = type.replace("TargetedCard", "Card");
            } else {
                type = type.replace("Targeted", "Card");
            }

        } else if (type.startsWith("Remembered")) {
            boolean hasRememberedCard = false;
            for (final Object object : source.getRemembered()) {
                if (object instanceof Card) {
                    hasRememberedCard = true;
                    source = (Card) object;
                    type = type.replace("Remembered", "Card");
                    break;
                }
            }

            if (!hasRememberedCard) {
                return new ArrayList<Card>();
            }
        } else if (type.equals("Card.AttachedBy")) {
            source = source.getEnchantingCard();
            type = type.replace("Card.AttachedBy", "Card.Self");
        }

        String valid = type;
        if (valid.contains("EQX")) {
            valid = valid.replace("X", Integer.toString(calculateAmount(source, "X", sa)));
        }
        return CardLists.getValidCards(list, valid.split(","), sa.getActivatingPlayer(), source);
    }

    /**
     * <p>
     * getDefinedPlayers.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @param def
     *            a {@link java.lang.String} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static List<Player> getDefinedPlayers(final Card card, final String def, final SpellAbility sa) {
        final List<Player> players = new ArrayList<Player>();
        final String defined = (def == null) ? "You" : def;

        if (defined.equals("Targeted")) {
            final SpellAbility saTargeting = sa.getSATargetingPlayer();
            if (saTargeting != null) {
                players.addAll(saTargeting.getTarget().getTargetPlayers());
            }
        } else if (defined.equals("TargetedController")) {
            final List<Card> list = getDefinedCards(card, "Targeted", sa);
            final List<SpellAbility> sas = AbilityUtils.getDefinedSpellAbilities(card, "Targeted", sa);

            for (final Card c : list) {
                final Player p = c.getController();
                if (!players.contains(p)) {
                    players.add(p);
                }
            }
            for (final SpellAbility s : sas) {
                final Player p = s.getActivatingPlayer();
                if (!players.contains(p)) {
                    players.add(p);
                }
            }
        } else if (defined.equals("TargetedOwner")) {
            final List<Card> list = getDefinedCards(card, "Targeted", sa);

            for (final Card c : list) {
                final Player p = c.getOwner();
                if (!players.contains(p)) {
                    players.add(p);
                }
            }
        } else if (defined.equals("TargetedAndYou")) {
            final SpellAbility saTargeting = sa.getSATargetingPlayer();
            if (saTargeting != null) {
                players.addAll(saTargeting.getTarget().getTargetPlayers());
                players.add(sa.getActivatingPlayer());
            }
        } else if (defined.equals("Remembered")) {
            for (final Object rem : card.getRemembered()) {
                if (rem instanceof Player) {
                    players.add((Player) rem);
                }
            }
        } else if (defined.equals("RememberedOpponent")) {
            for (final Object rem : card.getRemembered()) {
                if (rem instanceof Player) {
                    players.add(((Player) rem).getOpponent());
                }
            }
        } else if (defined.equals("RememberedController")) {
            for (final Object rem : card.getRemembered()) {
                if (rem instanceof Card) {
                    players.add(((Card) rem).getController());
                }
            }
        } else if (defined.startsWith("Triggered")) {
            final SpellAbility root = sa.getRootAbility();
            Object o = null;
            if (defined.endsWith("Controller")) {
                String triggeringType = defined.substring(9);
                triggeringType = triggeringType.substring(0, triggeringType.length() - 10);
                final Object c = root.getTriggeringObject(triggeringType);
                if (c instanceof Card) {
                    o = ((Card) c).getController();
                }
                if (c instanceof SpellAbility) {
                    o = ((SpellAbility) c).getSourceCard().getController();
                }
            } else if (defined.endsWith("Opponent")) {
                String triggeringType = defined.substring(9);
                triggeringType = triggeringType.substring(0, triggeringType.length() - 8);
                final Object c = root.getTriggeringObject(triggeringType);
                if (c instanceof Card) {
                    o = ((Card) c).getController().getOpponents();
                }
                if (c instanceof SpellAbility) {
                    o = ((SpellAbility) c).getSourceCard().getController().getOpponents();
                }
            } else if (defined.endsWith("Owner")) {
                String triggeringType = defined.substring(9);
                triggeringType = triggeringType.substring(0, triggeringType.length() - 5);
                final Object c = root.getTriggeringObject(triggeringType);
                if (c instanceof Card) {
                    o = ((Card) c).getOwner();
                }
            } else {
                final String triggeringType = defined.substring(9);
                o = root.getTriggeringObject(triggeringType);
            }
            if (o != null) {
                if (o instanceof Player) {
                    final Player p = (Player) o;
                    if (!players.contains(p)) {
                        players.add(p);
                    }
                }
                if (o instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    final List<Player> pList = (List<Player>) o;
                    if (!pList.isEmpty() && pList.get(0) instanceof Player) {
                        for (final Player p : pList) {
                            if (!players.contains(p)) {
                                players.add(p);
                            }
                        }
                    }
                }
            }
        } else if (defined.startsWith("Replaced")) {
            final SpellAbility root = sa.getRootAbility();
            Object o = null;
            if (defined.endsWith("Controller")) {
                String replacingType = defined.substring(8);
                replacingType = replacingType.substring(0, replacingType.length() - 10);
                final Object c = root.getReplacingObject(replacingType);
                if (c instanceof Card) {
                    o = ((Card) c).getController();
                }
                if (c instanceof SpellAbility) {
                    o = ((SpellAbility) c).getSourceCard().getController();
                }
            } else if (defined.endsWith("Opponent")) {
                String replacingType = defined.substring(8);
                replacingType = replacingType.substring(0, replacingType.length() - 8);
                final Object c = root.getReplacingObject(replacingType);
                if (c instanceof Card) {
                    o = ((Card) c).getController().getOpponent();
                }
                if (c instanceof SpellAbility) {
                    o = ((SpellAbility) c).getSourceCard().getController().getOpponent();
                }
            } else if (defined.endsWith("Owner")) {
                String replacingType = defined.substring(8);
                replacingType = replacingType.substring(0, replacingType.length() - 5);
                final Object c = root.getReplacingObject(replacingType);
                if (c instanceof Card) {
                    o = ((Card) c).getOwner();
                }
            } else {
                final String replacingType = defined.substring(8);
                o = root.getReplacingObject(replacingType);
            }
            if (o != null) {
                if (o instanceof Player) {
                    final Player p = (Player) o;
                    if (!players.contains(p)) {
                        players.add(p);
                    }
                }
            }
        } else if (defined.equals("EnchantedController")) {
            if (card.getEnchantingCard() == null) {
                return players;
            }
            final Player p = card.getEnchantingCard().getController();
            if (!players.contains(p)) {
                players.add(p);
            }
        } else if (defined.equals("EnchantedOwner")) {
            if (card.getEnchantingCard() == null) {
                return players;
            }
            final Player p = card.getEnchantingCard().getOwner();
            if (!players.contains(p)) {
                players.add(p);
            }
        } else if (defined.equals("EnchantedPlayer")) {
            final Object o = sa.getSourceCard().getEnchanting();
            if (o instanceof Player) {
                if (!players.contains(o)) {
                    players.add((Player) o);
                }
            }
        } else if (defined.equals("AttackingPlayer")) {
            final Player p = Singletons.getModel().getGame().getCombat().getAttackingPlayer();
            if (!players.contains(p)) {
                players.add(p);
            }
        } else if (defined.equals("DefendingPlayer")) {
            final Player p = Singletons.getModel().getGame().getCombat().getDefendingPlayerRelatedTo(card);
            if (!players.contains(p)) {
                players.add(p);
            }
        } else if (defined.equals("ChosenPlayer")) {
            final Player p = card.getChosenPlayer();
            if (!players.contains(p)) {
                players.add(p);
            }
        } else if (defined.equals("SourceController")) {
            final Player p = sa.getSourceCard().getController();
            if (!players.contains(p)) {
                players.add(p);
            }
        } else if (defined.startsWith("Flipped")) {
            for (Player p : Singletons.getModel().getGame().getPlayers()) {
                if (null != sa.getSourceCard().getFlipResult(p)) {
                    if (sa.getSourceCard().getFlipResult(p).equals(defined.substring(7))) {
                        players.add(p);
                    }
                }
            }
        } else if (defined.equals("You")) {
            players.add(sa.getActivatingPlayer());
        } else if (defined.equals("Each")) {
            players.addAll(Singletons.getModel().getGame().getPlayers());
        } else if (defined.equals("Opponent")) {
            players.add(sa.getActivatingPlayer().getOpponent());
        } else {
            for (Player p : Singletons.getModel().getGame().getPlayers()) {
                if (p.isValid(defined, sa.getActivatingPlayer(), sa.getSourceCard())) {
                    players.add(p);
                }
            }
        }
        return players;
    }

    /**
     * <p>
     * getDefinedSpellAbilities.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @param def
     *            a {@link java.lang.String} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public static ArrayList<SpellAbility> getDefinedSpellAbilities(final Card card, final String def,
            final SpellAbility sa) {
        final ArrayList<SpellAbility> sas = new ArrayList<SpellAbility>();
        final String defined = (def == null) ? "Self" : def; // default to Self

        SpellAbility s = null;

        // TODO - this probably needs to be fleshed out a bit, but the basics
        // work
        if (defined.equals("Self")) {
            s = sa;
        } else if (defined.equals("Targeted")) {
            final SpellAbility saTargeting = sa.getSATargetingSA();
            if (saTargeting != null) {
                sas.addAll(saTargeting.getTarget().getTargetSAs());
            }
        } else if (defined.startsWith("Triggered")) {
            final SpellAbility root = sa.getRootAbility();

            final String triggeringType = defined.substring(9);
            final Object o = root.getTriggeringObject(triggeringType);
            if (o instanceof SpellAbility) {
                s = (SpellAbility) o;
            }
        } else if (defined.equals("Remembered")) {
            for (final Object o : card.getRemembered()) {
                if (o instanceof Card) {
                    final Card rem = (Card) o;
                    sas.addAll(Singletons.getModel().getGame().getCardState(rem).getSpellAbilities());
                }
            }
        } else if (defined.equals("Imprinted")) {
            for (final Card imp : card.getImprinted()) {
                sas.addAll(imp.getSpellAbilities());
            }
        } else if (defined.equals("EffectSource")) {
            if (card.getEffectSource() != null) {
                sas.addAll(card.getEffectSource().getSpellAbilities());
            }
        } else if (defined.equals("Imprinted.doesNotShareNameWith+TriggeredCard+Exiled")) {
            //get Imprinted list
            ArrayList<SpellAbility> imprintedCards = new ArrayList<SpellAbility>();
            for (final Card imp : card.getImprinted()) {
                imprintedCards.addAll(imp.getSpellAbilities());
            } //get Triggered card
            Card triggeredCard = null;
            final SpellAbility root = sa.getRootAbility();
            final Object crd = root.getTriggeringObject("Card");
            if (crd instanceof Card) {
                triggeredCard = Singletons.getModel().getGame().getCardState((Card) crd);
            } //find the imprinted card that does not share a name with the triggered card
            for (final SpellAbility spell : imprintedCards) {
                if (!spell.getSourceCard().getName().equals(triggeredCard.getName())) {
                    sas.add(spell);
                }
            } //is it exiled?
            if (!sas.get(0).getSourceCard().isInZone(ZoneType.Exile)) {
                sas.clear();
            }
        }

        if (s != null) {
            sas.add(s);
        }

        return sas;
    }

    public static ArrayList<String> getProtectionList(final SpellAbility sa) {
        final ArrayList<String> gains = new ArrayList<String>();

        final String gainStr = sa.getParam("Gains");
        if (gainStr.equals("Choice")) {
            String choices = sa.getParam("Choices");

            // Replace AnyColor with the 5 colors
            if (choices.contains("AnyColor")) {
                gains.addAll(Arrays.asList(Constant.Color.ONLY_COLORS));
                choices = choices.replaceAll("AnyColor,?", "");
            }
            // Add any remaining choices
            if (choices.length() > 0) {
                gains.addAll(Arrays.asList(choices.split(",")));
            }
        } else {
            gains.addAll(Arrays.asList(gainStr.split(",")));
        }
        return gains;
    }




    /////////////////////////////////////////////////////////////////////////////////////
    //
    // BELOW ARE resove() METHOD AND ITS DEPENDANTS, CONSIDER MOVING TO DEDICATED CLASS
    //
    /////////////////////////////////////////////////////////////////////////////////////
    public static void resolve(final SpellAbility sa, final boolean usedStack) {
        if (sa == null) {
            return;
        }
        final ApiType api = sa.getApi();
        if (api == null) {
            sa.resolve();
            if (sa.getSubAbility() != null) {
                resolve(sa.getSubAbility(), usedStack);
            }
            return;
        }

        final GameState game = Singletons.getModel().getGame();
        AbilityUtils.resolveApiAbility(sa, usedStack, game);
    }

    private static void resolveSubAbilities(final SpellAbility sa, boolean usedStack, final GameState game) {
        final AbilitySub abSub = sa.getSubAbility();
        if (abSub == null || sa.isWrapper()) {
            // every resolving spellAbility will end here
            if (usedStack) {
                SpellAbility root = sa.getRootAbility();
                game.getStack().finishResolving(root, false);
            }
            return;
        }
        // check conditions
        AbilityUtils.resolveApiAbility(abSub, usedStack, game);
    }

    private static void handleUnlessCost(final SpellAbility sa, final boolean usedStack, final GameState game) {
        final Card source = sa.getSourceCard();
        String unlessCost = sa.getParam("UnlessCost");
        unlessCost = unlessCost.trim();

        // The player who has the chance to cancel the ability
        final String pays = sa.hasParam("UnlessPayer") ? sa.getParam("UnlessPayer") : "TargetedController";
        final List<Player> payers = getDefinedPlayers(sa.getSourceCard(), pays, sa);
        final String  resolveSubs = sa.getParam("UnlessResolveSubs"); // no value means 'Always'
        final boolean execSubsWhenPaid = "WhenPaid".equals(resolveSubs) || StringUtils.isBlank(resolveSubs);
        final boolean execSubsWhenNotPaid = "WhenNotPaid".equals(resolveSubs) || StringUtils.isBlank(resolveSubs);

        // The cost
        if (unlessCost.equals("CardManaCost")) {
            unlessCost = source.getManaCost().toString();
        } else if (unlessCost.equals("RememberedCostMinus2")) {
            if (source.getRemembered().isEmpty() || !(source.getRemembered().get(0) instanceof Card)) {
                sa.resolve();
                resolveSubAbilities(sa, usedStack, game);
            }
            Card rememberedCard = (Card) source.getRemembered().get(0);
            unlessCost = rememberedCard.getManaCost().toString();
            ManaCostBeingPaid newCost = new ManaCostBeingPaid(unlessCost.toString());
            newCost.decreaseColorlessMana(2);
            unlessCost = newCost.toString();
        } else {
            try {
                String unlessVar = Integer.toString(calculateAmount(source, sa.getParam("UnlessCost").replace(" ", ""), sa));
                unlessCost = unlessVar;
            } catch (final NumberFormatException n) {
            } //This try/catch method enables UnlessCost to parse any svar name
              //instead of just X for cards like Draco.
        }

        final boolean isSwitched = sa.hasParam("UnlessSwitched");

        Command paidCommand = new Command() {
            private static final long serialVersionUID = 8094833091127334678L;

            @Override
            public void execute() {
                if (isSwitched && execSubsWhenNotPaid || execSubsWhenPaid) {
                    resolveSubAbilities(sa, usedStack, game);
                }
            }
        };

        Command unpaidCommand = new Command() {
            private static final long serialVersionUID = 8094833091127334678L;

            @Override
            public void execute() {
                sa.resolve();
                if (isSwitched && execSubsWhenPaid || execSubsWhenNotPaid) {
                    resolveSubAbilities(sa, usedStack, game);
                }
            }
        };

        if (isSwitched) {
            final Command dummy = paidCommand;
            paidCommand = unpaidCommand;
            unpaidCommand = dummy;
        }

        final Cost cost = new Cost(source, unlessCost, true);
        final Ability ability = new AbilityStatic(source, cost, null) {
            @Override
            public void resolve() {
                // nothing to do here
            }
        };

        boolean paid = false;
        for (Player payer : payers) {
            if (payer.isComputer()) {
                if (AbilityUtils.willAIPayForAbility(sa, payer, ability, paid, payers)) {
                    ability.setActivatingPlayer(payer);
                    ability.setTarget(sa.getTarget());
                    ComputerUtil.playNoStack((AIPlayer) payer, ability, game); // Unless cost was payed - no resolve
                    paid = true;
                }
            }
        }
        boolean waitForInput = false;
        for (Player payer : payers) {
            if (payer.isHuman()) {
                // if it's paid by the AI already the human can pay, but it won't change anything
                if (paid) {
                    unpaidCommand = paidCommand;
                }
                ability.setActivatingPlayer(payer);
                ability.setTarget(sa.getTarget());
                GameActionUtil.payCostDuringAbilityResolve(payer, ability, cost, paidCommand, unpaidCommand, sa, game);
                waitForInput = true; // wait for the human input
                break; // multiple human players are not supported
            }
        }
        if (!waitForInput) {
            Command toExecute = paid ? paidCommand : unpaidCommand;
            toExecute.execute();
        }

    }

    /**
     * <p>
     * handleRemembering.
     * </p>
     * 
     * @param sa
     *            a SpellAbility object.
     */
    public static void handleRemembering(final SpellAbility sa) {
        Card host = sa.getSourceCard();

        if (sa.hasParam("RememberTargets") && sa.getTarget() != null) {
            if (sa.hasParam("ForgetOtherTargets")) {
                host.clearRemembered();
            }
            for (final Object o : sa.getTarget().getTargets()) {
                host.addRemembered(o);
            }
        }

        if (sa.hasParam("RememberCostCards")) {
            if (sa.getParam("Cost").contains("Exile")) {
                final List<Card> paidListExiled = sa.getPaidList("Exiled");
                for (final Card exiledAsCost : paidListExiled) {
                    host.addRemembered(exiledAsCost);
                }
            } else if (sa.getParam("Cost").contains("Sac")) {
                final List<Card> paidListSacrificed = sa.getPaidList("Sacrificed");
                for (final Card sacrificedAsCost : paidListSacrificed) {
                    host.addRemembered(sacrificedAsCost);
                }
            } else if (sa.getParam("Cost").contains("tapXType")) {
                final List<Card> paidListTapped = sa.getPaidList("Tapped");
                for (final Card tappedAsCost : paidListTapped) {
                    host.addRemembered(tappedAsCost);
                }
            } else if (sa.getParam("Cost").contains("Unattach")) {
                final List<Card> paidListUnattached = sa.getPaidList("Unattached");
                for (final Card unattachedAsCost : paidListUnattached) {
                    host.addRemembered(unattachedAsCost);
                }
            }
        }
    }

    private static boolean willAIPayForAbility(SpellAbility sa, Player payer, SpellAbility ability, boolean paid, List<Player> payers) {
        Card source = sa.getSourceCard();
        boolean payForOwnOnly = "OnlyOwn".equals(sa.getParam("UnlessAI"));
        boolean payNever = "Never".equals(sa.getParam("UnlessAI"));
        boolean isMine = sa.getActivatingPlayer().equals(payer);

        if (payNever) { return false; }
        if (payForOwnOnly && !isMine) { return false; }

        // AI will only pay when it's not already payed and only opponents abilities
        if (paid || (payers.size() > 1 && (isMine && !payForOwnOnly))) {
            return false;
        }
        if (ComputerUtilCost.canPayCost(ability, payer)
                && ComputerUtilCost.checkLifeCost(payer, ability.getPayCosts(), source, 4, sa)
                && ComputerUtilCost.checkDamageCost(payer, ability.getPayCosts(), source, 4)
                && ComputerUtilCost.checkDiscardCost(payer, ability.getPayCosts(), source)
                && (!source.getName().equals("Tyrannize") || payer.getCardsIn(ZoneType.Hand).size() > 2)
                && (!source.getName().equals("Breaking Point") || payer.getCreaturesInPlay().size() > 1)
                && (!source.getName().equals("Chain of Vapor")
                        || (payer.getOpponent().getCreaturesInPlay().size() > 0 && payer.getLandsInPlay().size() > 3))) {
            // AI was crashing because the blank ability used to pay costs
            // Didn't have any of the data on the original SA to pay dependant costs
            return true;
        }
        return false;
    }

    private static void resolveApiAbility(final SpellAbility sa, boolean usedStack, final GameState game) {
        // check conditions
        if (sa.getConditions().areMet(sa)) {
            if (sa.isWrapper() || StringUtils.isBlank(sa.getParam("UnlessCost"))) {
                sa.resolve();
            } else {
                handleUnlessCost(sa, usedStack, game);
                return;
            }
        }
        resolveSubAbilities(sa, usedStack, game);
    }

    /**
     * <p>
     * Parse non-mana X variables.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param s
     *            a {@link java.lang.String} object.
     * @param sa
     *            a {@link forge.SpellAbility} object.
     * @return a int.
     */
    public static int xCount(final Card c, final String s, final SpellAbility sa) {

        final String[] l = s.split("/");
        final String[] m = CardFactoryUtil.parseMath(l);

        final String[] sq;
        sq = l[0].split("\\.");

        if (sa != null) {
            // Count$Kicked.<numHB>.<numNotHB>
            if (sq[0].startsWith("Kicked")) {
                if (sa.isKicked()) {
                    return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c); // Kicked
                } else {
                    return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c); // not Kicked
                }
            }

            // Count$Compare <int comparator value>.<True>.<False>
            if (sq[0].startsWith("Compare")) {
                final String[] compString = sq[0].split(" ");
                final int lhs = calculateAmount(c, compString[1], sa);
                final int rhs =  calculateAmount(c, compString[2].substring(2), sa);
                if (Expressions.compare(lhs, compString[2], rhs)) {
                    return CardFactoryUtil.doXMath(Integer.parseInt(sq[1]), m, c);
                } else {
                    return CardFactoryUtil.doXMath(Integer.parseInt(sq[2]), m, c);
                }
            }
        }
        return CardFactoryUtil.xCount(c, s);
    }

}
