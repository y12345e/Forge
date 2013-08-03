package forge.gui.toolbox;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/** 
 * An extension of JScrollPane that can be used as a panel and supports using arrow buttons to scroll instead of scrollbars
 *
 */
@SuppressWarnings("serial")
public class FScrollPanel extends JScrollPane {
    private final Color clrMain = FSkin.getColor(FSkin.Colors.CLR_THEME);
    private final Color arrowButtonBorderColor1 = FSkin.stepColor(clrMain, -20);
    private final Color arrowButtonColor1 = FSkin.stepColor(clrMain, -10);
    private final Color arrowButtonBorderColor2 = FSkin.stepColor(clrMain, 10);
    private final Color arrowButtonColor2 = FSkin.stepColor(clrMain, 20);
    private final Rectangle[] arrowButtonBounds = new Rectangle[4];
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
        
        JScrollBar horzScrollBar = getHorizontalScrollBar();
        JScrollBar vertScrollBar = getVerticalScrollBar();
        horzScrollBar.setUnitIncrement(16);
        vertScrollBar.setUnitIncrement(16);
        
        if (useArrowButtons) {
            horzScrollBar.setPreferredSize(new Dimension(0, 0)); //ensure scrollbar isn't shown
            horzScrollBar.addAdjustmentListener(new AdjustmentListener() {
                @Override
                public void adjustmentValueChanged(AdjustmentEvent arg0) {
                    
                }
            });
            vertScrollBar.setPreferredSize(new Dimension(0, 0)); //ensure scrollbar isn't shown
            vertScrollBar.addAdjustmentListener(new AdjustmentListener() {
                @Override
                public void adjustmentValueChanged(AdjustmentEvent arg0) {
                    
                }
            });
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
            Graphics2D g2d = (Graphics2D)g;
            for (int dir = 0; dir < 4; dir++) {
                drawArrowButton(g2d, dir, visible);
            }
        }
    }
    
    private void drawArrowButton(final Graphics2D g, int dir, boolean[] visible) {
        if (!visible[dir]) {
            arrowButtonBounds[dir] = null; //prevent checking for mouse down
            return;
        }
        
        //determine bounds of button
        int x, y, w, h;
        final int panelWidth = getWidth();
        final int panelHeight = getHeight();
        final int arrowButtonSize = 20;
        
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
        
        arrowButtonBounds[dir] = new Rectangle(x, y, w, h); //store bounds for use in mouse events
        
        //draw button
        GradientPaint gradient = new GradientPaint(0, h, arrowButtonColor1, 0, 0, arrowButtonColor2);
        g.setPaint(gradient);
        g.fillRect(x, y, w, h);

        g.setColor(arrowButtonBorderColor1);
        g.drawRect(x, y, w - 2, h - 2);
        g.setColor(arrowButtonBorderColor2);
        g.drawRect(x + 1, y + 1, w - 4, h - 4);
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
