package forge.gui.cardseteditor.controllers;

import java.awt.Dialog.ModalityType;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import forge.Command;
import forge.cardset.CardSet;
import forge.cardset.CardSetBase;
import forge.cardset.io.CardSetSerializer;
import forge.error.BugReporter;
import forge.gui.cardseteditor.CCardSetEditorUI;
import forge.gui.cardseteditor.CardSetImport;
import forge.gui.cardseteditor.SEditorIO;
import forge.gui.cardseteditor.tables.CardSetController;
import forge.gui.cardseteditor.views.VCurrentCard;
import forge.gui.framework.ICDoc;
import forge.gui.toolbox.FLabel;
import forge.item.InventoryItem;
import forge.properties.NewConstants;

/** 
 * Controls the "current card" panel in the card editor UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CCurrentCard implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    private static File previousDirectory = null;

    private JFileChooser fileChooser = new JFileChooser(NewConstants.DECK_BASE_DIR);
    
    //========== Overridden methods
    
    private CCurrentCard() {
        FileFilter[] defaultFilters = fileChooser.getChoosableFileFilters();
        for(FileFilter defFilter : defaultFilters)
        {
            fileChooser.removeChoosableFileFilter(defFilter);
        }
        
        fileChooser.addChoosableFileFilter(CardSetSerializer.DCK_FILTER);
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @Override
    @SuppressWarnings("serial")
    public void initialize() {
        ((FLabel) VCurrentCard.SINGLETON_INSTANCE.getBtnSave())
            .setCommand(new Command() { @Override
                public void run() { SEditorIO.saveCard(); } });

        ((FLabel) VCurrentCard.SINGLETON_INSTANCE.getBtnSaveAs())
            .setCommand(new Command() { @Override
                public void run() { exportCardSet(); } });

        ((FLabel) VCurrentCard.SINGLETON_INSTANCE.getBtnPrintProxies())
        .setCommand(new Command() { @Override
            public void run() { printProxies(); } });

        ((FLabel) VCurrentCard.SINGLETON_INSTANCE.getBtnOpen())
            .setCommand(new Command() { @Override
                public void run() { openCardSet(); } });

        ((FLabel) VCurrentCard.SINGLETON_INSTANCE.getBtnNew())
            .setCommand(new Command() { @Override
                public void run() { newCardSet(); } });

        VCurrentCard.SINGLETON_INSTANCE.getTxfTitle().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                if (((JTextField) e.getSource()).getText().equals("[New CardSet]")) {
                    ((JTextField) e.getSource()).setText("");
                }
            }

            @Override
            public void focusLost(final FocusEvent e) {
                if (((JTextField) e.getSource()).getText().isEmpty()) {
                    ((JTextField) e.getSource()).setText("[New CardSet]");
                }
            }
        });

        ((FLabel) VCurrentCard.SINGLETON_INSTANCE.getBtnRemove()).setCommand(new Command() {
            @Override  public void run() {
                CCardSetEditorUI.SINGLETON_INSTANCE.removeSelectedCards(false, 1);
            } });

        ((FLabel) VCurrentCard.SINGLETON_INSTANCE.getBtnRemove4()).setCommand(new Command() {
            @Override  public void run() {
                CCardSetEditorUI.SINGLETON_INSTANCE.removeSelectedCards(false, 4);
            }
        });

        VCurrentCard.SINGLETON_INSTANCE.getBtnImport()
        .setCommand(new Command() { @Override
            public void run() { importCardSet(); } });
    }
    
    /**
     * Opens dialog for importing a card from a different MTG software.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <TItem extends InventoryItem, TModel extends CardSetBase> void importCardSet() {
        final ACEditorBase<TItem, TModel> ed = (ACEditorBase<TItem, TModel>)
                CCardSetEditorUI.SINGLETON_INSTANCE.getCurrentEditorController();

        final CardSetImport dImport = new CardSetImport(ed);
        dImport.setModalityType(ModalityType.APPLICATION_MODAL);
        dImport.setVisible(true);
    }


    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
    }

    //

    /** */
    @SuppressWarnings("unchecked")
    private void newCardSet() {
        if (!SEditorIO.confirmSaveChanges()) { return; }

        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ((CardSetController<CardSetBase>) CCardSetEditorUI.SINGLETON_INSTANCE.getCurrentEditorController().getCardSetController()).newModel();
                    VCurrentCard.SINGLETON_INSTANCE.getTxfTitle().setText("");
                    VCurrentCard.SINGLETON_INSTANCE.getTxfTitle().requestFocusInWindow();
                }
            });
        } catch (final Exception ex) {
            BugReporter.reportException(ex);
            throw new RuntimeException("Error creating new card. " + ex);
        }
    }

    /** */
    @SuppressWarnings("unchecked")
    private void openCardSet() {
        if (!SEditorIO.confirmSaveChanges()) { return; }

        final File file = this.getImportFilename();

        if (file != null) {
            try {
                ((CardSetController<CardSetBase>) CCardSetEditorUI.SINGLETON_INSTANCE
                        .getCurrentEditorController().getCardSetController())
                        .setModel(CardSet.fromFile(file));

            } catch (final Exception ex) {
                BugReporter.reportException(ex);
                throw new RuntimeException("Error importing card." + ex);
            }
        }
    }

    /** */
    private File getImportFilename() {
        fileChooser.setDialogTitle("Import CardSet");
        
        final int returnVal = fileChooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            previousDirectory = file.getParentFile();
            return file;
        }
        return null;
    }

    /** */
    @SuppressWarnings("unchecked")
    private void exportCardSet() {
        final File filename = this.getExportFilename();
        if (filename == null) {
            return;
        }

        try {
            CardSetSerializer.writeCard(
                ((CardSetController<CardSet>) CCardSetEditorUI.SINGLETON_INSTANCE
                .getCurrentEditorController().getCardSetController()).getModel(), filename);
        } catch (final Exception ex) {
            BugReporter.reportException(ex);
            throw new RuntimeException("Error exporting card." + ex);
        }
    }

    /** */
    @SuppressWarnings("unchecked")
    private void printProxies() {
        final File filename = this.getPrintProxiesFilename();
        if (filename == null) {
            return;
        }

        try {
            CardSetSerializer.writeCardHtml(
                ((CardSetController<CardSet>) CCardSetEditorUI.SINGLETON_INSTANCE
                .getCurrentEditorController().getCardSetController()).getModel(), filename);
        } catch (final Exception ex) {
            BugReporter.reportException(ex);
            throw new RuntimeException("Error exporting card." + ex);
        }
    }

    private File getExportFilename() {
        fileChooser.setDialogTitle("Export CardSet");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setCurrentDirectory(previousDirectory);
        
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            final String check = file.getAbsolutePath();

            previousDirectory = file.getParentFile();

            return check.endsWith(".dck") ? file : new File(check + ".dck");
        }
        return null;
    }

    private File getPrintProxiesFilename() {
        final JFileChooser save = new JFileChooser(previousDirectory);
        save.setDialogTitle("Print Proxies");
        save.setDialogType(JFileChooser.SAVE_DIALOG);
        save.setFileFilter(CardSetSerializer.HTML_FILTER);

        if (save.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            final File file = save.getSelectedFile();
            final String check = file.getAbsolutePath();

            previousDirectory = file.getParentFile();

            return check.endsWith(".html") ? file : new File(check + ".html");
        }
        return null;
    }

}
