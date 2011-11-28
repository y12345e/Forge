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
package forge.view.match;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import forge.AllZone;
import forge.control.match.ControlInput;
import forge.view.toolbox.FButton;
import forge.view.toolbox.FRoundedPanel;
import forge.view.toolbox.FSkin;

/**
 * Assembles Swing components of input area.
 * 
 */
@SuppressWarnings("serial")
public class ViewInput extends FRoundedPanel {
    private final ControlInput control;
    private final JButton btnOK, btnCancel;
    private final JTextArea tarMessage;
    private final FSkin skin;

    /**
     * Assembles UI for input area (buttons and message panel).
     * 
     */
    public ViewInput() {
        super();
        this.skin = AllZone.getSkin();
        this.setToolTipText("Input Area");
        this.setBackground(this.skin.getClrTheme());
        this.setForeground(this.skin.getClrText());
        this.setCorners(new boolean[] { false, false, false, true });
        this.setBorders(new boolean[] { true, false, false, true });

        this.setLayout(new MigLayout("wrap 2, fill, insets 0, gap 0"));

        // Cancel button
        this.btnCancel = new FButton("Cancel");
        this.btnOK = new FButton("OK");

        this.tarMessage = new JTextArea();
        this.tarMessage.setOpaque(false);
        this.tarMessage.setFocusable(false);
        this.tarMessage.setEditable(false);
        this.tarMessage.setLineWrap(true);
        this.tarMessage.setWrapStyleWord(true);
        this.tarMessage.setForeground(this.skin.getClrText());
        this.tarMessage.setFont(this.skin.getFont1().deriveFont(Font.PLAIN, 12));
        this.add(this.tarMessage, "span 2 1, h 80%!, w 96%!, gapleft 2%, gaptop 1%");
        this.add(this.btnOK, "w 47%!, gapright 2%, gapleft 1%");
        this.add(this.btnCancel, "w 47%!, gapright 1%");

        // After all components are in place, instantiate controller.
        this.control = new ControlInput(this);
    }

    /**
     * Gets the controller.
     * 
     * @return ControlInput
     */
    public ControlInput getController() {
        return this.control;
    }

    /**
     * Gets the btn ok.
     * 
     * @return JButton
     */
    public JButton getBtnOK() {
        return this.btnOK;
    }

    /**
     * Gets the btn cancel.
     * 
     * @return JButton
     */
    public JButton getBtnCancel() {
        return this.btnCancel;
    }

    /**
     * Gets the tar message.
     * 
     * @return JTextArea
     */
    public JTextArea getTarMessage() {
        return this.tarMessage;
    }
}
