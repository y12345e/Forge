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
package forge.gui.input;

import forge.AllZone;
import forge.ButtonUtil;
import forge.Card;
import forge.Constant.Zone;
import forge.PhaseHandler;
import forge.Player;
import forge.PlayerZone;
import forge.Singletons;
import forge.card.mana.ManaCost;
import forge.card.spellability.SpellAbility;

//pays the cost of a card played from the player's hand
//the card is removed from the players hand if the cost is paid
//CANNOT be used for ABILITIES
/**
 * <p>
 * Input_PayManaCost class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class InputPayManaCost extends InputMana {
    // anything that uses this should be converted to Ability_Cost
    /** Constant <code>serialVersionUID=3467312982164195091L</code>. */
    private static final long serialVersionUID = 3467312982164195091L;

    private final String originalManaCost;

    private final Card originalCard;

    /** The mana cost. */
    private ManaCost manaCost;

    private final SpellAbility spell;

    private boolean skipStack;

    private int phyLifeToLose = 0;

    /**
     * <p>
     * Constructor for Input_PayManaCost.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param noStack
     *            a boolean.
     */
    public InputPayManaCost(final SpellAbility sa, final boolean noStack) {
        this.skipStack = noStack;
        this.originalManaCost = sa.getManaCost(); // Change
        this.originalCard = sa.getSourceCard();

        this.spell = sa;

        if (PhaseHandler.getGameBegins() == 1) {
            if (sa.getSourceCard().isCopiedSpell() && sa.isSpell()) {
                if (this.spell.getAfterPayMana() != null) {
                    this.stopSetNext(this.spell.getAfterPayMana());
                } else {
                    this.manaCost = new ManaCost("0");
                    AllZone.getStack().add(this.spell);
                }
            } else {
                this.manaCost = AllZone.getGameAction().getSpellCostChange(sa, new ManaCost(this.originalManaCost));
            }
        } else {
            this.manaCost = new ManaCost(sa.getManaCost());
        }
    }

    /**
     * <p>
     * Constructor for Input_PayManaCost.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    public InputPayManaCost(final SpellAbility sa) {
        this(sa, new ManaCost(sa.getManaCost()));
    }

    /**
     * <p>
     * Constructor for Input_PayManaCost.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * 
     * @param manaCostToPay
     *            a {@link forge.card.mana.ManaCost} object.
     */
    public InputPayManaCost(final SpellAbility sa, final ManaCost manaCostToPay) {
        this.originalManaCost = manaCostToPay.toString(); // Change
        this.originalCard = sa.getSourceCard();

        this.spell = sa;

        if (PhaseHandler.getGameBegins() == 1) {
            if (sa.getSourceCard().isCopiedSpell() && sa.isSpell()) {
                if (this.spell.getAfterPayMana() != null) {
                    this.stopSetNext(this.spell.getAfterPayMana());
                } else {
                    this.manaCost = new ManaCost("0");
                    AllZone.getStack().add(this.spell);
                }
            } else {
                this.manaCost = manaCostToPay; // AllZone.getGameAction().getSpellCostChange(sa,
                                               // new
                                               // ManaCost(this.originalManaCost));
            }
        } else {
            this.manaCost = new ManaCost(sa.getManaCost());
        }
    }

    /**
     * <p>
     * resetManaCost.
     * </p>
     */
    private void resetManaCost() {
        this.manaCost = new ManaCost(this.originalManaCost);
        this.phyLifeToLose = 0;
    }

    /** {@inheritDoc} */
    @Override
    public final void selectCard(final Card card, final PlayerZone zone) {
        // this is a hack, to prevent lands being able to use mana to pay their
        // own abilities from cards like
        // Kher Keep, Pendelhaven, Blinkmoth Nexus, and Mikokoro, Center of the
        // Sea, ....

        if (this.originalCard.equals(card) && this.spell.isTapAbility()) {
            // I'm not sure if this actually prevents anything that wouldn't be
            // handled by canPlay below
            return;
        }

        this.manaCost = InputPayManaCostUtil.activateManaAbility(this.spell, card, this.manaCost);

        // only show message if this is the active input
        if (AllZone.getInputControl().getInput() == this) {
            this.showMessage();
        }

        if (this.manaCost.isPaid()) {
            this.originalCard.setSunburstValue(this.manaCost.getSunburst());
            this.done();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void selectPlayer(final Player player) {

        if (player.isHuman()) {
            if (this.manaCost.payPhyrexian()) {
                this.phyLifeToLose += 2;
            }

            this.showMessage();
        }

    }

    /**
     * <p>
     * done.
     * </p>
     */
    private void done() {
        if (this.phyLifeToLose > 0) {
            AllZone.getHumanPlayer().payLife(this.phyLifeToLose, this.originalCard);
        }
        if (this.spell.getSourceCard().isCopiedSpell()) {
            if (this.spell.getAfterPayMana() != null) {
                this.stopSetNext(this.spell.getAfterPayMana());
            } else {
                AllZone.getInputControl().resetInput();
            }
        } else {
            AllZone.getHumanPlayer().getManaPool().clearPay(this.spell, false);
            this.resetManaCost();

            // if tap ability, tap card
            if (this.spell.isTapAbility()) {
                this.originalCard.tap();
            }
            if (this.spell.isUntapAbility()) {
                this.originalCard.untap();
            }

            // if this is a spell, move it to the Stack ZOne

            if (this.spell.isSpell()) {
                this.spell.setSourceCard(AllZone.getGameAction().moveToStack(this.originalCard));
            }

            if (this.spell.getAfterPayMana() != null) {
                this.stopSetNext(this.spell.getAfterPayMana());
            } else {
                if (this.skipStack) {
                    this.spell.resolve();
                } else {
                    AllZone.getStack().add(this.spell);
                }
                AllZone.getInputControl().resetInput();
            }

            // If this is a spell with convoke, re-tap all creatures used for
            // it.
            // This is done to make sure Taps triggers go off at the right time
            // (i.e. AFTER cost payment, they are tapped previously as well so
            // that
            // any mana tapabilities can't be used in payment as well as being
            // tapped for convoke)

            if (this.spell.getTappedForConvoke() != null) {
                AllZone.getTriggerHandler().suppressMode("Untaps");
                for (final Card c : this.spell.getTappedForConvoke()) {
                    c.untap();
                    c.tap();
                }
                AllZone.getTriggerHandler().clearSuppression("Untaps");
                this.spell.clearTappedForConvoke();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void selectButtonCancel() {
        // If this is a spell with convoke, untap all creatures used for it.
        if (this.spell.getTappedForConvoke() != null) {
            AllZone.getTriggerHandler().suppressMode("Untaps");
            for (final Card c : this.spell.getTappedForConvoke()) {
                c.untap();
            }
            AllZone.getTriggerHandler().clearSuppression("Untaps");
            this.spell.clearTappedForConvoke();
        }

        this.resetManaCost();
        AllZone.getHumanPlayer().getManaPool().unpaid(this.spell, true);
        AllZone.getHumanPlayer().getZone(Zone.Battlefield).updateObservers(); // DO

        this.stop();
    }

    /** {@inheritDoc} */
    @Override
    public final void showMessage() {
        ButtonUtil.enableOnlyCancel();

        final StringBuilder msg = new StringBuilder("Pay Mana Cost: " + this.manaCost.toString());
        if (this.phyLifeToLose > 0) {
            msg.append(" (");
            msg.append(this.phyLifeToLose);
            msg.append(" life paid for phyrexian mana)");
        }

        if (this.manaCost.containsPhyrexianMana()) {
            msg.append("\n(Click on your life total to pay life for phyrexian mana.)");
        }

        Singletons.getControl().getMatchControl().showMessage(msg.toString());
        if (this.manaCost.isPaid() && !new ManaCost(this.originalManaCost).isPaid()) {
            this.originalCard.setSunburstValue(this.manaCost.getSunburst());
            this.done();
        }

    }

    /* (non-Javadoc)
     * @see forge.gui.input.InputMana#selectManaPool(String)
     */
    @Override
    public void selectManaPool(String color) {
        this.manaCost = InputPayManaCostUtil.activateManaAbility(color, this.spell, this.manaCost);

        // only show message if this is the active input
        if (AllZone.getInputControl().getInput() == this) {
            this.showMessage();
        }

        if (this.manaCost.isPaid()) {
            this.originalCard.setSunburstValue(this.manaCost.getSunburst());
            this.done();
        }
    }
}
