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
package forge.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import forge.gui.GuiDisplayUtil;
import forge.properties.NewConstants;

/**
 * <p>
 * CopyFiles class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CopyFiles extends SwingWorker<Void, Integer> {

    private final List<File> fileList;

    /** The j lb. */
    private final JLabel jLabel;

    /** The j b. */
    private final JProgressBar jProgressBar;

    /** The j check. */
    private final JCheckBox jCheck;

    /** The j source. */
    private final JButton jSource;

    /** The count. */
    private int count;

    /**
     * <p>
     * Constructor for CopyFiles.
     * </p>
     * 
     * @param fileList
     *            a {@link java.util.List} object.
     * @param jLabelTotalFiles
     *            a {@link javax.swing.JLabel} object.
     * @param jProgressBar
     *            a {@link javax.swing.JProgressBar} object.
     * @param jCheckBox
     *            a {@link javax.swing.JCheckBox} object.
     * @param jButtonSource
     *            a {@link javax.swing.JButton} object.
     */
    public CopyFiles(final List<File> fileList, final JLabel jLabelTotalFiles, final JProgressBar jProgressBar,
            final JCheckBox jCheckBox, final JButton jButtonSource) {
        this.fileList = fileList;
        this.jLabel = jLabelTotalFiles;
        this.jProgressBar = jProgressBar;
        this.jCheck = jCheckBox;
        this.jSource = jButtonSource;
    }

    /** {@inheritDoc} */
    @Override
    protected final Void doInBackground() {
        for (int i = 0; i < this.fileList.size(); i++) {
            this.publish();
            String cName, name, source;
            name = this.fileList.get(i).getName();
            source = this.fileList.get(i).getAbsolutePath();
            cName = name.substring(0, name.length() - 8);
            //cName = GuiDisplayUtil.cleanString(cName) + ".jpg";
            final File sourceFile = new File(source);
            final File reciever = new File(NewConstants.CACHE_CARD_PICS_DIR, cName);
            reciever.delete();

            try {
                reciever.createNewFile();
                final FileOutputStream fos = new FileOutputStream(reciever);
                final FileInputStream fis = new FileInputStream(sourceFile);
                final byte[] buff = new byte[32 * 1024];
                int length;
                while (fis.available() > 0) {
                    length = fis.read(buff);
                    if (length > 0) {
                        fos.write(buff, 0, length);
                    }
                }
                fos.flush();
                fis.close();
                fos.close();
                this.count = ((i * 100) / this.fileList.size()) + 1;
                this.setProgress(this.count);

            } catch (final IOException e1) {
                e1.printStackTrace();
            }

        }
        return null;

    }

    /** {@inheritDoc} */
    @Override
    protected final void done() {
        this.jLabel.setText("All files were copied successfully.");
        this.jProgressBar.setIndeterminate(false);
        this.jCheck.setEnabled(true);
        this.jSource.setEnabled(true);

    }

}
