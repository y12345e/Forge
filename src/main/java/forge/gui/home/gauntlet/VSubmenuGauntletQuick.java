package forge.gui.home.gauntlet;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.home.EMenuGroup;
import forge.gui.home.IVSubmenu;
import forge.gui.home.StartButton;
import forge.gui.home.VHomeUI;
import forge.gui.toolbox.FCheckBox;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FPanel;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.special.FDeckChooser;

/** 
 * Assembles Swing components of "quick gauntlet" submenu singleton.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 *
 */
public enum VSubmenuGauntletQuick implements IVSubmenu<CSubmenuGauntletQuick> {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Quick Gauntlets");

    // Other fields
    private final FPanel pnlOptions = new FPanel(new MigLayout("insets 0, gap 0, wrap"));
    private final FLabel lblTitle = new FLabel.Builder()
        .text("Quick Gauntlet Builder").fontAlign(SwingConstants.CENTER)
        .opaque(true).fontSize(16).build();

    private final FLabel lblDecklist = new FLabel.Builder()
        .text("Double click a non-random deck for its decklist.")
        .fontSize(12).build();

    private final JSlider sliOpponents = new JSlider(SwingConstants.HORIZONTAL, 5, 50, 20);
    //private JSlider sliGamesPerMatch = new JSlider(JSlider.HORIZONTAL, 1, 7, 3);

    private final JCheckBox boxUserDecks = new FCheckBox("Custom User Decks");
    private final JCheckBox boxQuestDecks = new FCheckBox("Quest Decks");
    private final JCheckBox boxColorDecks = new FCheckBox("Fully random color Decks");
    private final JCheckBox boxThemeDecks = new FCheckBox("Semi-random theme Decks");

    private final FDeckChooser lstDecks = new FDeckChooser("Deck", false);
    private final QuickGauntletLister gauntletList = new QuickGauntletLister();

    private final JLabel lblOptions = new FLabel.Builder().fontSize(16)
            .fontStyle(Font.BOLD).text("OPTIONS").fontAlign(SwingConstants.CENTER).build();



    private final JScrollPane scrLoad = new JScrollPane(gauntletList,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    private final JLabel lblDesc1 = new FLabel.Builder()
            .text("Matches per gauntlet").fontStyle(Font.ITALIC).build();

    private final JLabel lblDesc3 = new FLabel.Builder()
            .text("Allowed deck types").fontStyle(Font.ITALIC).build();

    private final JLabel lblDesc = new FLabel.Builder().text(
            "A new quick gauntlet is auto-saved. They can be loaded in the \"Load Gauntlet\" screen.")
            .fontSize(12).build();

    private final StartButton btnStart  = new StartButton();

    private VSubmenuGauntletQuick() {
        FSkin.get(lblTitle).setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        boxUserDecks.setSelected(true);
        boxQuestDecks.setSelected(true);
        boxThemeDecks.setSelected(true);
        boxColorDecks.setSelected(true);


        sliOpponents.setMajorTickSpacing(5);
        sliOpponents.setMinorTickSpacing(0);
        sliOpponents.setPaintTicks(false);
        sliOpponents.setPaintLabels(true);
        sliOpponents.setSnapToTicks(true);
        sliOpponents.setOpaque(false);
        FSkin.get(sliOpponents).setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        sliOpponents.setFont(FSkin.getFont(12));



        scrLoad.setOpaque(false);
        scrLoad.getViewport().setOpaque(false);
        scrLoad.setBorder(null);

        FSkin.get(pnlOptions).setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));
        pnlOptions.add(lblOptions, "h 30px!, w 96%!, gap 2% 0 0 5px");
        pnlOptions.add(sliOpponents, "h 40px!, w 96%!, gap 2% 0 0 5px, ax center");
        pnlOptions.add(lblDesc1, "w 96%!, gap 2% 0 0 20px");
        pnlOptions.add(lblDesc3, "w 96%!, gap 2% 0 0 0");
        pnlOptions.setCornerDiameter(0);
        pnlOptions.add(boxUserDecks, "w 96%!, h 30px!, gap 2% 0 0 5px");
        pnlOptions.add(boxQuestDecks, "w 96%!, h 30px!, gap 2% 0 0 5px");
        pnlOptions.add(boxThemeDecks, "w 96%!, h 30px!, gap 2% 0 0 5px");
        pnlOptions.add(boxColorDecks, "w 96%!, h 30px!, gap 2% 0 0 0");
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getGroupEnum()
     */
    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.GAUNTLET;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuTitle()
     */
    @Override
    public String getMenuTitle() {
        return "Quick Gauntlet";
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getItemEnum()
     */
    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_GAUNTLETQUICK;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#populate()
     */
    @Override
    public void populate() {
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0, wrap 2"));

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblTitle, "w 98%!, h 30px!, gap 1% 0 15px 15px, span 2");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblDesc, "ax center, gap 0 0 0 5px, span 2");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblDecklist, "ax center, gap 0 0 0 15px, span 2");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(pnlOptions, "w 40%!, gap 1% 1% 0 0, pushy, growy");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lstDecks, "w 57%!, pushy, growy");
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(btnStart, "w 98%!, ax center, gap 1% 0 20px 20px, span 2");

        getLstDecks().populate();
        
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
    }

    /** @return {@link javax.swing.JList} */
    public FDeckChooser getLstDecks() {
        return this.lstDecks;
    }

    /** @return {@link forge.gui.home.gauntlet.QuickGauntletLister} */
    public QuickGauntletLister getGauntletLister() {
        return this.gauntletList;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getBoxUserDecks() {
        return boxUserDecks;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getBoxQuestDecks() {
        return boxQuestDecks;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getBoxColorDecks() {
        return boxColorDecks;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getBoxThemeDecks() {
        return boxThemeDecks;
    }
    /** @return {@link javax.swing.JSlider} */
    public JSlider getSliOpponents() {
        return this.sliOpponents;
    }

    /** @return {@link javax.swing.JButton} */
    public JButton getBtnStart() {
        return this.btnStart;
    }

    //========== Overridden from IVDoc

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_GAUNTLETQUICK;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getTabLabel()
     */
    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getLayoutControl()
     */
    @Override
    public CSubmenuGauntletQuick getLayoutControl() {
        return CSubmenuGauntletQuick.SINGLETON_INSTANCE;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#setParentCell(forge.gui.framework.DragCell)
     */
    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getParentCell()
     */
    @Override
    public DragCell getParentCell() {
        return parentCell;
    }
}
