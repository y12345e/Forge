package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import forge.Card;
import forge.Constant;
import forge.CounterType;
import forge.card.MagicColor;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.spellability.AbilityManaPart;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.TargetRestrictions;
import forge.game.GameActionUtil;
import forge.game.Game;
import forge.game.ai.ComputerUtilCard;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;

public class ManaEffect extends SpellAbilityEffect {

    @Override
    public void resolve(SpellAbility sa) {

        final Card card = sa.getSourceCard();

        AbilityManaPart abMana = sa.getManaPart();

        // Spells are not undoable
        sa.setUndoable(sa.isAbility() && sa.isUndoable());

        final List<Player> tgtPlayers = getTargetPlayers(sa);
        final TargetRestrictions tgt = sa.getTargetRestrictions();
        final boolean optional = sa.hasParam("Optional");
        final Game game = sa.getActivatingPlayer().getGame();

        if (optional && !sa.getActivatingPlayer().getController().confirmAction(sa, null, "Do you want to add mana to your mana pool?")) {
            return;
        }
        if (abMana.isComboMana()) {
            for (Player p : tgtPlayers) {
                int amount = sa.hasParam("Amount") ? AbilityUtils.calculateAmount(card, sa.getParam("Amount"), sa) : 1;
                if (tgt == null || p.canBeTargetedBy(sa)) {
                    Player activator = sa.getActivatingPlayer();
                    // AI color choice is set in ComputerUtils so only human players need to make a choice
                    if (activator.isHuman()) {
                        //String colorsNeeded = abMana.getExpressChoice();
                        String[] colorsProduced = abMana.getComboColors().split(" ");
                        final StringBuilder choiceString = new StringBuilder();
                        List<String> colorMenu = null;
                        if (!abMana.isAnyMana()) {
                            colorMenu = new ArrayList<String>();
                            //loop through colors to make menu
                            for (int nColor = 0; nColor < colorsProduced.length; nColor++) {
                                colorMenu.add(forge.card.MagicColor.toLongString(colorsProduced[nColor]));
                            }
                        }
                        else {
                            colorMenu = Constant.Color.ONLY_COLORS;
                        }
                        for (int nMana = 1; nMana <= amount; nMana++) {
                            String choice = "";
                                Object o = GuiChoose.one("Select Mana to Produce", colorMenu);
                                if (o == null) {
                                    final StringBuilder sb = new StringBuilder();
                                    sb.append("AbilityFactoryMana::manaResolve() - Human color mana choice is empty for ");
                                    sb.append(card.getName());
                                    throw new RuntimeException(sb.toString());
                                } else {
                                    choice = MagicColor.toShortString((String) o);
                                    if (nMana != 1) {
                                        choiceString.append(" ");
                                    }
                                    choiceString.append(choice);
                                }
                        }
                        abMana.setExpressChoice(choiceString.toString());
                    }
                    else {
                        // TODO: Add some logic for AI choice (ArsenalNut 2012/09/16)
                        if (!sa.hasParam("AILogic") || sa.getParam("AILogic").equals("MostProminentInComputerHand")) {
                            String chosen = Constant.Color.BLACK;
                            List<Card> hand = new ArrayList<Card>(activator.getCardsIn(ZoneType.Hand));
                            hand.addAll(activator.getCardsIn(ZoneType.Stack));
                            chosen = ComputerUtilCard.getMostProminentColor(hand);
                            if (chosen.equals("")) {
                                chosen = Constant.Color.BLACK;
                            }
                            GuiChoose.one("Computer picked: ", new String[]{chosen});
                            String manaString = "";
                            for (int i = 0; i < amount; i++) {
                                manaString = manaString + MagicColor.toShortString(chosen) + " ";
                            }
                            abMana.setExpressChoice(manaString);
                        }
                        if (abMana.getExpressChoice().isEmpty() && amount > 0) {
                            System.out.println("AbilityFactoryMana::manaResolve() - combo mana color choice is empty for " + card.getName());
                        }
                    }
                }
            }
        } else if (abMana.isAnyMana()) {
            for (Player p : tgtPlayers) {
                if (tgt == null || p.canBeTargetedBy(sa)) {
                    Player act = sa.getActivatingPlayer();
                    // AI color choice is set in ComputerUtils so only human players need to make a choice
                    if (act.isHuman()) {
                        String colorsNeeded = abMana.getExpressChoice();
                        String choice = "";
                        if (colorsNeeded.length() == 1) {
                            choice = colorsNeeded;
                        }
                        else {
                            List<String> colorMenu = null;
                            if (colorsNeeded.length() > 1 && colorsNeeded.length() < 5) {
                                colorMenu = new ArrayList<String>();
                                //loop through colors to make menu
                                for (int nChar = 0; nChar < colorsNeeded.length(); nChar++) {
                                    colorMenu.add(forge.card.MagicColor.toLongString(colorsNeeded.substring(nChar, nChar + 1)));
                                }
                            }
                            else {
                                colorMenu = Constant.Color.ONLY_COLORS;
                            }
                            String s = GuiChoose.one("Select Mana to Produce", colorMenu);
                            if (s == null) {
                                final StringBuilder sb = new StringBuilder();
                                sb.append("AbilityFactoryMana::manaResolve() - Human color mana choice is empty for ");
                                sb.append(card.getName());
                                throw new RuntimeException(sb.toString());
                            } else {
                                choice = MagicColor.toShortString(s);
                            }
                        }
                        abMana.setExpressChoice(choice);
                    }
                    else {
                        if (abMana.getExpressChoice().isEmpty()) {
                            final String logic = sa.hasParam("AILogic") ? sa.getParam("AILogic") : null;
                            String chosen = Constant.Color.BLACK;
                            if (logic == null || logic.equals("MostProminentInComputerHand")) {
                                chosen = ComputerUtilCard.getMostProminentColor(act.getCardsIn(ZoneType.Hand));
                            }
                            if (chosen.equals("")) {
                                chosen = Constant.Color.GREEN;
                            }
                            GuiChoose.one("Computer picked: ", new String[]{chosen});
                            abMana.setExpressChoice(MagicColor.toShortString(chosen));
                        }
                        if (abMana.getExpressChoice().isEmpty()) {
                            final StringBuilder sb = new StringBuilder();
                            sb.append("AbilityFactoryMana::manaResolve() - any color mana choice is empty for ");
                            sb.append(card.getName());
                            throw new RuntimeException(sb.toString());
                        }
                    }
                }
            }
        } else if (abMana.isSpecialMana()) {
            for (Player p : tgtPlayers) {
                if (tgt == null || p.canBeTargetedBy(sa)) {
                    String type = abMana.getOrigProduced().split("Special ")[1];
                    String choice = "";
                    if (type.equals("EnchantedManaCost")) {
                        if (card.getEnchanting() != null ) {
                            Card enchanted = card.getEnchantingCard();
                            // Remove X and phyrexian mana
                            choice = enchanted.getManaCost().toString().replaceAll("X|/P", "").trim();
                        }
                    }
                    if (choice.equals("no cost") || choice.isEmpty()) {
                        choice = "0";
                    }
                    StringBuilder sb = new StringBuilder();
                    for (String s : choice.split(" ")) {
                        if (s.length() == 1 || StringUtils.isNumeric(s)) {
                            sb.append(" ").append(s);
                        } else { // Handle hybrid mana
                            sb.append(" ").append(sa.getActivatingPlayer().getController().chooseHybridMana(s));
                        }
                    }
                    abMana.setExpressChoice(sb.toString().trim());
                    if (abMana.getExpressChoice().isEmpty()) {
                        System.out.println("AbilityFactoryMana::manaResolve() - special mana effect is empty for " + sa.getSourceCard().getName());
                    }
                }
            }    
        }

        for (final Player player : tgtPlayers) {
            abMana.produceMana(GameActionUtil.generatedMana(sa), player, sa);
        }

        // Only clear express choice after mana has been produced
        abMana.clearExpressChoice();

        // convert these to SubAbilities when appropriate
        if (sa.hasParam("Stuck")) {
            sa.setUndoable(false);
            card.addHiddenExtrinsicKeyword("This card doesn't untap during your next untap step.");
        }

        final String deplete = sa.getParam("Deplete");
        if (deplete != null) {
            final int num = card.getCounters(CounterType.getType(deplete));
            if (num == 0) {
                sa.setUndoable(false);
                game.getAction().sacrifice(card, null);
            }
        }

        //resolveDrawback(sa);
    }

    /**
     * <p>
     * manaStackDescription.
     * </p>
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param abMana
     *            a {@link forge.card.spellability.AbilityMana} object.
     * @param af
     *            a {@link forge.card.ability.AbilityFactory} object.
     * 
     * @return a {@link java.lang.String} object.
     */

    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
        String mana = !sa.hasParam("Amount") || StringUtils.isNumeric(sa.getParam("Amount"))
                ? GameActionUtil.generatedMana(sa) : "mana";
        sb.append("Add ").append(mana).append(" to your mana pool.");
        return sb.toString();
    }
}
