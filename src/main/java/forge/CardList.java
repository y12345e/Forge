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
package forge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.code.jyield.Generator;
import com.google.code.jyield.Yieldable;

import forge.card.spellability.SpellAbility;
import forge.util.MyRandom;

/**
 * <p>
 * CardList class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CardList implements Iterable<Card> {

    /**
     * <p>
     * iterator.
     * </p>
     * 
     * @return a {@link java.util.Iterator} object.
     */
    @Override
    public final Iterator<Card> iterator() {
        return this.list.iterator();
    }

    private ArrayList<Card> list = new ArrayList<Card>();

    /**
     * <p>
     * Constructor for CardList.
     * </p>
     */
    public CardList() {
    }

    /**
     * <p>
     * Constructor for CardList.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    public CardList(final Card... c) {
        this.addAll(c);
    }

    /**
     * <p>
     * Constructor for CardList.
     * </p>
     * 
     * @param al
     *            a {@link java.util.ArrayList} object.
     */
    public CardList(final List<Card> al) {
        this.addAll(al);
    }

    /**
     * Make a shallow copy of an Iterable's contents; this could be another
     * CardList.
     * 
     * @param iterable
     *            we traverse this and copy its contents into a local field.
     */
    public CardList(final Iterable<Card> iterable) {
        for (final Card card : iterable) {
            this.add(card);
        }
    }

    /**
     * Create a CardList from a finite generator of Card instances.
     * 
     * We ignore null values produced by the generator.
     * 
     * @param generator
     *            a non-infinite generator of Card instances.
     */
    public CardList(final Generator<Card> generator) {
        // Generators yield their contents to a Yieldable. Here,
        // we create a quick Yieldable that adds the information it
        // receives to this CardList's list field.

        final Yieldable<Card> valueReceiver = new Yieldable<Card>() {
            @Override
            public void yield(final Card card) {
                if (card != null) {
                    CardList.this.list.add(card);
                }
            }
        };

        generator.generate(valueReceiver);
    }

    /**
     * Create a cardlist with an initial estimate of its maximum size.
     * 
     * @param size
     *            an initialize estimate of its maximum size
     */
    public CardList(final int size) {
        this.list = new ArrayList<Card>(size);
    }

    /**
     * <p>
     * getColor.
     * </p>
     * 
     * @param cardColor
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getColor(final String cardColor) {
        final CardList list = new CardList();
        for (final Card c : this) {
            if (cardColor.equals("Multicolor") && (c.getColor().size() > 1)) {
                list.add(c);
            } else if (c.isColor(cardColor) && (c.getColor().size() == 1)) {
                list.add(c);
            }
        }

        return list;
    } // getColor()

    /**
     * <p>
     * getOnly2Colors.
     * </p>
     * 
     * @param clr1
     *            a {@link java.lang.String} object.
     * @param clr2
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getOnly2Colors(final String clr1, final String clr2) {
        final CardList list = new CardList();
        list.addAll(this);

        final CardListFilter clrF = new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                if (c.isColorless()) {
                    return true;
                }
                final ArrayList<CardColor> cClrs = c.getColor();
                for (int i = 0; i < cClrs.size(); i++) {
                    CardColor cc = cClrs.get(i);
                    for (final String s : cc.toStringArray()) {
                        if (!s.equals(clr1) && !s.equals(clr2)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        };

        return list.filter(clrF);
    }

    /**
     * <p>
     * reverse.
     * </p>
     */
    public final void reverse() {
        Collections.reverse(this.list);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object a) {
        if (a instanceof CardList) {
            final CardList b = (CardList) a;
            if (this.list.size() != b.size()) {
                return false;
            }

            for (int i = 0; i < this.list.size(); i++) {
                if (!this.list.get(i).equals(b.get(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (41 * (41 + this.list.size() + this.list.hashCode()));
    }

    // removes one copy of that card
    /**
     * <p>
     * remove.
     * </p>
     * 
     * @param cardName
     *            a {@link java.lang.String} object.
     */
    public final void remove(final String cardName) {
        final CardList find = this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.getName().equals(cardName);
            }
        });

        if (0 < find.size()) {
            this.remove(find.get(0));
        } else {
            throw new RuntimeException("CardList : remove(String cardname), error - card name not found: " + cardName
                    + " - contents of Arraylist:" + this.list);
        }

    } // remove(String cardName)

    /**
     * <p>
     * size.
     * </p>
     * 
     * @return a int.
     */
    public final int size() {
        return this.list.size();
    }

    /**
     * <p>
     * add.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    public final void add(final Card c) {
        this.list.add(c);
    }

    /**
     * <p>
     * add.
     * </p>
     * 
     * @param n
     *            a int.
     * @param c
     *            a {@link forge.Card} object.
     */
    public final void add(final int n, final Card c) {
        this.list.add(n, c);
    }

    /**
     * addAll(CardList) - lets you add one CardList to another directly.
     * 
     * @param in
     *            - CardList to add to the current CardList
     */
    public final void addAll(final Iterable<Card> in) {
        for (final Card element : in) {
            this.list.add(element);
        }
    }

    /**
     * <p>
     * addAll.
     * </p>
     * 
     * @param c
     *            an array of {@link java.lang.Object} objects.
     */
    public final void addAll(final Card[] c) {
        for (final Object element : c) {
            this.list.add((Card) element);
        }
    }

    /**
     * <p>
     * contains.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public final boolean contains(final Card c) {
        return this.list.contains(c);
    }

    // probably remove getCard() in the future
    /**
     * <p>
     * getCard.
     * </p>
     * 
     * @param index
     *            a int.
     * @return a {@link forge.Card} object.
     */
    public final Card getCard(final int index) {
        return this.list.get(index);
    }

    /**
     * <p>
     * get.
     * </p>
     * 
     * @param i
     *            a int.
     * @return a {@link forge.Card} object.
     */
    public final Card get(final int i) {
        return this.getCard(i);
    }

    /**
     * <p>
     * containsName.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @return a boolean.
     */
    public final boolean containsName(final Card c) {
        return this.containsName(c.getName());
    }

    /**
     * <p>
     * containsName.
     * </p>
     * 
     * @param name
     *            a {@link java.lang.String} object.
     * @return a boolean.
     */
    public final boolean containsName(final String name) {
        for (int i = 0; i < this.size(); i++) {
            if (this.getCard(i).getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    // returns new subset of all the cards with the same name
    /**
     * <p>
     * getName.
     * </p>
     * 
     * @param name
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getName(final String name) {
        final CardList c = new CardList();

        for (int i = 0; i < this.size(); i++) {
            if (this.getCard(i).getName().equals(name)) {
                c.add(this.getCard(i));
            }
        }

        return c;
    }

    // returns new subset of all the cards that have a different name
    /**
     * <p>
     * getNotName.
     * </p>
     * 
     * @param name
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getNotName(final String name) {
        final CardList c = new CardList();

        for (int i = 0; i < this.size(); i++) {
            if (!this.getCard(i).getName().equals(name)) {
                c.add(this.getCard(i));
            }
        }

        return c;
    }

    /**
     * <p>
     * getImageName.
     * </p>
     * 
     * @param name
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getImageName(final String name) {
        final CardList c = new CardList();

        for (int i = 0; i < this.size(); i++) {
            if (this.getCard(i).getImageName().equals(name)) {
                c.add(this.getCard(i));
            }
        }

        return c;
    }

    /**
     * <p>
     * getController.
     * </p>
     * 
     * @param player
     *            a {@link forge.Player} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getController(final Player player) {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.getController().isPlayer(player);
            }
        });
    }

    /**
     * <p>
     * getOwner.
     * </p>
     * 
     * @param player
     *            a {@link forge.Player} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getOwner(final Player player) {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.getOwner().isPlayer(player);
            }
        });
    }

    // cardType is like "Land" or "Goblin", returns a new CardList that is a
    // subset of current CardList
    /**
     * <p>
     * getType.
     * </p>
     * 
     * @param cardType
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getType(final String cardType) {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.isType(cardType);
            }
        });
    }

    // cardType is like "Land" or "Goblin", returns a new CardList with cards
    // that do not have this type
    /**
     * <p>
     * getNotType.
     * </p>
     * 
     * @param cardType
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getNotType(final String cardType) {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return !c.isType(cardType);
            }
        });
    }

    /**
     * <p>
     * getPermanents.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    public final CardList getPermanents() {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.isPermanent();
            }
        });
    }

    /**
     * <p>
     * getKeyword.
     * </p>
     * 
     * @param keyword
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getKeyword(final String keyword) {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.hasKeyword(keyword);
            }
        });
    }

    /**
     * <p>
     * getNotKeyword.
     * </p>
     * 
     * @param keyword
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getNotKeyword(final String keyword) {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return !c.hasKeyword(keyword);
            }
        });
    }

    // get all cards that have this string in their keywords
    /**
     * <p>
     * getKeywordsContain.
     * </p>
     * 
     * @param keyword
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getKeywordsContain(final String keyword) {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.keywordsContain(keyword);
            }
        });
    }

    // get all cards that don't have this string in their keywords
    /**
     * <p>
     * getKeywordsDontContain.
     * </p>
     * 
     * @param keyword
     *            a {@link java.lang.String} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getKeywordsDontContain(final String keyword) {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return !c.keywordsContain(keyword);
            }
        });
    }

    /**
     * <p>
     * getTokens.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    public final CardList getTokens() {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.isToken();
            }
        });
    }

    /**
     * Create a new list of cards by applying a filter to this one.
     * 
     * @param filt
     *            determines which cards are present in the resulting list
     * 
     * @return a subset of this CardList whose items meet the filtering
     *         criteria; may be empty, but never null.
     */
    public final CardList filter(final CardListFilter filt) {
        return CardFilter.filter(this, filt);
    }

    /**
     * <p>
     * toArray.
     * </p>
     * 
     * @return an array of {@link forge.Card} objects.
     */
    public final Card[] toArray() {
        final Card[] c = new Card[this.list.size()];
        this.list.toArray(c);
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return this.list.toString();
    }

    /**
     * <p>
     * isEmpty.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isEmpty() {
        return this.list.isEmpty();
    }

    /**
     * <p>
     * remove.
     * </p>
     * 
     * @param i
     *            a int.
     * @return a {@link forge.Card} object.
     */
    public final Card remove(final int i) {
        return this.list.remove(i);
    }

    /**
     * <p>
     * remove.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    public final void remove(final Card c) {
        this.list.remove(c);
    }

    /**
     * <p>
     * removeAll.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     */
    public final void removeAll(final Card c) {
        final ArrayList<Card> cList = new ArrayList<Card>();
        cList.add(c);
        this.list.removeAll(cList);
    }

    /**
     * <p>
     * clear.
     * </p>
     */
    public final void clear() {
        this.list.clear();
    }

    /**
     * <p>
     * shuffle.
     * </p>
     */
    public final void shuffle() {
        // reseed Random each time we want to Shuffle
        // MyRandom.random = MyRandom.random;
        Collections.shuffle(this.list, MyRandom.getRandom());
        Collections.shuffle(this.list, MyRandom.getRandom());
        Collections.shuffle(this.list, MyRandom.getRandom());
    }

    /**
     * <p>
     * sort.
     * </p>
     * 
     * @param c
     *            a {@link java.util.Comparator} object.
     */
    public final void sort(final Comparator<Card> c) {
        Collections.sort(this.list, c);
    }

    /**
     * <p>
     * getTargetableCards.
     * </p>
     * 
     * @param source
     *            a {@link forge.Card} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getTargetableCards(final SpellAbility source) {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return c.canBeTargetedBy(source);
            }
        });
    }

    /**
     * <p>
     * getUnprotectedCards.
     * </p>
     * 
     * @param source
     *            a {@link forge.Card} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getUnprotectedCards(final Card source) {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return !c.hasProtectionFrom(source);
            }
        });
    }

    /**
     * <p>
     * getValidCards.
     * </p>
     * 
     * @param restrictions
     *            a {@link java.lang.String} object.
     * @param sourceController
     *            a {@link forge.Player} object.
     * @param source
     *            a {@link forge.Card} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getValidCards(final String restrictions, final Player sourceController, final Card source) {
        return this.getValidCards(restrictions.split(","), sourceController, source);
    }

    /**
     * <p>
     * getValidCards.
     * </p>
     * 
     * @param restrictions
     *            a {@link java.lang.String} object.
     * @param sourceController
     *            a {@link forge.Player} object.
     * @param source
     *            a {@link forge.Card} object.
     * @return a {@link forge.CardList} object.
     */
    public final CardList getValidCards(final String[] restrictions, final Player sourceController, final Card source) {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return (c != null) && c.isValid(restrictions, sourceController, source);
            }
        });
    }

    /**
     * <p>
     * getEquipMagnets.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    public final CardList getEquipMagnets() {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return (c.isCreature() && (c.getSVar("EquipMe").equals("Multiple") || (c.getSVar("EquipMe").equals(
                        "Once") && !c.isEquipped())));
            }
        });
    }

    /**
     * <p>
     * getEnchantMagnets.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    public final CardList getEnchantMagnets() {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return (c.isCreature() && (c.getSVar("EnchantMe").equals("Multiple") || (c.getSVar("EnchantMe").equals(
                        "Once") && !c.isEnchanted())));
            }
        });
    }

    /**
     * <p>
     * getTotalConvertedManaCost.
     * </p>
     * 
     * @return a int.
     */
    public final int getTotalConvertedManaCost() {
        int total = 0;
        for (int i = 0; i < this.size(); i++) {
            total += this.get(i).getCMC();
        }
        return total;
    }

    /**
     * 
     * <p>
     * getTotalCreaturePower.
     * </p>
     * 
     * @return a int.
     */

    public final int getTotalCreaturePower() {
        int total = 0;
        for (int i = 0; i < this.size(); i++) {
            total += this.get(i).getCurrentPower();
        }
        return total;
    }

    /**
     * <p>
     * getHighestConvertedManaCost.
     * </p>
     * 
     * @return a int.
     * @since 1.0.15
     */
    public final int getHighestConvertedManaCost() {
        int total = 0;
        for (int i = 0; i < this.size(); i++) {
            total = Math.max(total, this.get(i).getCMC());
        }
        return total;
    }

    /**
     * <p>
     * getColored.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    public final CardList getColored() {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return (!c.isColorless());
            }
        });
    }

    /**
     * <p>
     * getMonoColored.
     * </p>
     * 
     * @return a {@link forge.CardList} object.
     */
    public final CardList getMonoColored() {
        return this.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                return (CardUtil.getColors(c).size() == 1 && !c.isColorless());
            }
        });
    }

    /**
     * getAbove.
     * 
     * @param source
     *            a Card object
     * @param compared
     *            a Card object
     * @return a boolean
     */
    public final boolean getAbove(final Card source, final Card compared) {
        if (source.equals(compared)) {
            return false;
        }

        for (final Card itr : this) {
            if (itr.equals(source)) {
                return true;
            } else if (itr.equals(compared)) {
                return false;
            }
        }
        return false;
    }

    /**
     * getDirectlyAbove.
     * 
     * @param source
     *            a Card object
     * @param compared
     *            a Card object
     * @return a boolean
     */
    public final boolean getDirectlyAbove(final Card source, final Card compared) {
        if (source.equals(compared)) {
            return false;
        }

        boolean checkNext = false;
        for (final Card itr : this) {
            if (checkNext) {
                if (itr.equals(compared)) {
                    return true;
                }
                return false;
            } else if (itr.equals(source)) {
                checkNext = true;
            } else if (itr.equals(compared)) {
                return false;
            }
        }
        return false;
    }

} // end class CardList
