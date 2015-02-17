package forge.screens.match.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import forge.menus.MenuUtil;
import forge.screens.match.controllers.CDev;

/**
 * Gets a menu that replicates all the DevMode options.
 * <p>
 * Simply calls the associated method in CDev.
 */
public class DevModeMenu implements ActionListener {

    private CDev controller;
    public DevModeMenu(final CDev controller) {
        this.controller = controller;
    };

    // Using an enum to avoid having to create multiple
    // ActionListeners each calling a single method.
    private enum DevMenuItem {
        GENERATE_MANA("Generate Mana"),
        TUTOR_FOR_CARD("Tutor for Card"),
        ADD_CARD_TO_HAND("Add Card to Hand"),
        ADD_CARD_TO_PLAY("Add Card to Play"),
        SET_PLAYER_LIFE("Set Player Life"),
        WIN_GAME("Win Game"),
        SETUP_GAME_STATE("Setup Game State"),
        DUMP_GAME_STATE("Dump Game State"),
        PLAY_UNLIMITED_LANDS("Play Unlimited Lands"),
        VIEW_ALL("View All Cards"),
        ADD_COUNTER("Add Counters to Permanent"),
        TAP_PERMANENT("Tap Permanents"),
        UNTAP_PERMANENT("Untap Permanents"),
        RIGGED_PLANAR_ROLL("Rigged Planar Roll"),
        PLANESWALK_TO("Planeswalk to"),
        DEV_CORNER("Developer's Corner");

        protected String caption;
        private DevMenuItem(final String value) {
            this.caption = value;
        }
        protected static DevMenuItem getValue(final String s) {
            for (final DevMenuItem t : DevMenuItem.values()) {
                if (t.caption == s) {
                    return t;
                }
            }
            return null;
        }
    };

    public JMenu getMenu() {
        final boolean hasController = controller.getController() != null;
        JMenu menu = new JMenu("Dev");
        menu.setMnemonic(KeyEvent.VK_D);
        menu.add(getMenuItem(DevMenuItem.GENERATE_MANA));
        menu.add(getMenuItem(DevMenuItem.TUTOR_FOR_CARD));
        menu.addSeparator();
        menu.add(getMenuItem(DevMenuItem.ADD_CARD_TO_HAND));
        menu.add(getMenuItem(DevMenuItem.ADD_CARD_TO_PLAY));
        menu.addSeparator();
        menu.add(getMenuItem(DevMenuItem.SET_PLAYER_LIFE));
        menu.add(getMenuItem(DevMenuItem.WIN_GAME));
        menu.addSeparator();
        menu.add(getMenuItem(DevMenuItem.SETUP_GAME_STATE));
        menu.add(getMenuItem(DevMenuItem.DUMP_GAME_STATE));
        menu.addSeparator();
        menu.add(getCheckboxMenuItem(DevMenuItem.PLAY_UNLIMITED_LANDS, hasController && controller.getController().canPlayUnlimitedLands()));
        menu.add(getCheckboxMenuItem(DevMenuItem.VIEW_ALL, hasController && controller.getController().mayLookAtAllCards()));
        menu.add(getMenuItem(DevMenuItem.ADD_COUNTER));
        menu.addSeparator();
        menu.add(getMenuItem(DevMenuItem.TAP_PERMANENT));
        menu.add(getMenuItem(DevMenuItem.UNTAP_PERMANENT));
        menu.addSeparator();
        menu.add(getMenuItem(DevMenuItem.RIGGED_PLANAR_ROLL));
        menu.add(getMenuItem(DevMenuItem.PLANESWALK_TO));
        menu.addSeparator();
        menu.add(getMenuItem(DevMenuItem.DEV_CORNER));
        return menu;
    }

    private JMenuItem getMenuItem(final DevMenuItem m) {
        JMenuItem menuItem = new JMenuItem(m.caption);
        menuItem.addActionListener(this);
        return menuItem;
    }

    private JCheckBoxMenuItem getCheckboxMenuItem(final DevMenuItem m, final boolean isSelected) {
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(m.caption);
        menuItem.setState(isSelected);
        menuItem.addActionListener(this);
        return menuItem;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        switch (DevMenuItem.getValue(e.getActionCommand())) {
        case GENERATE_MANA:        { controller.generateMana(); break; }
        case TUTOR_FOR_CARD:       { controller.tutorForCard(); break; }
        case ADD_CARD_TO_HAND:     { controller.addCardToHand(); break; }
        case ADD_CARD_TO_PLAY:     { controller.addCardToBattlefield(); break; }
        case SET_PLAYER_LIFE:      { controller.setPlayerLife(); break; }
        case WIN_GAME:             { controller.winGame(); break; }
        case SETUP_GAME_STATE:     { controller.setupGameState(); break; }
        case DUMP_GAME_STATE:      { controller.dumpGameState(); break; }
        case PLAY_UNLIMITED_LANDS: { controller.togglePlayManyLandsPerTurn(); break; }
        case VIEW_ALL:             { controller.toggleViewAllCards(); break; }
        case ADD_COUNTER:          { controller.addCounterToPermanent(); break; }
        case TAP_PERMANENT:        { controller.tapPermanent(); break; }
        case UNTAP_PERMANENT:      { controller.untapPermanent(); break; }
        case RIGGED_PLANAR_ROLL:   { controller.riggedPlanerRoll(); break; }
        case PLANESWALK_TO:        { controller.planeswalkTo(); break; }
        case DEV_CORNER:           { openDevForumInBrowser(); break; }
        default:
            break;
        }
    }

    private static void openDevForumInBrowser() {
        MenuUtil.openUrlInBrowser("http://www.slightlymagic.net/forum/viewforum.php?f=52");
    }

}
