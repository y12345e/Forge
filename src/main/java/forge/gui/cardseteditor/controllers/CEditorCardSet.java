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
package forge.gui.cardseteditor.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;

import forge.Command;
import forge.Singletons;
import forge.card.CardDb;
import forge.card.CardRulesPredicates;
import forge.cardset.CardSet;
import forge.cardset.io.CardSetSerializer;
import forge.gui.cardseteditor.SEditorIO;
import forge.gui.cardseteditor.SEditorIO.EditorPreference;
import forge.gui.cardseteditor.SEditorUtil;
import forge.gui.cardseteditor.tables.CardSetController;
import forge.gui.cardseteditor.tables.EditorTableView;
import forge.gui.cardseteditor.tables.SColumnUtil;
import forge.gui.cardseteditor.tables.SColumnUtil.ColumnName;
import forge.gui.cardseteditor.tables.TableColumnInfo;
import forge.gui.cardseteditor.views.VCardSetCatalog;
import forge.gui.cardseteditor.views.VCurrentCard;
import forge.gui.cardseteditor.views.VCurrentSet;
import forge.gui.framework.EDocID;
import forge.gui.toolbox.FLabel;
import forge.item.PaperCard;
import forge.item.InventoryItem;
import forge.item.ItemPool;
import forge.item.ItemPoolView;
import forge.properties.NewConstants;
import forge.properties.ForgePreferences.FPref;
import forge.util.storage.StorageImmediatelySerialized;

/**
 * Child controller for constructed cardset editor UI.
 * This is the least restrictive mode;
 * all cards are available.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 * 
 * @author Forge
 * @version $Id: CEditorConstructed.java 21977 2013-06-05 15:34:26Z Max mtg $
 */
public final class CEditorCardSet extends ACEditorBase<PaperCard, CardSet> {
    private final CardSetController<CardSet> controller;
    //private boolean sideboardMode = false;
    
    //=========== Constructor
    /**
     * Child controller for constructed cardset editor UI.
     * This is the least restrictive mode;
     * all cards are available.
     */
    public CEditorCardSet() {
        super();

        boolean wantUnique = SEditorIO.getPref(EditorPreference.display_unique_only);

        final EditorTableView<PaperCard> tblCatalog = new EditorTableView<PaperCard>(wantUnique, PaperCard.class);
        //final EditorTableView<PaperCard> tblCardSet = new EditorTableView<PaperCard>(wantUnique, PaperCard.class);

        VCardSetCatalog.SINGLETON_INSTANCE.setTableView(tblCatalog.getTable());
        //VCurrentCard.SINGLETON_INSTANCE.setTableView(tblCardSet.getTable());

        this.setTableCatalog(tblCatalog);
        //this.setTableCardSet(tblCardSet);

        final Supplier<CardSet> newCreator = new Supplier<CardSet>() {
            @Override
            public CardSet get() {
                return new CardSet();
            }
        };
        
        this.controller = new CardSetController<CardSet>(new StorageImmediatelySerialized<CardSet>(new CardSetSerializer(new File(NewConstants.DECK_CONSTRUCTED_DIR), true)), this, newCreator);
    }

    //=========== Overridden from ACEditorBase

    /* (non-Javadoc)
     * @see forge.gui.cardseteditor.ACEditorBase#addCard()
     */
    @Override
    public void addCard(InventoryItem item, boolean toAlternate, int qty) {
        if ((item == null) || !(item instanceof PaperCard)) {
            return;
        }
        this.controller.notifyModelChanged();
    }

    /* (non-Javadoc)
     * @see forge.gui.cardseteditor.ACEditorBase#removeCard()
     */
    @Override
    public void removeCard(InventoryItem item, boolean toAlternate, int qty) {
        if ((item == null) || !(item instanceof PaperCard)) {
            return;
        }
        this.controller.notifyModelChanged();
    }

    @Override
    public void buildAddContextMenu(ContextMenuBuilder cmb) {
    }
    
    @Override
    public void buildRemoveContextMenu(ContextMenuBuilder cmb) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.cardseteditor.ACEditorBase#updateView()
     */
    @Override
    public void resetTables() {
        // Constructed mode can use all cards, no limitations.
        this.getTableCatalog().setCardSet(ItemPool.createFrom(CardDb.instance().getAllCards(), PaperCard.class), true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.gui.cardseteditor.ACEditorBase#getController()
     */
    @Override
    public CardSetController<CardSet> getCardSetController() {
        return this.controller;
    }

    /* (non-Javadoc)
     * @see forge.gui.cardseteditor.ACEditorBase#show(forge.Command)
     */
    @SuppressWarnings("serial")
    @Override
    public void init() {
        final List<TableColumnInfo<InventoryItem>> lstCatalogCols = SColumnUtil.getCatalogDefaultColumns();
        lstCatalogCols.remove(SColumnUtil.getColumn(ColumnName.CAT_QUANTITY));

        this.getTableCatalog().setup(VCardSetCatalog.SINGLETON_INSTANCE, lstCatalogCols);
        //this.getTableCardSet().setup(VCurrentCard.SINGLETON_INSTANCE, SColumnUtil.getCardSetDefaultColumns());

        SEditorUtil.resetUI();

        this.controller.newModel();
    }

    /* (non-Javadoc)
     * @see forge.gui.cardseteditor.controllers.ACEditorBase#exit()
     */
    @Override
    public boolean exit() {
        // Override the submenu save choice - tell it to go to "constructed".
        Singletons.getModel().getPreferences().setPref(FPref.SUBMENU_CURRENTMENU, EDocID.HOME_CONSTRUCTED.toString());

        return SEditorIO.confirmSaveChanges();
    }
}
