package forge.gui.toolbox;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;

import forge.gui.toolbox.FSkin.SkinProp;

/** 
 * An extension of JScrollPane that can be used as a panel and supports using arrow buttons to scroll instead of scrollbars
 *
 */
@SuppressWarnings("serial")
public class FScrollPanel extends JScrollPane {
    private final FLabel[] arrowButtons = new FLabel[4];
    private final JPanel innerPanel;
    private final boolean useArrowButtons;
    
    /**
     * An extension of JScrollPane that can be used as a panel and supports using arrow buttons to scroll instead of scrollbars
     * This constructor assumes no layout, assumes using scrollbars to scroll, and "as needed" for horizontal and vertical scroll policies.
     * 
     */
    public FScrollPanel() {
        this(null);
    }
    
    /**
     * An extension of JScrollPane that can be used as a panel and supports using arrow buttons to scroll instead of scrollbars
     * This constructor assumes using scrollbars to scroll and "as needed" for horizontal and vertical scroll policies.
     * 
     * @param layout &emsp; Layout for panel.
     */
    public FScrollPanel(final LayoutManager layout) {
        this(layout, false);
    }
    
    /**
     * An extension of JScrollPane that can be used as a panel and supports using arrow buttons to scroll instead of scrollbars
     * This constructor assumes "as needed" for horizontal and vertical scroll policies.
     * 
     * @param layout &emsp; Layout for panel.
     * @param useArrowButtons &emsp; True to use arrow buttons to scroll, false to use scrollbars
     */
    public FScrollPanel(final LayoutManager layout, boolean useArrowButtons) {
        this(layout, useArrowButtons, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * An extension of JScrollPane that can be used as a panel and supports using arrow buttons to scroll instead of scrollbars
     * 
     * @param layout &emsp; Layout for panel.
     * @param useArrowButtons &emsp; True to use arrow buttons to scroll, false to use scrollbars
     * @param vertical0 &emsp; Vertical scroll bar policy
     * @param horizontal0 &emsp; Horizontal scroll bar policy
     */
    public FScrollPanel(final LayoutManager layout, boolean useArrowButtons0, final int vertical0, final int horizontal0) {
        super(new JPanel(layout), vertical0, horizontal0);

        innerPanel = (JPanel)getViewport().getView();
        useArrowButtons = useArrowButtons0;

        getViewport().setOpaque(false);
        innerPanel.setOpaque(false);
        setOpaque(false);
        setBorder(null);
        getHorizontalScrollBar().setUnitIncrement(16);
        getVerticalScrollBar().setUnitIncrement(16);
        if (useArrowButtons) {
            //ensure scrollbar aren't shown
            getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
            getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        }
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (useArrowButtons) {
            //determine which buttons should be visible
            boolean[] visible = new boolean[] { false, false, false, false };
            final JScrollBar horzScrollBar = this.getHorizontalScrollBar();
            if (horzScrollBar.isVisible()) { //NOTE: scrollbar wouldn't actually be visible since size set to 0 to hide it
                visible[0] = horzScrollBar.getValue() > 0;
                visible[1] = horzScrollBar.getValue() < horzScrollBar.getMaximum()  - horzScrollBar.getModel().getExtent();
            }
            final JScrollBar vertScrollBar = this.getVerticalScrollBar();
            if (vertScrollBar.isVisible()) {
                visible[2] = vertScrollBar.getValue() > 0;
                visible[3] = vertScrollBar.getValue() < vertScrollBar.getMaximum() - vertScrollBar.getModel().getExtent();
            }
            for (int dir = 0; dir < 4; dir++) {
                updateArrowButton(dir, visible);
            }
        }
    }
    
    private void updateArrowButton(int dir, boolean[] visible) {
        FLabel arrowButton = arrowButtons[dir];
        if (!visible[dir]) {
            if (arrowButton != null) {
                arrowButton.setVisible(false);
            }
            return;
        }
        
        //determine bounds of button
        int x, y, w, h;
        final int panelWidth = getWidth();
        final int panelHeight = getHeight();
        final int arrowButtonSize = 18;
        
        if (dir < 2) { //if button for horizontal scrolling
            y = 0;
            h = panelHeight;
            if (visible[2]) {
                y += arrowButtonSize;
                h -= arrowButtonSize;
            }
            if (visible[3]) {
                h -= arrowButtonSize;
            }
            x = (dir == 0 ? 0 : panelWidth - arrowButtonSize);
            w = arrowButtonSize;
        }
        else { //if button for vertical scrolling
            x = 0;
            w = panelWidth;
            if (visible[0]) {
                x += arrowButtonSize;
                w -= arrowButtonSize;
            }
            if (visible[1]) {
                w -= arrowButtonSize;
            }
            y = (dir == 2 ? 0 : panelHeight - arrowButtonSize);
            h = arrowButtonSize;
        }
        
        if (arrowButton == null) {
            SkinProp arrowProp;
            JScrollBar scrollBar;
            int incrementDirection;
            switch (dir) {
                case 0:
                    arrowProp = FSkin.LayoutImages.IMG_CUR_L;
                    scrollBar = getHorizontalScrollBar();
                    incrementDirection = -1;
                    break;
                case 1:
                    arrowProp = FSkin.LayoutImages.IMG_CUR_R;
                    scrollBar = getHorizontalScrollBar();
                    incrementDirection = 1;
                    break;
                case 2:
                    arrowProp = FSkin.LayoutImages.IMG_CUR_T;
                    scrollBar = getVerticalScrollBar();
                    incrementDirection = -1;
                    break;
                default:
                    arrowProp = FSkin.LayoutImages.IMG_CUR_B;
                    scrollBar = getVerticalScrollBar();
                    incrementDirection = 1;
                    break;
            }
            arrowButton = arrowButtons[dir] = new FLabel.ButtonBuilder()
                .icon(new ImageIcon(FSkin.getImage(arrowProp)))
                .iconScaleAuto(true).iconScaleFactor(1.8).build();
            hookArrowButtonEvents(arrowButton, scrollBar, incrementDirection);
        }
        //absolutely position button in front of scroll panel
        arrowButton.setSize(w, h);
        FAbsolutePositioner.SINGLETON_INSTANCE.show(arrowButton, this, x, y);
    }
    
    private void hookArrowButtonEvents(final FLabel arrowButton, final JScrollBar scrollBar, final int incrementDirection) {
        //create timer to continue scrollling while mouse remains down
        final Timer timer = new Timer(50, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!arrowButton.isVisible()) {
                    //ensure timer stops if button hidden from scrolling to beginning/end (based on incrementDirection)
                    ((Timer)e.getSource()).stop();
                }
                scrollBar.setValue(scrollBar.getValue() + scrollBar.getUnitIncrement() * incrementDirection);
            }
        });
        timer.setInitialDelay(500); //wait half a second after mouse down before starting timer

        arrowButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                scrollBar.setValue(scrollBar.getValue() + scrollBar.getUnitIncrement() * incrementDirection);
                timer.start();
            }
            
            @Override
            public void mouseReleased(final MouseEvent e) {
                timer.stop();
            }
        });
    }
    
    //relay certain methods to the inner panel if it has been initialized
    @Override
    public Component add(Component comp) {
        if (innerPanel != null) {
            return innerPanel.add(comp);
        }
        return super.add(comp);
    }
    
    @Override
    public void add(PopupMenu popup) {
        if (innerPanel != null) {
            innerPanel.add(popup);
            return;
        }
        super.add(popup);
    }
    
    @Override
    public void add(Component comp, Object constraints) {
        if (innerPanel != null) {
            innerPanel.add(comp, constraints);
            return;
        }
        super.add(comp, constraints);
    }
    
    @Override
    public Component add(Component comp, int index) {
        if (innerPanel != null) {
            return innerPanel.add(comp, index);
        }
        return super.add(comp, index);
    }
    
    @Override
    public void add(Component comp, Object constraints, int index) {
        if (innerPanel != null) {
            innerPanel.add(comp, constraints, index);
            return;
        }
        super.add(comp, constraints, index);
    }
    
    @Override
    public Component add(String name, Component comp) {
        if (innerPanel != null) {
            return innerPanel.add(name, comp);
        }
        return super.add(name, comp);
    }
}
