package forge.gui.home.variant;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import net.miginfocom.swing.MigLayout;
import forge.card.CardCoreType;
import forge.card.CardRules;
import forge.card.CardRulesPredicates;
import forge.game.player.PlayerType;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.home.EMenuGroup;
import forge.gui.home.IVSubmenu;
import forge.gui.home.LblHeader;
import forge.gui.home.StartButton;
import forge.gui.home.VHomeUI;
import forge.gui.toolbox.FCheckBox;
import forge.gui.toolbox.FDeckChooser;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FList;
import forge.gui.toolbox.FPanel;
import forge.gui.toolbox.FRadioButton;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FSkin;
import forge.item.CardDb;
import forge.item.CardPrinted;

/** 
 * Assembles Swing components of constructed submenu singleton.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 *
 */
public enum VSubmenuVanguard implements IVSubmenu<CSubmenuVanguard> {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Vanguard Mode");

    /** */
    private final LblHeader lblTitle = new LblHeader("Variant: Vanguard");

    private final JPanel pnlStart = new JPanel(new MigLayout("insets 0, gap 0, wrap 2"));

    private final StartButton btnStart  = new StartButton();

    private final JCheckBox cbSingletons = new FCheckBox("Singleton Mode");
    private final JCheckBox cbArtifacts = new FCheckBox("Remove Artifacts");
    private final JCheckBox cbRemoveSmall = new FCheckBox("Remove Small Creatures");

    private final Predicate<CardPrinted> avatarTypePred = CardPrinted.Predicates.type("Vanguard");
    private final Iterable<CardPrinted> allAvatars = Iterables.filter(CardDb.instance().getAllCards(), avatarTypePred);
    private final List<CardPrinted> allAiAvatars = new ArrayList<CardPrinted>();
    
    //////////////////////////////
        
    private final JTabbedPane tabPane = new JTabbedPane();
    private final List<FPanel> playerPanels = new ArrayList<FPanel>();
    private final List<FDeckChooser> deckChoosers = new ArrayList<FDeckChooser>();
    private final List<FList> avatarLists = new ArrayList<FList>();
    
    private final List<JRadioButton> fieldRadios = new ArrayList<JRadioButton>();
    private final ButtonGroup grpFields = new ButtonGroup();
    private int currentNumTabsShown = 8;
    
    //////////////////////////////
    
    private VSubmenuVanguard() {
        
        lblTitle.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME2));

        Vector<Object> humanListData = new Vector<Object>();
        Vector<Object> aiListData = new Vector<Object>();
        humanListData.add("Random");
        aiListData.add("Random");
        for(CardPrinted cp : allAvatars)
        {
            humanListData.add(cp);
            if(!cp.getCard().getRemAIDecks())
            {
                aiListData.add(cp);
                allAiAvatars.add(cp);
            }
        }
        
        //This listener will look for any of the radio buttons being selected
        //and call the method that shows/hides tabs appropriately.
        ItemListener iListener = new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent arg0) {
                FRadioButton aButton = (FRadioButton)arg0.getSource();
                
                if(arg0.getStateChange() == ItemEvent.SELECTED)
                {
                    changeTabs(Integer.parseInt(aButton.getText()));
                }
            }
            
        };
        
        //Create all 8 player settings panel
        FRadioButton tempRadio = null;
        FPanel tempPanel;
        FDeckChooser tempChooser;
        FList tempList;

        //Settings panel
        FPanel settingsPanel = new FPanel();
        FPanel radioPane = new FPanel();
        radioPane.setLayout(new MigLayout("wrap 1"));
        radioPane.setOpaque(false);
        for (int i = 1; i < 8; i++) {
            tempRadio = new FRadioButton();
            tempRadio.setText(String.valueOf(i));
            fieldRadios.add(tempRadio);
            grpFields.add(tempRadio);
            tempRadio.setSelected(true);
            tempRadio.addItemListener(iListener);
            radioPane.add(tempRadio,"wrap");
        }
        settingsPanel.add(radioPane);
        settingsPanel.add(new FLabel.Builder().text("Set number of opponents").build());
        tabPane.add("Settings",settingsPanel);
        
        //Player panels (Human + 7 AIs)
        for (int i = 0; i < 8;i++) {
            tempPanel = new FPanel();
            tempPanel.setLayout(new MigLayout("insets 0, gap 0 , wrap 2"));
            
            tempChooser = new FDeckChooser("Select deck:", i == 0 ? PlayerType.HUMAN : PlayerType.COMPUTER);
            tempChooser.initialize();
            
            tempList = new FList();
            
            tempList.setListData(i == 0 ? humanListData : aiListData);
            tempList.setSelectedIndex(0);
            
            deckChoosers.add(tempChooser);
            avatarLists.add(tempList);
            
            tempPanel.add(tempChooser,"span 1 2, w 44%!, gap 0 0 20px 20px, growy, pushy");
            
            tempPanel.add(new FLabel.Builder().text("Select Avatar:").build());
            
            JScrollPane scrAvatar = new FScrollPane(tempList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            tempPanel.add(scrAvatar,"h 90%!");
            
            playerPanels.add(tempPanel);
            tabPane.add("Player " + (i+1), tempPanel);
        }
        
        final String strCheckboxConstraints = "w 200px!, h 30px!, gap 0 20px 0 0";
        pnlStart.setOpaque(false);
        pnlStart.add(cbSingletons, strCheckboxConstraints);
        pnlStart.add(btnStart, "span 1 3, growx, pushx, align center");
        pnlStart.add(cbArtifacts, strCheckboxConstraints);
        pnlStart.add(cbRemoveSmall, strCheckboxConstraints);
    }
    
    private void changeTabs(int toShow)
    {
        if(toShow < currentNumTabsShown)
        {
            for(int i=currentNumTabsShown;i>toShow+1;i--)
            {
                tabPane.remove(i);
            }
            currentNumTabsShown = tabPane.getComponentCount()-1;
        }
        else
        {
            for(int i=currentNumTabsShown;i<=toShow;i++)
            {
                tabPane.add("Player " + (i+1),playerPanels.get(i));
            }
            currentNumTabsShown = tabPane.getComponentCount()-1;
        }
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getGroupEnum()
     */
    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.VARIANT;
    }    
    
    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuTitle()
     */
    @Override
    public String getMenuTitle() {
        return "Vanguard";
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getItemEnum()
     */
    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_VANGUARD;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#populate()
     */
    @Override
    public void populate() {
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0, wrap 1, ax right"));

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(lblTitle, "w 80%!, h 40px!, gap 0 0 15px 15px, span 2, ax right");
        
        for(FDeckChooser fdc : deckChoosers)
        {
            fdc.populate();
        }        
        
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(tabPane, "gap 0 0 50px 50px, growx, growy");

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(pnlStart, "span 1, ax center");

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
        
    }


    /** @return {@link javax.swing.JButton} */
    public JButton getBtnStart() {
        return this.btnStart;
    }
    

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbSingletons() {
        return cbSingletons;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbArtifacts() {
        return cbArtifacts;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbRemoveSmall() {
        return cbRemoveSmall;
    }

    //========== Overridden from IVDoc

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_VANGUARD;
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
    public CSubmenuVanguard getLayoutControl() {
        return CSubmenuVanguard.SINGLETON_INSTANCE;
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
    
    /**
     * 
     * @return a deckchooser for every player
     */
    public List<FDeckChooser> getDeckChoosers()
    {
        return deckChoosers;
    }
    
    public List<FList> getAvatarLists()
    {
        return avatarLists;
    }
    
    public int getNumPlayers()
    {
        return currentNumTabsShown-1;
    }

    /**
     * @return the allAvatars
     */
    public Iterable<CardPrinted> getAllAvatars() {
        return allAvatars;
    }

    /**
     * @return the allAiAvatars
     */
    public List<CardPrinted> getAllAiAvatars() {
        return allAiAvatars;
    }
}
