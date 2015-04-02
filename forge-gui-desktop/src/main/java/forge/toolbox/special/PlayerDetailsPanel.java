package forge.toolbox.special;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Function;

import forge.assets.FSkinProp;
import forge.card.MagicColor;
import forge.game.player.PlayerView;
import forge.gui.ForgeAction;
import forge.toolbox.FLabel;
import forge.toolbox.FMouseAdapter;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinFont;
import forge.toolbox.FSkin.SkinnedPanel;

public class PlayerDetailsPanel extends JPanel {
    private static final long serialVersionUID = -6531759554646891983L;

    private final PlayerView player;

    // Info labels
    private final DetailLabel lblHand = new DetailLabel(FSkinProp.IMG_ZONE_HAND, "Hand (%s/%s)");
    private final DetailLabel lblGraveyard = new DetailLabel(FSkinProp.IMG_ZONE_GRAVEYARD, "Graveyard (%s)");
    private final DetailLabel lblLibrary = new DetailLabel(FSkinProp.IMG_ZONE_LIBRARY, "Library (%s)");
    private final DetailLabel lblExile = new DetailLabel(FSkinProp.IMG_ZONE_EXILE, "Exile (%s)");
    private final DetailLabel lblFlashback = new DetailLabel(FSkinProp.IMG_ZONE_FLASHBACK, "Flashback cards (%s)");
    private final DetailLabel lblPoison = new DetailLabel(FSkinProp.IMG_ZONE_POISON, "Poison counters (%s)");
    private final List<Pair<DetailLabel, Byte>> manaLabels = new ArrayList<Pair<DetailLabel, Byte>>();

    public PlayerDetailsPanel(final PlayerView player0) {
        player = player0;

        manaLabels.add(Pair.of(new DetailLabel(FSkinProp.IMG_MANA_W,         "White mana (%s)"),     MagicColor.WHITE));
        manaLabels.add(Pair.of(new DetailLabel(FSkinProp.IMG_MANA_U,         "Blue mana (%s)"),      MagicColor.BLUE));
        manaLabels.add(Pair.of(new DetailLabel(FSkinProp.IMG_MANA_B,         "Black mana (%s)"),     MagicColor.BLACK));
        manaLabels.add(Pair.of(new DetailLabel(FSkinProp.IMG_MANA_R,         "Red mana (%s)"),       MagicColor.RED));
        manaLabels.add(Pair.of(new DetailLabel(FSkinProp.IMG_MANA_G,         "Green mana (%s)"),     MagicColor.GREEN));
        manaLabels.add(Pair.of(new DetailLabel(FSkinProp.IMG_MANA_COLORLESS, "Colorless mana (%s)"), MagicColor.COLORLESS));

        setOpaque(false);
        setLayout(new MigLayout("insets 0, gap 0, wrap"));
        populateDetails();

        updateZones();
        updateManaPool();
    }

    /** Adds various labels to pool area JPanel container. */
    private void populateDetails() {
        final SkinnedPanel row1 = new SkinnedPanel(new MigLayout("insets 0, gap 0"));
        final SkinnedPanel row2 = new SkinnedPanel(new MigLayout("insets 0, gap 0"));
        final SkinnedPanel row3 = new SkinnedPanel(new MigLayout("insets 0, gap 0"));
        final SkinnedPanel row4 = new SkinnedPanel(new MigLayout("insets 0, gap 0"));
        final SkinnedPanel row5 = new SkinnedPanel(new MigLayout("insets 0, gap 0"));
        final SkinnedPanel row6 = new SkinnedPanel(new MigLayout("insets 0, gap 0"));

        row1.setBackground(FSkin.getColor(FSkin.Colors.CLR_ZEBRA));
        row2.setOpaque(false);
        row3.setBackground(FSkin.getColor(FSkin.Colors.CLR_ZEBRA));
        row4.setOpaque(false);
        row5.setBackground(FSkin.getColor(FSkin.Colors.CLR_ZEBRA));
        row6.setOpaque(false);

        // Hand, library, graveyard, exile, flashback, poison labels
        final String constraintsCell = "w 50%-4px!, h 100%!, gapleft 2px, gapright 2px";

        row1.add(lblHand, constraintsCell);
        row1.add(lblLibrary, constraintsCell);

        row2.add(lblGraveyard, constraintsCell);
        row2.add(lblExile, constraintsCell);

        row3.add(lblFlashback, constraintsCell);
        row3.add(lblPoison, constraintsCell);

        row4.add(manaLabels.get(0).getLeft(), constraintsCell);
        row4.add(manaLabels.get(1).getLeft(), constraintsCell);

        row5.add(manaLabels.get(2).getLeft(), constraintsCell);
        row5.add(manaLabels.get(3).getLeft(), constraintsCell);

        row6.add(manaLabels.get(4).getLeft(), constraintsCell);
        row6.add(manaLabels.get(5).getLeft(), constraintsCell);

        final String constraintsRow = "w 100%!, h 16%!";
        add(row1, constraintsRow + ", gap 0 0 2% 0");
        add(row2, constraintsRow);
        add(row3, constraintsRow);
        add(row4, constraintsRow);
        add(row5, constraintsRow);
        add(row6, constraintsRow);
    }

    public Component getLblLibrary() {
        return lblLibrary;
    }

    /**
     * Handles observer update of player Zones - hand, graveyard, etc.
     * 
     * @param p0 &emsp; {@link forge.game.player.Player}
     */
    public void updateZones() {
        final String handSize = String.valueOf(player.getHandSize()),
                graveyardSize = String.valueOf(player.getGraveyardSize()),
                librarySize   = String.valueOf(player.getLibrarySize()),
                flashbackSize = String.valueOf(player.getFlashbackSize()),
                exileSize     = String.valueOf(player.getExileSize());
        lblHand.setText(handSize);
        lblHand.setToolTip(handSize, player.getMaxHandString());
        lblGraveyard.setText(graveyardSize);
        lblGraveyard.setToolTip(graveyardSize);
        lblLibrary.setText(librarySize);
        lblLibrary.setToolTip(librarySize);
        lblFlashback.setText(flashbackSize);
        lblFlashback.setToolTip(flashbackSize);
        lblExile.setText(exileSize);
        lblExile.setToolTip(exileSize);
    }

    /**
     * Handles observer update of non-Zone details (poison).
     */
    public void updateDetails() {
        // Poison
        final int poison = player.getPoisonCounters();
        lblPoison.setText(String.valueOf(poison));
        lblPoison.setToolTip(String.valueOf(poison));
        if (poison < 8) {
            lblPoison.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        } else {
            lblPoison.setForeground(Color.red);
        }
    }

    /**
     * Handles observer update of the mana pool.
     */
    public void updateManaPool() {
        for (final Pair<DetailLabel, Byte> label : manaLabels) {
            final String mana = String.valueOf(player.getMana(label.getRight().byteValue()));
            label.getKey().setText(mana);
            label.getKey().setToolTip(mana);
        }
    }

    public void setupMouseActions(final ForgeAction handAction, final ForgeAction libraryAction, final ForgeAction exileAction,
                                  final ForgeAction graveAction, final ForgeAction flashBackAction, final Function<Byte, Boolean> manaAction) {
        // Detail label listeners
        lblGraveyard.addMouseListener(new FMouseAdapter() {
            @Override
            public void onLeftClick(final MouseEvent e) {
                graveAction.actionPerformed(null);
            }
        });
        lblExile.addMouseListener(new FMouseAdapter() {
            @Override
            public void onLeftClick(final MouseEvent e) {
                exileAction.actionPerformed(null);
            }
        });
        lblLibrary.addMouseListener(new FMouseAdapter() {
            @Override
            public void onLeftClick(final MouseEvent e) {
                libraryAction.actionPerformed(null);
            }
        });
        lblHand.addMouseListener(new FMouseAdapter() {
            @Override
            public void onLeftClick(final MouseEvent e) {
                handAction.actionPerformed(null);
            }
        });
        lblFlashback.addMouseListener(new FMouseAdapter() {
            @Override
            public void onLeftClick(final MouseEvent e) {
                flashBackAction.actionPerformed(null);
            }
        });

        for (final Pair<DetailLabel, Byte> labelPair : manaLabels) {
            labelPair.getLeft().addMouseListener(new FMouseAdapter() {
                @Override
                public void onLeftClick(final MouseEvent e) {
                    //if shift key down, keep using mana until it runs out or no longer can be put towards the cost
                    final Byte mana = labelPair.getRight();
                    while (manaAction.apply(mana) && e.isShiftDown()) {}
                }
            });
        }
    }

    @SuppressWarnings("serial")
    private class DetailLabel extends FLabel {
        private final String tooltip;
        private DetailLabel(final FSkinProp icon, final String tooltip) {
            super(new FLabel.Builder().icon(FSkin.getImage(icon))
            .opaque(false).fontSize(14).hoverable()
            .fontStyle(Font.BOLD).iconInBackground()
            .fontAlign(SwingConstants.RIGHT));

            this.tooltip = tooltip;
            setFocusable(false);
        }

        public void setText(final String text0) {
            super.setText(text0);
            autoSizeFont();
        }

        public void setToolTip(final String... args) {
            super.setToolTipText(String.format(tooltip, (Object[]) args));
        }

        protected void resetIcon() {
            super.resetIcon();
            autoSizeFont();
        }

        private void autoSizeFont() {
            final String text = getText();
            if (StringUtils.isEmpty(text)) { return; }

            final Graphics g = getGraphics();
            if (g == null) { return; }

            final int max = getMaxTextWidth();

            SkinFont font = null;
            for (int fontSize = 14; fontSize > 5; fontSize--) {
                font = FSkin.getBoldFont(fontSize);
                if (font.measureTextWidth(g, text) <= max) {
                    break;
                }
            }
            if (font != null) {
                setFont(font);
            }
        }
    }
}
