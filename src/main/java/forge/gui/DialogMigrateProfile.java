/*
 * Forge: Play Magic: the Gathering.
 * Copyright (c) 2013  Forge Team
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
package forge.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import forge.gui.MigrationSourceAnalyzer.OpType;
import forge.gui.toolbox.FButton;
import forge.gui.toolbox.FCheckBox;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FOverlay;
import forge.gui.toolbox.FPanel;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FTextArea;
import forge.gui.toolbox.FTextField;
import forge.properties.NewConstants;

public class DialogMigrateProfile {
    private final Runnable _onImportDone;
    private final FButton  _btnStart;
    private final JPanel   _selectionPanel;
    
    private volatile boolean _cancel;
    
    public DialogMigrateProfile(String srcDir, boolean showMigrationBlurb, final Runnable onImportDone) {
        FPanel p = new FPanel(new MigLayout("insets dialog, gap 0, center, wrap"));
        p.setOpaque(false);
        p.setBackgroundTexture(FSkin.getIcon(FSkin.Backgrounds.BG_TEXTURE));

        // header
        p.add(new FLabel.Builder().text("Migrate profile data (in progress: not yet functional)").fontSize(15).build(), "center");
        
        if (showMigrationBlurb) {
            FPanel blurbPanel = new FPanel(new MigLayout("insets dialog, gap 10, center, wrap"));
            blurbPanel.setOpaque(false);
            blurbPanel.add(new FLabel.Builder().text("<html><b>What's this?</b></html>").build(), "growx");
            blurbPanel.add(new FLabel.Builder().text(
                    "<html>Over the last several years, people have had to jump through a lot of hoops to" +
                    " update to the most recent version.  We hope to reduce this workload to a point where a new" +
                    " user will find that it is fairly painless to update.  In order to make this happen, Forge" +
                    " has changed where it stores your data so that it is outside of the program installation directory." +
                    "  This way, when you upgrade, you will no longer need to import your data every time to get things" +
                    " working.  There are other benefits to having user data separate from program data, too, and it" +
                    " lays the groundwork for some cool new features.</html>").build());
            blurbPanel.add(new FLabel.Builder().text("<html><b>So where's my data going?</b></html>").build(), "growx");
            blurbPanel.add(new FLabel.Builder().text(
                    "<html>Forge will now store your data in the same place as other applications on your system." +
                    "  Specifically, your personal data, like decks, quest progress, and program preferences will be" +
                    " stored in <b>" + NewConstants.USER_DIR + "</b> and all downloaded content, such as card pictures," +
                    " skins, and quest world prices will be under <b>" + NewConstants.CACHE_DIR + "</b>.  If, for whatever" +
                    " reason, you need to set different paths, cancel out of this dialog, exit Forge, and find the <b>" +
                    NewConstants.PROFILE_TEMPLATE_FILE + "</b> file in the program installation directory.  Copy or rename" +
                    " it to <b>" + NewConstants.PROFILE_FILE + "</b> and edit the paths inside it.  Then restart Forge and use" +
                    " this dialog to move your data to the paths that you set.  Keep in mind that if you install a future" +
                    " version of Forge into a different directory, you'll need to copy this file over so Forge will know" +
                    " where to find your data.</html>").build());
            blurbPanel.add(new FLabel.Builder().text(
                    "<html><b>Remember, your data won't be available until you complete this step!</b></html>").build(), "growx");
            p.add(blurbPanel, "gap 10 10 20 0");
        }
        
        // import source widgets
        JPanel importSourcePanel = new JPanel(new MigLayout("insets 0, gap 10"));
        importSourcePanel.setOpaque(false);
        importSourcePanel.add(new FLabel.Builder().text("Import from:").build());
        boolean emptySrcDir = StringUtils.isEmpty(srcDir); 
        FTextField txfSrc = new FTextField.Builder().readonly(!emptySrcDir).build();
        importSourcePanel.add(txfSrc, "pushx, growx");
        if (!emptySrcDir) {
            File srcDirFile = new File(srcDir);
            txfSrc.setText(srcDirFile.getAbsolutePath());
        }
        importSourcePanel.add(new FLabel.ButtonBuilder().text("Choose directory...").enabled(emptySrcDir).build(), "h pref+8!, w pref+12!");
        p.add(importSourcePanel, "gaptop 20, growx");
        
        // prepare import selection panel
        _selectionPanel = new JPanel();
        _selectionPanel.setOpaque(false);
        p.add(_selectionPanel, "growx, h 100%, gaptop 10");
        
        // action button widgets
        final Runnable cleanup = new Runnable() {
            @Override public void run() { SOverlayUtils.hideOverlay(); }
        };
        _btnStart = new FButton("Start import");
        _btnStart.setEnabled(false);

        final FButton btnCancel = new FButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { _cancel = true; cleanup.run(); }
        });

        _onImportDone = new Runnable() {
            @Override public void run() {
                cleanup.run();
                if (null != onImportDone) {
                    onImportDone.run();
                }
            }
        };
        
        JPanel southPanel = new JPanel(new MigLayout("ax center"));
        southPanel.setOpaque(false);
        southPanel.add(_btnStart, "center, w 40%, h pref+12!");
        southPanel.add(btnCancel, "center, w 40%, h pref+12!");
        
        p.add(southPanel, "growx");
      
        JPanel overlay = FOverlay.SINGLETON_INSTANCE.getPanel();
        overlay.setLayout(new MigLayout("insets 0, gap 0, wrap, ax center, ay center"));
        overlay.add(p, "w 800::80%, h 800::90%");
        SOverlayUtils.showOverlay();
        
        // focus cancel button
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() { btnCancel.requestFocusInWindow(); }
        });
        
        _AnalyzerUpdater analyzer = new _AnalyzerUpdater(srcDir, !emptySrcDir);
        analyzer.execute();
    }
    
    private class _UnknownDeckChoice {
        public final String name;
        public final String path;
        
        public _UnknownDeckChoice(String name0, String path0) {
            name = name0;
            path = path0;
        }
        
        @Override
        public String toString() { return name; }
    }
    
    private class _AnalyzerUpdater extends SwingWorker<Void, Void> {
        private final Map<OpType, Pair<FCheckBox, ? extends Set<Pair<File, File>>>> _selections =
                new HashMap<OpType, Pair<FCheckBox, ? extends Set<Pair<File, File>>>>();
        
        ChangeListener _stateChangedListener = new ChangeListener() {
            @Override public void stateChanged(ChangeEvent arg0) { _updateUI(); }
        };
        
        private final String       _srcDir;
        private final JComboBox    _unknownDeckCombo;
        private final FCheckBox    _moveCheckbox;
        private final FTextArea    _operationLog;
        private final JProgressBar _progressBar;
        
        // used to ensure we only have one UI update pending at a time
        private volatile boolean _uiUpdateAck;

        public _AnalyzerUpdater(String srcDir, boolean forced) {
            _srcDir = srcDir;
            
            _selectionPanel.removeAll();
            _selectionPanel.setLayout(new MigLayout("insets 0, gap 5, wrap"));
            
            JPanel cbPanel = new JPanel(new MigLayout("insets 0, gap 5"));
            cbPanel.setOpaque(false);
            
            // add deck selections
            JPanel knownDeckPanel = new JPanel(new MigLayout("insets 0, gap 5, wrap 2"));
            knownDeckPanel.setOpaque(false);
            knownDeckPanel.add(new FLabel.Builder().text("Decks").build(), "wrap");
            _addSelectionWidget(knownDeckPanel, forced, OpType.CONSTRUCTED_DECK, "Constructed decks");
            _addSelectionWidget(knownDeckPanel, forced, OpType.DRAFT_DECK,       "Draft decks");
            _addSelectionWidget(knownDeckPanel, forced, OpType.PLANAR_DECK,      "Planar decks");
            _addSelectionWidget(knownDeckPanel, forced, OpType.SCHEME_DECK,      "Scheme decks");
            _addSelectionWidget(knownDeckPanel, forced, OpType.SEALED_DECK,      "Sealed decks");
            _addSelectionWidget(knownDeckPanel, forced, OpType.UNKNOWN_DECK,     "Unknown decks");
            JPanel unknownDeckPanel = new JPanel(new MigLayout("insets 0, gap 5"));
            unknownDeckPanel.setOpaque(false);
            _unknownDeckCombo = new JComboBox();
            _unknownDeckCombo.addItem(new _UnknownDeckChoice("Constructed", NewConstants.DECK_CONSTRUCTED_DIR));
            _unknownDeckCombo.addItem(new _UnknownDeckChoice("Draft",       NewConstants.DECK_DRAFT_DIR));
            _unknownDeckCombo.addItem(new _UnknownDeckChoice("Planar",      NewConstants.DECK_PLANE_DIR));
            _unknownDeckCombo.addItem(new _UnknownDeckChoice("Scheme",      NewConstants.DECK_SCHEME_DIR));
            _unknownDeckCombo.addItem(new _UnknownDeckChoice("Sealed",      NewConstants.DECK_SEALED_DIR));
            unknownDeckPanel.add(new FLabel.Builder().text("Treat decks of unknown type as:").build());
            unknownDeckPanel.add(_unknownDeckCombo);
            knownDeckPanel.add(unknownDeckPanel, "span");
            cbPanel.add(knownDeckPanel, "aligny top");
            
            // add other data elements (gauntlets, quest data)
            JPanel dataPanel = new JPanel(new MigLayout("insets 0, gap 5, wrap"));
            dataPanel.setOpaque(false);
            dataPanel.add(new FLabel.Builder().text("Other data").build());
            _addSelectionWidget(dataPanel, forced, OpType.GAUNTLET_DATA, "Gauntlet data");
            _addSelectionWidget(dataPanel, forced, OpType.QUEST_DATA, "Quest saves");
            _addSelectionWidget(dataPanel, forced, OpType.PREFERENCE_FILE, "Preference files");
            cbPanel.add(dataPanel, "aligny top");
            _selectionPanel.add(cbPanel, "center");
            
            // add move/copy checkbox
            _moveCheckbox = new FCheckBox("move files");
            _moveCheckbox.setSelected(true);
            _moveCheckbox.setEnabled(!forced);
            _moveCheckbox.addChangeListener(_stateChangedListener);
            _selectionPanel.add(_moveCheckbox);
            
            // add operation summary textfield
            _operationLog = new FTextArea();
            _operationLog.setFocusable(true);
            JScrollPane scroller = new JScrollPane(_operationLog);
            scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            _selectionPanel.add(scroller, "w 600:100%:100%, h 60:100%:100%");
            
            // add progress bar
            _progressBar = new JProgressBar();
            _progressBar.setIndeterminate(true);
            _progressBar.setString("Analyzing source directory...");
            _progressBar.setStringPainted(true);
            _selectionPanel.add(_progressBar, "w 100%!");
            
            // set checkbox labels
            _updateUI();
        }
        
        private void _addSelectionWidget(JPanel parent, boolean forced, OpType type, String name) {
            FCheckBox cb = new FCheckBox();
            cb.setName(name);
            cb.setSelected(true);
            cb.setEnabled(!forced);
            cb.addChangeListener(_stateChangedListener);
            _selections.put(type, Pair.of(cb, Collections.newSetFromMap(new ConcurrentHashMap<Pair<File, File>, Boolean>())));
            parent.add(cb);
        }
        
        // must be called from GUI event loop thread
        private void _updateUI() {
            // set operation summary
            StringBuilder log = new StringBuilder();
            int totalOps = 0;
            for (Pair<FCheckBox, ? extends Set<Pair<File, File>>> selection : _selections.values()) {
                FCheckBox cb              = selection.getLeft();
                Set<Pair<File, File>> ops = selection.getRight();
                
                if (cb.isSelected()) {
                    totalOps += ops.size();
                }
                
                // update checkbox text with new totals
                cb.setText(String.format("%s (%d)", cb.getName(), ops.size()));
            }
            log.append(_moveCheckbox.isSelected() ? "Moving" : "Copying");
            log.append(" ").append(totalOps).append(" files\n\n");
            for (Map.Entry<OpType, Pair<FCheckBox, ? extends Set<Pair<File, File>>>> entry : _selections.entrySet()) {
                Pair<FCheckBox, ? extends Set<Pair<File, File>>> selection = entry.getValue();
                if (selection.getLeft().isSelected()) {
                    for (Pair<File, File> op : selection.getRight()) {
                        File dest = op.getRight();
                        if (OpType.UNKNOWN_DECK == entry.getKey()) {
                            _UnknownDeckChoice choice = (_UnknownDeckChoice)_unknownDeckCombo.getSelectedItem();
                            dest = new File(choice.path, dest.getName());
                        }
                        log.append(String.format("%s -> %s\n",
                                op.getLeft().getAbsolutePath(), dest.getAbsolutePath()));
                    }
                }
            }
            _operationLog.setText(log.toString());
            
            _uiUpdateAck = true;
        }
        
        private void _disableAll() {
            for (Pair<FCheckBox, ? extends Set<Pair<File, File>>> selection : _selections.values()) {
                selection.getLeft().setEnabled(false);
            }
            _unknownDeckCombo.setEnabled(false);
            _moveCheckbox.setEnabled(false);
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            Map<OpType, Set<Pair<File, File>>> selections = new HashMap<OpType, Set<Pair<File, File>>>();
            for (Map.Entry<OpType, Pair<FCheckBox, ? extends Set<Pair<File, File>>>> entry : _selections.entrySet()) {
                selections.put(entry.getKey(), entry.getValue().getRight());
            }
            
            Callable<Boolean> checkCancel = new Callable<Boolean>() {
                @Override public Boolean call() { return _cancel; }
            };
            
            final MigrationSourceAnalyzer msa = new MigrationSourceAnalyzer(_srcDir, selections, checkCancel);
            final int numFilesToAnalyze = msa.getNumFilesToAnalyze();
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    if (_cancel) { return; }
                    _progressBar.setMaximum(numFilesToAnalyze);
                    
                    // start update timer
                    final Timer timer = new Timer(500, null);
                    timer.addActionListener(new ActionListener() {
                        @Override public void actionPerformed(ActionEvent arg0) {
                            if (_cancel) {
                                timer.stop();
                                return;
                            }
                            
                            _progressBar.setValue(msa.getNumFilesAnalyzed());
                            
                            // only update if we don't already have an update pending.  we may not be prompt in
                            // updating sometimes, but that's ok
                            if (!_uiUpdateAck) { return; }
                            _uiUpdateAck = false;
                            _stateChangedListener.stateChanged(null);
                        }
                    });
                }
            });
            
            msa.doAnalysis();
            
            return null;
        }

        @Override
        protected void done() {
            if (_cancel) { return; }
            _btnStart.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent arg0) {
                    _btnStart.removeActionListener(this);
                    _btnStart.setEnabled(false);
                    
                    _disableAll();
                    
                    _Importer importer = new _Importer(
                            _selections, _unknownDeckCombo, _operationLog, _progressBar, _moveCheckbox.isSelected());
                    importer.execute();
                }
            });
            _btnStart.setEnabled(true);
        }
    }
    
    private class _Importer extends SwingWorker<Void, Void> {
        private final List<Pair<File, File>> _operations;
        private final FTextArea              _operationLog;
        private final JProgressBar           _progressBar;
        private final boolean                _move;
        
        public _Importer(Map<OpType, Pair<FCheckBox, ? extends Set<Pair<File, File>>>> selections, JComboBox unknownDeckCombo,
                FTextArea operationLog, JProgressBar progressBar, boolean move) {
            _operationLog = operationLog;
            _progressBar  = progressBar;
            _move         = move;
            
            int totalOps = 0;
            for (Pair<FCheckBox, ? extends Set<Pair<File, File>>> selection : selections.values()) {
                if (selection.getLeft().isSelected()) {
                    totalOps += selection.getRight().size();
                }
            }
            _operations = new ArrayList<Pair<File, File>>(totalOps);
            for (Map.Entry<OpType, Pair<FCheckBox, ? extends Set<Pair<File, File>>>> entry : selections.entrySet()) {
                Pair<FCheckBox, ? extends Set<Pair<File, File>>> selection = entry.getValue();
                if (selection.getLeft().isSelected()) {
                    if (OpType.UNKNOWN_DECK != entry.getKey()) {
                        _operations.addAll(selection.getRight());
                    } else {
                        for (Pair<File, File> op : selection.getRight()) {
                            _UnknownDeckChoice choice = (_UnknownDeckChoice)unknownDeckCombo.getSelectedItem();
                            _operations.add(Pair.of(op.getLeft(), new File(choice.path, op.getRight().getName())));
                        }
                    }
                }
            }
            for (Pair<FCheckBox, ? extends Set<Pair<File, File>>> selection : selections.values()) {
                if (selection.getLeft().isSelected()) {
                    _operations.addAll(selection.getRight());
                }
            }
            
            // set progress bar bounds
            _progressBar.setString(_move ? "Moving files" : "Copying files");
            _progressBar.setMinimum(0);
            _progressBar.setMaximum(_operations.size());
            _progressBar.setIndeterminate(false);
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            // working with textbox text is thread safe
            _operationLog.setText("");
            
            // assumes all destination directories have been created
            int numOps = 0;
            for (Pair<File, File> op : _operations) {
                final int curOpNum = ++numOps;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() { _progressBar.setValue(curOpNum); }
                });
                
                File srcFile  = op.getLeft();
                File destFile = op.getRight();

                try {
                    _copyFile(srcFile, destFile);
                    
                    if (_move) {
                        srcFile.delete();
                    }
                    
                    // working with textbox text is thread safe
                    _operationLog.append(String.format("%s %s -> %s\n",
                            _move ? "Moved" : "Copied",
                            srcFile.getAbsolutePath(), destFile.getAbsolutePath()));
                } catch (IOException e) {
                    _operationLog.append(String.format("Failed to %s %s -> %s (%s)\n",
                            _move ? "move" : "copy",
                            srcFile.getAbsolutePath(), destFile.getAbsolutePath(),
                            e.getMessage()));
                }
            }
            
            return null;
        }

        @Override
        protected void done() {
            _onImportDone.run();
        }
        
        private void _copyFile(File srcFile, File destFile) throws IOException {
            if(!destFile.exists()) {
                destFile.createNewFile();
            }

            FileChannel src  = null;
            FileChannel dest = null;
            try {
                src  = new FileInputStream(srcFile).getChannel();
                dest = new FileOutputStream(destFile).getChannel();
                dest.transferFrom(src, 0, src.size());
            } finally {
                if (src  != null) { src.close();  }
                if (dest != null) { dest.close(); }
            }
        }
    }
}
