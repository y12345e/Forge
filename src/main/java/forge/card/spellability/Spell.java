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
package forge.card.spellability;

import java.util.ArrayList;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.Card;
import forge.CardList;
import forge.Constant;
import forge.Constant.Zone;
import forge.PhaseHandler;
import forge.Player;
import forge.card.cost.Cost;
import forge.card.cost.CostPayment;
import forge.card.staticability.StaticAbility;
import forge.error.ErrorViewer;

/**
 * <p>
 * Abstract Spell class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public abstract class Spell extends SpellAbility implements java.io.Serializable, Cloneable {

    /** Constant <code>serialVersionUID=-7930920571482203460L</code>. */
    private static final long serialVersionUID = -7930920571482203460L;

    /**
     * <p>
     * Constructor for Spell.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     */
    public Spell(final Card sourceCard) {
        super(SpellAbility.getSpell(), sourceCard);

        this.setManaCost(sourceCard.getManaCost());
        this.setStackDescription(sourceCard.getSpellText());
        this.getRestrictions().setZone(Constant.Zone.Hand);
    }

    /**
     * <p>
     * Constructor for Spell.
     * </p>
     * 
     * @param sourceCard
     *            a {@link forge.Card} object.
     * @param abCost
     *            a {@link forge.card.cost.Cost} object.
     * @param abTgt
     *            a {@link forge.card.spellability.Target} object.
     */
    public Spell(final Card sourceCard, final Cost abCost, final Target abTgt) {
        super(SpellAbility.getSpell(), sourceCard);

        this.setManaCost(sourceCard.getManaCost());

        this.setPayCosts(abCost);
        this.setTarget(abTgt);
        this.setStackDescription(sourceCard.getSpellText());
        this.getRestrictions().setZone(Constant.Zone.Hand);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPlay() {
        if (AllZone.getStack().isSplitSecondOnStack()) {
            return false;
        }

        final Card card = this.getSourceCard();

        final Player activator = this.getActivatingPlayer();

        // CantBeCast static abilities
        final CardList allp = AllZoneUtil.getCardsIn(Zone.Battlefield);
        allp.add(card);
        for (final Card ca : allp) {
            final ArrayList<StaticAbility> staticAbilities = ca.getStaticAbilities();
            for (final StaticAbility stAb : staticAbilities) {
                if (stAb.applyAbility("CantBeCast", card, activator)) {
                    return false;
                }
            }
        }

        if (this.getPayCosts() != null) {
            if (!CostPayment.canPayAdditionalCosts(this.getPayCosts(), this)) {
                return false;
            }
        }

        if (!this.getRestrictions().canPlay(card, this)) {
            return false;
        }

        return (card.isInstant() || card.hasKeyword("Flash") || PhaseHandler.canCastSorcery(activator));
    } // canPlay()

    /** {@inheritDoc} */
    @Override
    public boolean canPlayAI() {
        final Card card = this.getSourceCard();
        if (card.getSVar("NeedsToPlay").length() > 0) {
            final String needsToPlay = card.getSVar("NeedsToPlay");
            CardList list = AllZoneUtil.getCardsIn(Zone.Battlefield);

            list = list.getValidCards(needsToPlay.split(","), card.getController(), card);
            if (list.isEmpty()) {
                return false;
            }
        }

        return super.canPlayAI();
    }

    /** {@inheritDoc} */
    @Override
    public String getStackDescription() {
        return super.getStackDescription();
    }

    /** {@inheritDoc} */
    @Override
    public final Object clone() {
        try {
            return super.clone();
        } catch (final Exception ex) {
            ErrorViewer.showError(ex);
            throw new RuntimeException("Spell : clone() error, " + ex);
        }
    }

    /**
     * <p>
     * canPlayFromEffectAI.
     * </p>
     *
     * @param mandatory
     *            can the controller chose not to play the spell
     * @param withOutManaCost
     *            is the spell cast without paying mana
     * @return a boolean.
     */
    public boolean canPlayFromEffectAI(boolean mandatory, boolean withOutManaCost) {
        return canPlayAI();
    }
}
