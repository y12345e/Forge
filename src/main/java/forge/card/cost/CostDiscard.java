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
package forge.card.cost;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Predicate;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.FThreads;
import forge.card.ability.AbilityUtils;
import forge.card.spellability.SpellAbility;
import forge.control.input.InputSelectCards;
import forge.control.input.InputSelectCardsFromList;
import forge.game.GameState;
import forge.game.player.AIPlayer;
import forge.game.player.Player;
import forge.game.zone.ZoneType;

/**
 * The Class CostDiscard.
 */
public class CostDiscard extends CostPartWithList {
    // Discard<Num/Type{/TypeDescription}>

    // Inputs
    
    /**
     * Instantiates a new cost discard.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @param description
     *            the description
     */
    public CostDiscard(final String amount, final String type, final String description) {
        super(amount, type, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Discard ");

        final Integer i = this.convertAmount();

        if (this.payCostFromSource()) {
            sb.append(this.getType());
        } else if (this.getType().equals("Hand")) {
            sb.append("your hand");
        } else if (this.getType().equals("LastDrawn")) {
            sb.append("the last card you drew this turn");
        } else {
            final StringBuilder desc = new StringBuilder();

            if (this.getType().equals("Card") || this.getType().equals("Random")) {
                desc.append("card");
            } else {
                desc.append(this.getTypeDescription() == null ? this.getType() : this.getTypeDescription()).append(
                        " card");
            }

            sb.append(Cost.convertAmountTypeToWords(i, this.getAmount(), desc.toString()));

            if (this.getType().equals("Random")) {
                sb.append(" at random");
            }
        }
        return sb.toString();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#canPay(forge.card.spellability.SpellAbility,
     * forge.Card, forge.Player, forge.card.cost.Cost)
     */
    @Override
    public final boolean canPay(final SpellAbility ability, final Card source, final Player activator, final Cost cost, final GameState game) {
        List<Card> handList = new ArrayList<Card>(activator.getCardsIn(ZoneType.Hand));
        String type = this.getType();
        final Integer amount = this.convertAmount();

        if (this.payCostFromSource()) {
            if (!source.isInZone(ZoneType.Hand)) {
                return false;
            }
        } else if (type.equals("Hand")) {
            // this will always work
        } else if (type.equals("LastDrawn")) {
            final Card c = activator.getLastDrawnCard();
            return handList.contains(c);
        } else {
            if (ability.isSpell()) {
                handList.remove(source); // can't pay for itself
            }
            boolean sameName = false;
            if (type.contains("+WithSameName")) {
                sameName = true;
                type = type.replace("+WithSameName", "");
            }
            if (!type.equals("Random")) {
                handList = CardLists.getValidCards(handList, type.split(";"), activator, source);
            }
            if (sameName) {
                for (Card c : handList) {
                    if (CardLists.filter(handList, CardPredicates.nameEquals(c.getName())).size() > 1) {
                        return true;
                    }
                }
                return false;
            }
            if ((amount != null) && (amount > handList.size())) {
                // not enough cards in hand to pay
                return false;
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#payAI(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final void payAI(final AIPlayer ai, final SpellAbility ability, final Card source, final CostPayment payment, final GameState game) {
        for (final Card c : this.getList()) {
            executePayment(ability, c);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#payHuman(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final boolean payHuman(final SpellAbility ability, final GameState game) {
        final Player activator = ability.getActivatingPlayer();
        final Card source = ability.getSourceCard();
        List<Card> handList = new ArrayList<Card>(activator.getCardsIn(ZoneType.Hand));
        String discardType = this.getType();
        final String amount = this.getAmount();
        this.resetList();

        if (this.payCostFromSource()) {
            if (!handList.contains(source)) {
                return false;
            }
            executePayment(ability, source);
            return true;
            //this.addToList(source);
        } else if (discardType.equals("Hand")) {
            this.setList(handList);
            activator.discardHand(ability);
            return true;
        } else if (discardType.equals("LastDrawn")) {
            final Card lastDrawn = activator.getLastDrawnCard();
            if (!handList.contains(lastDrawn)) {
                return false;
            }
            executePayment(ability, lastDrawn);
            return true;
        } else {
            Integer c = this.convertAmount();

            if (discardType.equals("Random")) {
                if (c == null) {
                    final String sVar = ability.getSVar(amount);
                    // Generalize this
                    if (sVar.equals("XChoice")) {
                        c = CostUtil.chooseXValue(source, ability,  handList.size());
                    } else {
                        c = AbilityUtils.calculateAmount(source, amount, ability);
                    }
                }

                this.setList(activator.discardRandom(c, ability));
                return true;
            } else {
                String type = new String(discardType);
                boolean sameName = false;
                if (type.contains("+WithSameName")) {
                    sameName = true;
                    type = type.replace("+WithSameName", "");
                }
                final String[] validType = type.split(";");
                handList = CardLists.getValidCards(handList, validType, activator, ability.getSourceCard());
                final List<Card> landList2 = handList;
                if (sameName) {
                    handList = CardLists.filter(handList, new Predicate<Card>() {
                        @Override
                        public boolean apply(final Card c) {
                            for (Card card : landList2) {
                                if (!card.equals(c) && card.getName().equals(c.getName())) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
                }

                if (c == null) {
                    final String sVar = ability.getSVar(amount);
                    // Generalize this
                    if (sVar.equals("XChoice")) {
                        c = CostUtil.chooseXValue(source, ability, handList.size());
                    } else {
                        c = AbilityUtils.calculateAmount(source, amount, ability);
                    }
                }

                InputSelectCards inp = new InputSelectCardsFromList(c, c, handList);
                inp.setMessage("Select %d more " + getDescriptiveType() + " to discard.");
                //InputPayment inp = new InputPayCostDiscard(ability, handList, this, c, discardType);
                FThreads.setInputAndWait(inp);
                if( inp.hasCancelled() || inp.getSelected().size() != c)
                    return false;
                for(Card crd : inp.getSelected())
                    executePayment(ability, crd);
                return true;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#decideAIPayment(forge.card.spellability.SpellAbility
     * , forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final boolean decideAIPayment(final AIPlayer ai, final SpellAbility ability, final Card source, final CostPayment payment) {
        final String type = this.getType();

        final List<Card> hand = ai.getCardsIn(ZoneType.Hand);
        this.resetList();
        if (type.equals("LastDrawn")) {
            if (!hand.contains(ai.getLastDrawnCard())) {
                return false;
            }
            this.addToList(ai.getLastDrawnCard());
        }

        else if (this.payCostFromSource()) {
            if (!hand.contains(source)) {
                return false;
            }

            this.addToList(source);
        }

        else if (type.equals("Hand")) {
            this.getList().addAll(hand);
        }

        else {
            if (type.contains("WithSameName")) {
                return false;
            }
            Integer c = this.convertAmount();
            if (c == null) {
                final String sVar = ability.getSVar(this.getAmount());
                if (sVar.equals("XChoice")) {
                    return false;
                }
                c = AbilityUtils.calculateAmount(source, this.getAmount(), ability);
            }

            if (type.equals("Random")) {
                this.setList(CardLists.getRandomSubList(hand, c));
            } else {
                this.setList(ai.getAi().getCardsToDiscard(c, type.split(";"), ability));
            }
        }
        return this.getList() != null;
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#executePayment(forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    public void executePayment(SpellAbility ability, Card targetCard) {
        this.addToList(targetCard);
        targetCard.getController().discard(targetCard, ability);
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#getHashForList()
     */
    @Override
    public String getHashForList() {
        return "Discarded";
    }

    // Inputs

}
