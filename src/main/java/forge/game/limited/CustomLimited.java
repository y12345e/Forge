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
package forge.game.limited;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import forge.card.BoosterTemplate;
import forge.card.CardDb;
import forge.card.SealedProductTemplate;
import forge.deck.Deck;
import forge.deck.DeckBase;
import forge.item.PaperCard;
import forge.item.ItemPool;
import forge.item.ItemPoolView;
import forge.util.FileSection;
import forge.util.TextUtil;
import forge.util.storage.IStorageView;

/**
 * <p>
 * CustomDraft class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CustomLimited extends DeckBase {
    private final SealedProductTemplate tpl;
    
    /**
     * TODO: Write javadoc for Constructor.
     *
     * @param name0 the name0
     * @param slots 
     */
    public CustomLimited(final String name0, List<Pair<String, Integer>> slots) {
        super(name0);
        tpl = new SealedProductTemplate(slots);
    }

    private static final long serialVersionUID = 7435640939026612173L;

    /** The Num packs. */
    private int numPacks = 3;

    private transient ItemPoolView<PaperCard> cardPool;

    /** The Land set code. */
    private String landSetCode = CardDb.instance().getCard("Plains", true).getEdition();

    private boolean singleton; 


    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * Parses the.
     *
     * @param dfData the df data
     * @param cubes the cubes
     * @return the custom limited
     */
    public static CustomLimited parse(final List<String> dfData, final IStorageView<Deck> cubes) {

        final FileSection data = FileSection.parse(dfData, ":");

        List<Pair<String, Integer>> slots = new ArrayList<Pair<String,Integer>>();
        String boosterData = data.get("Booster");
        if(StringUtils.isNotEmpty(boosterData)){
            final String[] booster = TextUtil.splitWithParenthesis(boosterData, ',');
            for(String slotDesc : booster) {
                String[] kv = TextUtil.splitWithParenthesis(slotDesc, ' ', 2);
                slots.add(ImmutablePair.of(kv[1], Integer.parseInt(kv[0])));
            }
        } else
            slots = BoosterTemplate.genericBooster.getSlots();

        final CustomLimited cd = new CustomLimited(data.get("Name"), slots);
        cd.landSetCode = data.get("LandSetCode");
        cd.numPacks = data.getInt("NumPacks");
        cd.singleton = data.getBoolean("Singleton");
        final Deck deckCube = cubes.get(data.get("DeckFile"));
        cd.cardPool = deckCube == null ? ItemPool.createFrom(CardDb.instance().getUniqueCards(), PaperCard.class) : deckCube.getMain();

        return cd;
    }



    /**
     * Gets the num packs.
     * 
     * @return the numPacks
     */
    public int getNumPacks() {
        return this.numPacks;
    }

    /**
     * Sets the num packs.
     * 
     * @param numPacksIn
     *            the numPacks to set
     */
    public void setNumPacks(final int numPacksIn) {
        this.numPacks = numPacksIn;
    }

    /**
     * Gets the land set code.
     * 
     * @return the landSetCode
     */
    public String getLandSetCode() {
        return this.landSetCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.item.CardCollectionBase#getCardPool()
     */
    public ItemPoolView<PaperCard> getCardPool() {
        return this.cardPool;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.deck.DeckBase#getInstance(java.lang.String)
     */
    @Override
    protected DeckBase newInstance(final String name0) {
        return new CustomLimited(name0, tpl.getSlots());
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public SealedProductTemplate getSealedProductTemplate() {
        return tpl;
    }

    public boolean isSingleton() {
        return singleton;
    }
}
