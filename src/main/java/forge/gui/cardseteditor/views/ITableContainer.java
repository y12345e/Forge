package forge.gui.cardseteditor.views;

import javax.swing.JTable;

import forge.gui.cardseteditor.SEditorUtil;
import forge.gui.toolbox.FLabel;

/** 
 * Dictates methods needed for a class to act as a container for
 * a EditorTableView cardset editing component.
 * 
 * <br><br><i>(I at beginning of class name denotes an interface.)</i>
 * 
 */
public interface ITableContainer {
    /**
     * Sets the table used for displaying cards in this
     * cardset editor container.
     * 
     * @param tbl0 &emsp; {@link forge.gui.cardseteditor.tables.EditorTableView}
     */
     void setTableView(JTable tbl0);

     FLabel getStatLabel(SEditorUtil.StatTypes s);
 }
