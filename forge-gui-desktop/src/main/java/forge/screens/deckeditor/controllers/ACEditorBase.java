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
package forge.screens.deckeditor.controllers;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import forge.UiCommand;
import forge.assets.FSkinProp;
import forge.deck.Deck;
import forge.deck.DeckBase;
import forge.deck.DeckSection;
import forge.gui.GuiChoose;
import forge.gui.GuiUtils;
import forge.gui.framework.DragCell;
import forge.gui.framework.FScreen;
import forge.gui.framework.ICDoc;
import forge.gui.framework.IVDoc;
import forge.gui.framework.SRearrangingUtil;
import forge.item.InventoryItem;
import forge.item.PaperCard;
import forge.itemmanager.ItemManager;
import forge.itemmanager.SItemManagerUtil;
import forge.menus.IMenuProvider;
import forge.model.FModel;
import forge.properties.ForgePreferences.FPref;
import forge.screens.deckeditor.CDeckEditorUI;
import forge.screens.deckeditor.menus.CDeckEditorUIMenus;
import forge.screens.deckeditor.views.VCardCatalog;
import forge.screens.deckeditor.views.VCurrentDeck;
import forge.screens.match.controllers.CDetailPicture;
import forge.toolbox.ContextMenuBuilder;
import forge.toolbox.FLabel;
import forge.toolbox.FSkin;
import forge.util.ItemPool;
import forge.view.FView;

/**
 * Maintains a generically typed architecture for various editing
 * environments.  A basic editor instance requires a card catalog, the
 * current deck being edited, and optional filters on the catalog.
 * <br><br>
 * These requirements are collected in this class and manipulated
 * in subclasses for different environments. There are two generic
 * types for all card display and filter predicates.
 *
 * <br><br><i>(A at beginning of class name denotes an abstract class.)</i>
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 * @param <TItem> extends {@link forge.item.InventoryItem}
 * @param <TModel> extends {@link forge.deck.DeckBase}
 */
public abstract class ACEditorBase<TItem extends InventoryItem, TModel extends DeckBase> implements IMenuProvider {
    public boolean listenersHooked;
    private final FScreen screen;
    private ItemManager<TItem> catalogManager;
    private ItemManager<TItem> deckManager;
    protected DeckSection sectionMode = DeckSection.Main;
    private final CDetailPicture cDetailPicture;

    // card transfer buttons
    private final FLabel btnAdd = new FLabel.Builder()
            .fontSize(14)
            .text("Add card")
            .tooltip("Add selected card to current deck (or double click the row or hit the spacebar)")
            .icon(FSkin.getIcon(FSkinProp.ICO_PLUS))
            .iconScaleAuto(false).hoverable().build();
    private final FLabel btnAdd4 = new FLabel.Builder()
            .fontSize(14)
            .text("Add 4 of card")
            .tooltip("Add up to 4 of selected card to current deck")
            .icon(FSkin.getIcon(FSkinProp.ICO_PLUS))
            .iconScaleAuto(false).hoverable().build();

    private final FLabel btnRemove = new FLabel.Builder()
            .fontSize(14)
            .text("Remove card")
            .tooltip("Remove selected card from current deck (or double click the row or hit the spacebar)")
            .icon(FSkin.getIcon(FSkinProp.ICO_MINUS))
            .iconScaleAuto(false).hoverable().build();

    private final FLabel btnRemove4 = new FLabel.Builder()
            .fontSize(14)
            .text("Remove 4 of card")
            .tooltip("Remove up to 4 of selected card to current deck")
            .icon(FSkin.getIcon(FSkinProp.ICO_MINUS))
            .iconScaleAuto(false).hoverable().build();

    private final FLabel btnAddBasicLands = new FLabel.Builder()
            .fontSize(14)
            .text("Add Basic Lands")
            .tooltip("Add basic lands to the deck")
            .icon(FSkin.getImage(FSkinProp.IMG_LAND, 18, 18))
            .iconScaleAuto(false).hoverable().build();

    protected ACEditorBase(final FScreen screen0, final CDetailPicture cDetailPicture) {
        this.screen = screen0;
        this.cDetailPicture = cDetailPicture;
    }

    public FScreen getScreen() {
        return this.screen;
    }

    public DeckSection getSectionMode() {
        return this.sectionMode;
    }

    protected final CDetailPicture getCDetailPicture() {
        return cDetailPicture;
    }

    /* (non-Javadoc)
     * @see forge.gui.menubar.IMenuProvider#getMenus()
     */
    @Override
    public List<JMenu> getMenus() {
        if (this.getDeckController() == null) {
            return null;
        }
        return new CDeckEditorUIMenus().getMenus();
    }

    public final void addItem(final TItem item) {
        onAddItems(createPoolForItem(item, 1), false);
    }
    public final void addItem(final TItem item, final int qty) {
        onAddItems(createPoolForItem(item, qty), false);
    }

    public final void removeItem(final TItem item) {
        onRemoveItems(createPoolForItem(item, 1), false);
    }
    public final void removeItem(final TItem item, final int qty) {
        onRemoveItems(createPoolForItem(item, qty), false);
    }

    @SuppressWarnings("unchecked")
    private ItemPool<TItem> createPoolForItem(final TItem item, final int qty) {
        if (item == null || qty <= 0) { return null; }

        final ItemPool<TItem> pool = new ItemPool<TItem>((Class<TItem>)item.getClass());
        pool.add(item, qty);
        return pool;
    }

    public final void addItems(final Iterable<Entry<TItem, Integer>> items, final boolean toAlternate) {
        if (items == null || !items.iterator().hasNext()) { return; } //do nothing if no items
        onAddItems(items, toAlternate);
    }

    public final void removeItems(final Iterable<Entry<TItem, Integer>> items, final boolean toAlternate) {
        if (items == null || !items.iterator().hasNext()) { return; } //do nothing if no items
        onRemoveItems(items, toAlternate);
    }

    public enum CardLimit {
        Singleton,
        Default,
        None
    }

    private static final List<String> limitExceptions = Arrays.asList(
            new String[]{"Relentless Rats", "Shadowborn Apostle"});

    /**
     * @return pool of additions allowed to deck
     */
    protected ItemPool<TItem> getAllowedAdditions(final Iterable<Entry<TItem, Integer>> itemsToAdd) {
        final ItemPool<TItem> additions = new ItemPool<TItem>(getCatalogManager().getGenericType());
        final CardLimit limit = getCardLimit();
        final DeckController<TModel> controller = getDeckController();
        final Deck deck = controller != null && controller.getModel() instanceof Deck ? (Deck)controller.getModel() : null;

        for (final Entry<TItem, Integer> itemEntry : itemsToAdd) {
            final TItem item = itemEntry.getKey();
            final PaperCard card = item instanceof PaperCard ? (PaperCard)item : null;
            int qty = itemEntry.getValue();

            int max;
            if (deck == null || card == null || card.getRules().getType().isBasic() ||
                    limit == CardLimit.None || limitExceptions.contains(card.getName())) {
                max = Integer.MAX_VALUE;
            }
            else {
                max = (limit == CardLimit.Singleton ? 1 : FModel.getPreferences().getPrefInt(FPref.DECK_DEFAULT_CARD_LIMIT));
                max -= deck.getMain().count(card);
                if (deck.has(DeckSection.Sideboard)) {
                    max -= deck.get(DeckSection.Sideboard).count(card);
                }
                if (deck.has(DeckSection.Commander)) {
                    max -= deck.get(DeckSection.Commander).count(card);
                }
            }
            if (qty > max) {
                qty = max;
            }
            if (qty > 0) {
                additions.add(item, qty);
            }
        }

        return additions;
    }

    protected abstract CardLimit getCardLimit();

    /**
     * Operation to add selected items to current deck.
     */
    protected abstract void onAddItems(Iterable<Entry<TItem, Integer>> items, boolean toAlternate);

    /**
     * Operation to remove selected item from current deck.
     */
    protected abstract void onRemoveItems(Iterable<Entry<TItem, Integer>> items, boolean toAlternate);

    protected abstract void buildAddContextMenu(EditorContextMenuBuilder cmb);
    protected abstract void buildRemoveContextMenu(EditorContextMenuBuilder cmb);

    /**
     * Resets the cards in the catalog table and current deck table.
     */
    public abstract void resetTables();

    /**
     * Gets controller responsible for the current deck being edited.
     *
     * @return {@link forge.screens.deckeditor.controllers.DeckController}
     */
    public abstract DeckController<TModel> getDeckController();

    /**
     * Called when switching away from or closing the editor wants to exit. Should confirm save options.
     *
     * @return boolean &emsp; true if safe to exit
     */
    public abstract boolean canSwitchAway(boolean isClosing);

    /**
     * Resets and initializes the current editor.
     */
    public abstract void update();

    /**
     * Reset UI changes made in update
     */
    public abstract void resetUIChanges();

    /**
     * Gets the ItemManager holding the cards in the current deck.
     *
     * @return {@link forge.itemmanager.ItemManager}
     */
    public ItemManager<TItem> getDeckManager() {
        return this.deckManager;
    }

    /**
     * Sets the ItemManager holding the cards in the current deck.
     *
     * @param itemManager &emsp; {@link forge.itemmanager.ItemManager}
     */
    @SuppressWarnings("serial")
    public void setDeckManager(final ItemManager<TItem> itemManager) {
        this.deckManager = itemManager;

        btnRemove.setCommand(new UiCommand() {
            @Override public void run() {
                CDeckEditorUI.SINGLETON_INSTANCE.removeSelectedCards(false, 1);
            }
        });
        btnRemove4.setCommand(new UiCommand() {
            @Override public void run() {
                CDeckEditorUI.SINGLETON_INSTANCE.removeSelectedCards(false, 4);
            }
        });
        btnAddBasicLands.setCommand(new UiCommand() {
            @Override public void run() {
                
            }
        });
        itemManager.getPnlButtons().add(btnRemove, "w 30%!, h 30px!, gapx 5");
        itemManager.getPnlButtons().add(btnRemove4, "w 30%!, h 30px!, gapx 5");
        itemManager.getPnlButtons().add(btnAddBasicLands, "w 30%!, h 30px!, gapx 5");
    }

    /**
     * Gets the ItemManager holding the cards in the current catalog.
     *
     * @return {@link forge.itemmanager.ItemManager}
     */
    public ItemManager<TItem> getCatalogManager() {
        return this.catalogManager;
    }

    /**
     * Sets the ItemManager holding the cards in the current catalog.
     *
     * @param itemManager &emsp; {@link forge.itemmanager.ItemManager}
     */
    @SuppressWarnings("serial")
    public void setCatalogManager(final ItemManager<TItem> itemManager) {
        this.catalogManager = itemManager;

        btnAdd.setCommand(new UiCommand() {
            @Override public void run() {
                CDeckEditorUI.SINGLETON_INSTANCE.addSelectedCards(false, 1);
            }
        });
        btnAdd4.setCommand(new UiCommand() {
            @Override public void run() {
                CDeckEditorUI.SINGLETON_INSTANCE.addSelectedCards(false, 4);
            }
        });
        itemManager.getPnlButtons().add(btnAdd, "w 30%!, h 30px!, h 30px!, gapx 5");
        itemManager.getPnlButtons().add(btnAdd4, "w 30%!, h 30px!, h 30px!, gapx 5");
    }

    /**
     * Removes the specified tab and returns its parent for later re-adding
     */
    protected DragCell removeTab (final IVDoc<? extends ICDoc> tab) {
        final DragCell parent;
        if (tab.getParentCell() == null) {
            parent = null;
        } else {
            parent = tab.getParentCell();
            parent.removeDoc(tab);
            tab.setParentCell(null);

            if (parent.getDocs().size() > 0) {
                // if specified tab was first child of its parent, the new first tab needs re-selecting.
                parent.setSelected(parent.getDocs().get(0));
            } else {
                // if the parent is now childless, fill in the resultant gap
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        SRearrangingUtil.fillGap(parent);
                        FView.SINGLETON_INSTANCE.removeDragCell(parent);
                    }
                });
            }
        }

        return parent;
    }

    protected void resetUI() {
        getBtnAdd4().setVisible(true);
        getBtnRemove4().setVisible(true);

        VCurrentDeck.SINGLETON_INSTANCE.getBtnSave().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnSaveAs().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnNew().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnOpen().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getBtnImport().setVisible(true);

        VCurrentDeck.SINGLETON_INSTANCE.getTxfTitle().setEnabled(true);

        VCurrentDeck.SINGLETON_INSTANCE.getPnlHeader().setVisible(true);

        VCardCatalog.SINGLETON_INSTANCE.getTabLabel().setText("Card Catalog");

        VCurrentDeck.SINGLETON_INSTANCE.getBtnPrintProxies().setVisible(true);
        getBtnCycleSection().setVisible(false);

        VCurrentDeck.SINGLETON_INSTANCE.getTxfTitle().setVisible(true);
        VCurrentDeck.SINGLETON_INSTANCE.getLblTitle().setText("Title:");
    }

    public FLabel getBtnAdd()     { return btnAdd; }
    public FLabel getBtnAdd4()    { return btnAdd4; }
    public FLabel getBtnRemove()  { return btnRemove; }
    public FLabel getBtnRemove4() { return btnRemove4; }
    public FLabel getBtnAddBasicLands() { return btnAddBasicLands; }
    public FLabel getBtnCycleSection() { return deckManager.getBtnCycleSection(); }

    public ContextMenuBuilder createContextMenuBuilder(final boolean isAddContextMenu0) {
        return new EditorContextMenuBuilder(isAddContextMenu0);
    }

    protected class EditorContextMenuBuilder implements ContextMenuBuilder {
        private final boolean isAddContextMenu;
        private JPopupMenu menu;

        private EditorContextMenuBuilder(final boolean isAddContextMenu0) {
            isAddContextMenu = isAddContextMenu0;
        }

        private ItemManager<TItem> getItemManager() {
            return isAddContextMenu ? catalogManager : deckManager;
        }

        private ItemManager<TItem> getNextItemManager() {
            return isAddContextMenu ? deckManager : catalogManager;
        }

        @Override
        public void buildContextMenu(final JPopupMenu menu) {
            this.menu = menu; //cache menu while controller populates menu
            if (isAddContextMenu) {
                buildAddContextMenu(this);
            }
            else {
                buildRemoveContextMenu(this);
            }
            this.menu = null;

            if (menu.getComponentCount() > 0) {
                menu.addSeparator();
            }

            GuiUtils.addMenuItem(menu, "Jump to previous table",
                    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
                    new Runnable() {
                @Override
                public void run() {
                    getNextItemManager().focus();
                }
            });
            GuiUtils.addMenuItem(menu, "Jump to next table",
                    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                    new Runnable() {
                @Override
                public void run() {
                    getNextItemManager().focus();
                }
            });
            GuiUtils.addMenuItem(menu, "Jump to text filter",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    new Runnable() {
                @Override
                public void run() {
                    getItemManager().focusSearch();
                }
            });
        }

        private void addItem(final String verb, final String dest, final boolean toAlternate, final int qty, final int shortcutModifiers) {
            String label = verb + " " + SItemManagerUtil.getItemDisplayString(getItemManager().getSelectedItems(), qty, false);
            if (dest != null && !dest.isEmpty()) {
                label += " " + dest;
            }
            GuiUtils.addMenuItem(menu, label,
                    KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, shortcutModifiers), new Runnable() {
                @Override public void run() {
                    Integer quantity = qty;
                    if (quantity < 0) {
                        quantity = GuiChoose.getInteger("Choose a value for X", 1, -quantity, 20);
                        if (quantity == null) { return; }
                    }
                    if (isAddContextMenu) {
                        CDeckEditorUI.SINGLETON_INSTANCE.addSelectedCards(toAlternate, quantity);
                    }
                    else {
                        CDeckEditorUI.SINGLETON_INSTANCE.removeSelectedCards(toAlternate, quantity);
                    }
                }
            }, true, shortcutModifiers == 0);
        }

        private int getMaxMoveQuantity() {
            ItemPool<TItem> selectedItemPool = getItemManager().getSelectedItemPool();
            if (isAddContextMenu) {
                selectedItemPool = getAllowedAdditions(selectedItemPool);
            }
            if (selectedItemPool.isEmpty()) {
                return 0;
            }
            int max = Integer.MAX_VALUE;
            for (final Entry<TItem, Integer> itemEntry : selectedItemPool) {
                if (itemEntry.getValue() < max) {
                    max = itemEntry.getValue();
                }
            }
            return max;
        }

        private void addItems(final String verb, final String dest, final boolean toAlternate, final int shortcutModifiers1, final int shortcutModifiers2, final int shortcutModifiers3) {
            final int max = getMaxMoveQuantity();
            if (max == 0) { return; }

            addItem(verb, dest, toAlternate, 1, shortcutModifiers1);
            if (max == 1) { return; }

            int qty = FModel.getPreferences().getPrefInt(FPref.DECK_DEFAULT_CARD_LIMIT);
            if (qty > max) {
                qty = max;
            }

            addItem(verb, dest, toAlternate, qty, shortcutModifiers2);
            if (max == 2) { return; }

            addItem(verb, dest, toAlternate, -max, shortcutModifiers3); //pass -max as quantity to indicate to prompt for specific quantity
        }

        public void addMoveItems(final String verb, final String dest) {
            addItems(verb, dest, false, 0, InputEvent.SHIFT_DOWN_MASK, InputEvent.ALT_MASK);
        }

        public void addMoveAlternateItems(final String verb, final String dest) {
            if (this.menu.getComponentCount() > 0) {
                this.menu.addSeparator();
            }
            //yes, CTRL_DOWN_MASK and not getMenuShortcutKeyMask().  On OSX, cmd-space is hard-coded to bring up Spotlight
            addItems(verb, dest, true, InputEvent.CTRL_DOWN_MASK,
                    //getMenuShortcutKeyMask() instead of CTRL_DOWN_MASK since on OSX, ctrl-shift-space brings up the window manager
                    InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
                    InputEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        }
    }

}
