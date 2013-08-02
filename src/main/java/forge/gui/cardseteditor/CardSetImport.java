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
package forge.gui.cardseteditor;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.miginfocom.swing.MigLayout;
import forge.cardset.CardSetBase;
import forge.gui.GuiUtils;
import forge.gui.cardseteditor.controllers.ACEditorBase;
import forge.item.InventoryItem;

/**
 * 
 * Dialog for quick import of cardsets.
 *
 * @param <TItem>
 * @param <TModel>
 */
public class CardSetImport<TItem extends InventoryItem, TModel extends CardSetBase> extends JDialog {
    private static final long serialVersionUID = -5837776824284093004L;

    private final JTextArea txtInput = new JTextArea();
    private static final String STYLESHEET = "<style>"
            + "body, h1, h2, h3, h4, h5, h6, table, tr, td, p {margin: 3px 1px; padding: 0; font-weight: "
            + "normal; font-style: normal; text-decoration: none; font-family: Arial; font-size: 10px;} "
            +
            // "h1 {border-bottom: solid 1px black; color: blue; font-size: 12px; margin: 3px 0 9px 0; } "
            // +
            ".comment {color: #666666;} " + ".knowncard {color: #009900;} " + ".unknowncard {color: #990000;} "
            + ".section {padding: 3px 10px; margin: 3px 0; font-weight: 700; background-color: #DDDDDD; } "
            + "</style>";
    private static final String HTML_WELCOME_TEXT = "<html>"
            + CardSetImport.STYLESHEET
            + "<h3>You'll see recognized cards here</h3>"
            + "<div class='section'>Legend</div>"
            + "<ul>"
            + "<li class='knowncard'>Recognized cards will be shown in green. These cards will be auto-imported into a new cardset<BR></li>"
            + "<li class='unknowncard'>Lines which seem to be cards but are either misspelled or unsupported by Forge, are shown in dark-red<BR></li>"
            + "<li class='comment'>Lines that appear unsignificant will be shown in gray<BR><BR></li>" + "</ul>"
            + "<div class='section'>Choosing source</div>"
            + "<p>In most cases when you paste from clipboard a carefully selected area of a webpage, it works perfectly.</p>"
            + "<p>Sometimes to filter out unneeded data you may have to export cardset in MTGO format, and paste here downloaded file contents.</p>"
            + "<p>Sideboard recognition is supported. Make sure that the sideboard cards are listed after a line that contains the word 'Sideboard'</p>"
            + "</html>";

    private final JEditorPane htmlOutput = new JEditorPane("text/html", CardSetImport.HTML_WELCOME_TEXT);
    private final JScrollPane scrollInput = new JScrollPane(this.txtInput);
    private final JScrollPane scrollOutput = new JScrollPane(this.htmlOutput);
    private final JLabel summaryMain = new JLabel("Imported cardset summary will appear here");
    private final JLabel summarySide = new JLabel("This is second line");
    private final JButton cmdAccept = new JButton("Import CardSet");
    private final JButton cmdCancel = new JButton("Cancel");
    private final JCheckBox newEditionCheck = new JCheckBox("Import latest version of card", true);

    private final ACEditorBase<TItem, TModel> host;

    /**
     * Instantiates a new cardset import.
     * 
     * @param g
     *            the g
     */
    public CardSetImport(final ACEditorBase<TItem, TModel> g) {
        this.host = g;

        final int wWidth = 600;
        final int wHeight = 600;

        this.setPreferredSize(new java.awt.Dimension(wWidth, wHeight));
        this.setSize(wWidth, wHeight);
        GuiUtils.centerFrame(this);
        this.setResizable(false);
        this.setTitle("CardSet Importer");

        final Font fButtons = new java.awt.Font("Dialog", 0, 13);
        this.cmdAccept.setFont(fButtons);
        this.cmdCancel.setFont(fButtons);

        this.txtInput.setFont(fButtons);
        // htmlOutput.setFont(fButtons);

        this.htmlOutput.setEditable(false);

        this.scrollInput.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), "Paste or type a cardsetlist"));
        this.scrollOutput.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
                "Expect the recognized lines to appear"));
        this.scrollInput.setViewportBorder(BorderFactory.createLoweredBevelBorder());
        this.scrollOutput.setViewportBorder(BorderFactory.createLoweredBevelBorder());

        this.getContentPane().setLayout(new MigLayout("fill"));
        this.getContentPane().add(this.scrollInput, "cell 0 0, w 50%, growy, pushy");
        this.getContentPane().add(this.newEditionCheck, "cell 0 1, w 50%, align c");
        this.getContentPane().add(this.scrollOutput, "cell 1 0, w 50%, growy, pushy");
        this.getContentPane().add(this.summaryMain, "cell 1 1, label");
        this.getContentPane().add(this.summarySide, "cell 1 2, label");

        this.getContentPane().add(this.cmdAccept, "cell 1 3, split 2, w 100, align c");
        this.getContentPane().add(this.cmdCancel, "w 100");


        this.cmdCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                CardSetImport.this.processWindowEvent(new WindowEvent(CardSetImport.this, WindowEvent.WINDOW_CLOSING));
            }
        });

        this.cmdAccept.addActionListener(new ActionListener() {
            /*@SuppressWarnings("unchecked")*/
            @Override
            public void actionPerformed(final ActionEvent e) {
                final String warning = "This will replace contents of your currently open cardset with whatever you are importing. Proceed?";
                final int answer = JOptionPane.showConfirmDialog(CardSetImport.this, warning, "Replacing old cardset",
                        JOptionPane.YES_NO_OPTION);
                if (JOptionPane.NO_OPTION == answer) {
                    return;
                }
                /*final CardSet toSet = CardSetImport.this.buildCardSet();
                CardSetImport.this.host.getCardSetController().setModel((TModel) toSet);*/
                CardSetImport.this.processWindowEvent(new WindowEvent(CardSetImport.this, WindowEvent.WINDOW_CLOSING));
            }
        });

        this.txtInput.getDocument().addDocumentListener(new OnChangeTextUpdate());
        this.cmdAccept.setEnabled(false);
    }

    /**
     * The Class OnChangeTextUpdate.
     */
    protected class OnChangeTextUpdate implements DocumentListener {
        private void onChange() {
            /*CardSetImport.this.readInput();
            CardSetImport.this.displayTokens();
            CardSetImport.this.updateSummaries();*/
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#insertUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public final void insertUpdate(final DocumentEvent e) {
            this.onChange();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#removeUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public final void removeUpdate(final DocumentEvent e) {
            this.onChange();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#changedUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public void changedUpdate(final DocumentEvent e) {
        } // Happend only on ENTER pressed
    }
}
