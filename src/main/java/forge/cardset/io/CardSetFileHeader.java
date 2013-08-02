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
package forge.cardset.io;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import forge.deck.DeckFormat;
import forge.game.player.PlayerType;
import forge.util.FileSection;

/**
 * TODO: Write javadoc for this type.
 * 
 */
public class CardSetFileHeader {

    /** The Constant NAME. */
    public static final String NAME = "Name";

    /** The Constant DECK_TYPE. */
    public static final String DECK_TYPE = "Deck Type";
    public static final String TAGS = "Tags";
    
    public static final String TAGS_SEPARATOR = ",";
    
    /** The Constant COMMENT. */
    public static final String COMMENT = "Comment";
    private static final String PLAYER = "Player";
    private static final String CSTM_POOL = "Custom Pool";
    private static final String PLAYER_TYPE = "PlayerType";

    private final DeckFormat deckType;
    private final PlayerType playerType;
    private final boolean customPool;

    private final String name;
    private final String comment;

    private final Set<String> tags;

    /**
     * TODO: Write javadoc for Constructor.
     * 
     * @param kvPairs
     *            the kv pairs
     */
    public CardSetFileHeader(final FileSection kvPairs) {
        this.name = kvPairs.get(CardSetFileHeader.NAME);
        this.comment = kvPairs.get(CardSetFileHeader.COMMENT);
        this.deckType = DeckFormat.smartValueOf(kvPairs.get(CardSetFileHeader.DECK_TYPE), DeckFormat.Constructed);
        this.customPool = kvPairs.getBoolean(CardSetFileHeader.CSTM_POOL);
        boolean isForAi = "computer".equalsIgnoreCase(kvPairs.get(CardSetFileHeader.PLAYER)) || "ai".equalsIgnoreCase(kvPairs.get(CardSetFileHeader.PLAYER_TYPE));
        this.playerType = isForAi ? PlayerType.COMPUTER : PlayerType.HUMAN;
        this.tags = new TreeSet<String>();
        
        String rawTags = kvPairs.get(CardSetFileHeader.TAGS);
        if( StringUtils.isNotBlank(rawTags) ) {
            for( String t: rawTags.split(TAGS_SEPARATOR))
                if ( StringUtils.isNotBlank(t))
                    tags.add(t.trim());
        }
        
            
    }

    /**
     * Gets the player type.
     * 
     * @return the player type
     */
    public final PlayerType getPlayerType() {
        return this.playerType;
    }

    /**
     * Checks if is custom pool.
     * 
     * @return true, if is custom pool
     */
    public final boolean isCustomPool() {
        return this.customPool;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Gets the comment.
     * 
     * @return the comment
     */
    public final String getComment() {
        return this.comment;
    }

    /**
     * Gets the deck type.
     * 
     * @return the deck type
     */
    public final DeckFormat getDeckType() {
        return this.deckType;
    }

    public final Set<String> getTags() {
        return tags;
    }

}
