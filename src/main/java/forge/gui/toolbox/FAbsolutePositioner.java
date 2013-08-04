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
package forge.gui.toolbox;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

/**
 * Utility to manage absolutely positioned components
 * 
 */

// Currently used only once, in top level UI, with layering already in place.
public enum FAbsolutePositioner {
    /** */
    SINGLETON_INSTANCE;

    private final JPanel pnl = new JPanel();

    private FAbsolutePositioner() {
        pnl.setOpaque(false);
        pnl.setLayout(null);
    }
    
    public void initialize(JLayeredPane parent, Integer index) {
        parent.add(pnl, index);
    }
    
    public void containerResized(Rectangle mainBounds) {
        pnl.setBounds(mainBounds);
        pnl.validate();
    }
    
    public void show(Component comp, Point screenLocation) {
        if (comp.getParent() == pnl) {
            comp.setLocation(screenLocation);
            return;
        }
        comp.setVisible(false);
        pnl.add(comp);
        comp.setLocation(screenLocation);
        comp.setVisible(true);
    }
    
    public void show(Component comp, Point relativeLocation, Component relativeToComp) {
        Point offset = relativeToComp.getLocationOnScreen();
        show(comp, new Point(relativeLocation.x + offset.x, relativeLocation.y + offset.y));
    }
    
    public void hide(Component comp) {
        pnl.remove(comp);
    }
    
    public void hideAll() {
        pnl.removeAll();
    }
}
