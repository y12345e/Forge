package forge.planarconquest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import forge.GuiBase;
import forge.assets.ISkinImage;
import forge.card.ColorSet;
import forge.deck.generation.DeckGenPool;
import forge.item.PaperCard;
import forge.model.FModel;
import forge.util.storage.StorageReaderFile;

public class ConquestRegion {
    public static final int ROWS_PER_REGION = 3;
    public static final int COLS_PER_REGION = 3;
    public static final int START_COL = (COLS_PER_REGION - 1) / 2;

    private final ConquestPlane plane;
    private final String name, artCardName;
    private final ColorSet colorSet;
    private final Predicate<PaperCard> pred;
    private final DeckGenPool cardPool = new DeckGenPool();

    private ISkinImage art;

    private ConquestRegion(ConquestPlane plane0, String name0, String artCardName0, ColorSet colorSet0, Predicate<PaperCard> pred0) {
        plane = plane0;
        name = name0;
        artCardName = artCardName0;
        pred = pred0;
        colorSet = colorSet0;
    }

    public ConquestPlane getPlane() {
        return plane;
    }

    public String getName() {
        return name;
    }

    public ISkinImage getArt() {
        if (art == null) {
            art = GuiBase.getInterface().getCardArt(cardPool.getCard(artCardName));
        }
        return art;
    }

    public ColorSet getColorSet() {
        return colorSet;
    }

    public DeckGenPool getCardPool() {
        return cardPool;
    }

    public String toString() {
        return plane.getName() + " - " + name;
    }

    public static final Function<ConquestRegion, String> FN_GET_NAME = new Function<ConquestRegion, String>() {
        @Override
        public String apply(ConquestRegion region) {
            return region.getName();
        }
    };

    public static class Reader extends StorageReaderFile<ConquestRegion> {
        private final ConquestPlane plane;

        public Reader(ConquestPlane plane0) {
            super(plane0.getDirectory() + "regions.txt", ConquestRegion.FN_GET_NAME);
            plane = plane0;
        }

        @Override
        protected ConquestRegion read(String line, int index) {
            String name = null;
            String artCardName = null;
            ColorSet colorSet = ColorSet.ALL_COLORS;
            Predicate<PaperCard> pred = null;

            String[] pieces = line.trim().split("\\|");
            for (String piece : pieces) {
                String[] kv = piece.split(":", 2);
                String key = kv[0].trim().toLowerCase();
                String value = kv[1].trim();
                switch(key) {
                case "name":
                    name = value;
                    break;
                case "art":
                    artCardName = value;
                    break;
                case "colors":
                    colorSet = ColorSet.fromNames(value.toCharArray());
                    final int colorMask = colorSet.getColor();
                    pred = new Predicate<PaperCard>() {
                        @Override
                        public boolean apply(PaperCard pc) {
                            return pc.getRules().getColorIdentity().hasNoColorsExcept(colorMask);
                        }
                    };
                    break;
                case "sets":
                    final String[] sets = value.split(",");
                    for (int i = 0; i < sets.length; i++) {
                        sets[i] = sets[i].trim();
                    }
                    pred = new Predicate<PaperCard>() {
                        @Override
                        public boolean apply(PaperCard pc) {
                            for (String set : sets) {
                                if (pc.getEdition().equals(set)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    };
                    break;
                }
            }
            return new ConquestRegion(plane, name, artCardName, colorSet, pred);
        }
    }

    static void addCard(PaperCard pc, Iterable<ConquestRegion> regions) {
        boolean foundRegion = false;
        for (ConquestRegion region : regions) {
            if (region.pred.apply(pc)) {
                region.cardPool.add(pc);
                foundRegion = true;
            }
        }

        if (foundRegion) { return; }

        //if card doesn't match any region's predicate, make card available to all regions
        for (ConquestRegion region : regions) {
            region.cardPool.add(pc);
        }
    }

    public static Set<ConquestRegion> getAllRegionsOfCard(PaperCard card) {
        Set<ConquestRegion> regions = new HashSet<ConquestRegion>();
        for (ConquestPlane plane : FModel.getPlanes()) {
            if (plane.getCardPool().contains(card)) {
                for (ConquestRegion region : plane.getRegions()) {
                    if (region.getCardPool().contains(card)) {
                        regions.add(region);
                    }
                }
            }
        }
        return regions;
    }

    public static List<ConquestRegion> getAllRegions() {
        List<ConquestRegion> regions = new ArrayList<ConquestRegion>();
        for (ConquestPlane plane : FModel.getPlanes()) {
            for (ConquestRegion region : plane.getRegions()) {
                regions.add(region);
            }
        }
        return regions;
    }
}