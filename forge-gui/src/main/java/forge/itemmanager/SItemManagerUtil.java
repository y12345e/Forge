package forge.itemmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Predicate;

import forge.assets.FSkinProp;
import forge.assets.IHasSkinProp;
import forge.card.CardRules;
import forge.card.CardRulesPredicates;
import forge.deck.DeckProxy;
import forge.interfaces.IComboBox;
import forge.item.InventoryItem;
import forge.util.ComparableOp;

/**
 * Static methods for working with top-level editor methods,
 * included but not limited to preferences IO, icon generation,
 * and stats analysis.
 *
 * <br><br>
 * <i>(S at beginning of class name denotes a static factory.)</i>
 *
 */
public final class SItemManagerUtil {
    /** An enum to encapsulate metadata for the stats/filter objects. */
    public static enum StatTypes implements IHasSkinProp {
        WHITE      (FSkinProp.IMG_MANA_W,         CardRulesPredicates.Presets.IS_WHITE, "White cards"),
        BLUE       (FSkinProp.IMG_MANA_U,         CardRulesPredicates.Presets.IS_BLUE, "Blue cards"),
        BLACK      (FSkinProp.IMG_MANA_B,         CardRulesPredicates.Presets.IS_BLACK, "Black cards"),
        RED        (FSkinProp.IMG_MANA_R,         CardRulesPredicates.Presets.IS_RED, "Red cards"),
        GREEN      (FSkinProp.IMG_MANA_G,         CardRulesPredicates.Presets.IS_GREEN, "Green cards"),
        COLORLESS  (FSkinProp.IMG_MANA_COLORLESS, CardRulesPredicates.Presets.IS_COLORLESS, "Colorless cards"),
        MULTICOLOR (FSkinProp.IMG_MULTI,          CardRulesPredicates.Presets.IS_MULTICOLOR, "Multicolor cards"),

        PACK_OR_DECK (FSkinProp.IMG_PACK,         null, "Card packs and prebuilt decks"),
        LAND         (FSkinProp.IMG_LAND,         CardRulesPredicates.Presets.IS_LAND, "Lands"),
        ARTIFACT     (FSkinProp.IMG_ARTIFACT,     CardRulesPredicates.Presets.IS_ARTIFACT, "Artifacts"),
        CREATURE     (FSkinProp.IMG_CREATURE,     CardRulesPredicates.Presets.IS_CREATURE, "Creatures"),
        ENCHANTMENT  (FSkinProp.IMG_ENCHANTMENT,  CardRulesPredicates.Presets.IS_ENCHANTMENT, "Enchantments"),
        PLANESWALKER (FSkinProp.IMG_PLANESWALKER, CardRulesPredicates.Presets.IS_PLANESWALKER, "Planeswalkers"),
        INSTANT      (FSkinProp.IMG_INSTANT,      CardRulesPredicates.Presets.IS_INSTANT, "Instants"),
        SORCERY      (FSkinProp.IMG_SORCERY,      CardRulesPredicates.Presets.IS_SORCERY, "Sorceries"),

        CMC_0 (FSkinProp.IMG_MANA_0, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 0), "Cards with CMC 0"),
        CMC_1 (FSkinProp.IMG_MANA_1, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 1), "Cards with CMC 1"),
        CMC_2 (FSkinProp.IMG_MANA_2, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 2), "Cards with CMC 2"),
        CMC_3 (FSkinProp.IMG_MANA_3, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 3), "Cards with CMC 3"),
        CMC_4 (FSkinProp.IMG_MANA_4, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 4), "Cards with CMC 4"),
        CMC_5 (FSkinProp.IMG_MANA_5, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.EQUALS, 5), "Cards with CMC 5"),
        CMC_6 (FSkinProp.IMG_MANA_6, new CardRulesPredicates.LeafNumber(CardRulesPredicates.LeafNumber.CardField.CMC, ComparableOp.GT_OR_EQUAL, 6), "Cards with CMC 6+"),

        DECK_WHITE      (FSkinProp.IMG_MANA_W,         null, "White decks"),
        DECK_BLUE       (FSkinProp.IMG_MANA_U,         null, "Blue decks"),
        DECK_BLACK      (FSkinProp.IMG_MANA_B,         null, "Black decks"),
        DECK_RED        (FSkinProp.IMG_MANA_R,         null, "Red decks"),
        DECK_GREEN      (FSkinProp.IMG_MANA_G,         null, "Green decks"),
        DECK_COLORLESS  (FSkinProp.IMG_MANA_COLORLESS, null, "Colorless decks"),
        DECK_MULTICOLOR (FSkinProp.IMG_MULTI,          null, "Multicolor decks");

        public final FSkinProp skinProp;
        public final Predicate<CardRules> predicate;
        public final String label;

        private StatTypes(final FSkinProp skinProp0, final Predicate<CardRules> predicate0, final String label0) {
            skinProp = skinProp0;
            predicate = predicate0;
            label = label0;
        }

        @Override
        public FSkinProp getSkinProp() {
            return skinProp;
        }
    }

    public static String getItemDisplayString(final InventoryItem item, final int qty, final boolean forTitle) {
        final List<InventoryItem> items = new ArrayList<InventoryItem>();
        items.add(item);
        return getItemDisplayString(items, qty, forTitle);
    }
    public static String getItemDisplayString(final Iterable<? extends InventoryItem> items, final int qty, final boolean forTitle) {
        //determine shared type among items
        int itemCount = 0;
        String sharedType = null;
        boolean checkForSharedType = true;

        for (final InventoryItem item : items) {
            if (checkForSharedType) {
                if (sharedType == null) {
                    sharedType = item.getItemType();
                }
                else if (!item.getItemType().equals(sharedType)) {
                    sharedType = null;
                    checkForSharedType = false;
                }
            }
            itemCount++;
        }
        if (sharedType == null) {
            sharedType = "Item"; //if no shared type, use generic "item"
        }

        //build display string based on shared type, item count, and quantity of each item
        String result;
        if (forTitle) { //convert to lowercase if not for title
            result = sharedType;
            if (itemCount != 1 || qty != 1) {
                result += "s";
            }
        }
        else {
            result = sharedType.toLowerCase();
            if (itemCount != 1) {
                result = itemCount + " " + result + "s";
            }
            if (qty < 0) { //treat negative numbers as unknown quantity
                result = "X copies of " + result;
            }
            else if (qty != 1) {
                result = qty + " copies of " + result;
            }
        }
        return result;
    }

    public static String buildDisplayList(final Iterable<Entry<InventoryItem, Integer>> items) {
        final List<Entry<InventoryItem, Integer>> sorted = new ArrayList<Entry<InventoryItem, Integer>>();
        for (final Entry<InventoryItem, Integer> itemEntry : items) {
            sorted.add(itemEntry);
        }
        Collections.sort(sorted, new Comparator<Entry<InventoryItem, Integer>>() {
            @Override
            public int compare(final Entry<InventoryItem, Integer> x, final Entry<InventoryItem, Integer> y) {
                return x.getKey().toString().compareTo(y.getKey().toString());
            }
        });
        final StringBuilder builder = new StringBuilder();
        for (final Entry<InventoryItem, Integer> itemEntry : sorted) {
            builder.append("\n" + itemEntry.getValue() + " * " + itemEntry.getKey().toString());
        }
        return builder.toString();
    }

    private static final GroupDef[] CARD_GROUPBY_OPTIONS = { GroupDef.DEFAULT, GroupDef.CARD_TYPE, GroupDef.COLOR, GroupDef.COLOR_IDENTITY, GroupDef.SET, GroupDef.CARD_RARITY };
    private static final GroupDef[] DECK_GROUPBY_OPTIONS = { GroupDef.COLOR, GroupDef.COLOR_IDENTITY, GroupDef.SET };
    private static final ColumnDef[] CARD_PILEBY_OPTIONS = { ColumnDef.CMC, ColumnDef.COLOR, ColumnDef.NAME, ColumnDef.COST, ColumnDef.TYPE, ColumnDef.RARITY, ColumnDef.SET };
    private static final ColumnDef[] DECK_PILEBY_OPTIONS = { ColumnDef.DECK_COLOR, ColumnDef.DECK_FOLDER, ColumnDef.NAME, ColumnDef.DECK_FORMAT, ColumnDef.DECK_EDITION };

    public static void populateImageViewOptions(final IItemManager<?> itemManager, final IComboBox<Object> cbGroupByOptions, final IComboBox<Object> cbPileByOptions) {
        final boolean isDeckManager = itemManager.getGenericType().equals(DeckProxy.class);
        final GroupDef[] groupByOptions = isDeckManager ? DECK_GROUPBY_OPTIONS : CARD_GROUPBY_OPTIONS;
        final ColumnDef[] pileByOptions = isDeckManager ? DECK_PILEBY_OPTIONS : CARD_PILEBY_OPTIONS;
        cbGroupByOptions.addItem("(none)");
        cbPileByOptions.addItem("(none)");
        for (final GroupDef option : groupByOptions) {
            cbGroupByOptions.addItem(option);
        }
        for (final ColumnDef option : pileByOptions) {
            cbPileByOptions.addItem(option);
        }
        cbGroupByOptions.setSelectedIndex(0);
        cbPileByOptions.setSelectedIndex(0);
    }
}
