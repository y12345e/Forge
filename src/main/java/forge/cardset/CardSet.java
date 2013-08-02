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
package forge.cardset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Function;

import forge.deck.io.DeckFileHeader;
import forge.deck.io.DeckSerializer;
import forge.util.FileSection;
import forge.util.FileUtil;


/**
 * <p>
 * Deck class.
 * </p>
 * 
 * The set of MTG legal cards that become player's library when the game starts.
 * Any other data is not part of a deck and should be stored elsewhere. Current
 * fields allowed for deck metadata are Name, Title, Description and Deck Type.
 */
@SuppressWarnings("serial")
public class CardSet extends CardSetBase {
    private final Set<String>                tags  = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

    // gameType is from Constant.GameType, like GameType.Regular
    /**
     * <p>
     * Decks have their named finalled.
     * </p>
     */
    public CardSet() {
        this("");
    }

    /**
     * Instantiates a new deck.
     *
     * @param name0 the name0
     */
    public CardSet(final String name0) {
        super(name0);
    }


    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getName();
    }

    /* (non-Javadoc)
     * @see forge.deck.DeckBase#cloneFieldsTo(forge.deck.DeckBase)
     */
    @Override
    protected void cloneFieldsTo(final CardSetBase clone) {
        super.cloneFieldsTo(clone);
        /*final CardSet result = (CardSet) clone;*/
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.deck.DeckBase#newInstance(java.lang.String)
     */
    @Override
    protected CardSetBase newInstance(final String name0) {
        return new CardSet(name0);
    }

    /**
     * From file.
     *
     * @param deckFile the deck file
     * @return the deck
     */
    public static CardSet fromFile(final File deckFile) {
        return CardSet.fromSections(FileSection.parseSections(FileUtil.readFile(deckFile)));
    }

    /**
     * From sections.
     *
     * @param sections the sections
     * @return the deck
     */
    public static CardSet fromSections(final Map<String, List<String>> sections) {
        return CardSet.fromSections(sections, false);
    }

    /**
     * From sections.
     *
     * @param sections the sections
     * @param canThrowExtendedErrors the can throw extended errors
     * @return the deck
     */
    public static CardSet fromSections(final Map<String, List<String>> sections, final boolean canThrowExtendedErrors) {
        if ((sections == null) || sections.isEmpty()) {
            return null;
        }

        final DeckFileHeader header = DeckSerializer.readDeckMetadata(sections, canThrowExtendedErrors);
        if (header == null) {
            return null;
        }

        final CardSet cardset = new CardSet(header.getName());
        cardset.setComment(header.getComment());
        cardset.tags.addAll(header.getTags());
        return cardset;
    }

    /**
     * <p>
     * writeDeck.
     * </p>
     *
     * @return the list
     */
    public List<String> save() {

        final List<String> out = new ArrayList<String>();
        out.add(String.format("[metadata]"));

        out.add(String.format("%s=%s", DeckFileHeader.NAME, this.getName().replaceAll("\n", "")));
        // these are optional
        if (this.getComment() != null) {
            out.add(String.format("%s=%s", DeckFileHeader.COMMENT, this.getComment().replaceAll("\n", "")));
        }
        if (!this.getTags().isEmpty()) {
            out.add(String.format("%s=%s", DeckFileHeader.TAGS, StringUtils.join(getTags(), DeckFileHeader.TAGS_SEPARATOR)));
        }
        return out;
    }


    public static final Function<CardSet, String> FN_NAME_SELECTOR = new Function<CardSet, String>() {
        @Override
        public String apply(CardSet arg1) {
            return arg1.getName();
        }
    };

    /**
     * @return the associated tags, a writable set
     */
    public Set<String> getTags() {
        return tags;
    }
}
