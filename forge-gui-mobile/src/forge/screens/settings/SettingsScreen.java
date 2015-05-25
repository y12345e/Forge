package forge.screens.settings;

import forge.Forge;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinColor.Colors;
import forge.screens.TabPageScreen;
import forge.util.Utils;

public class SettingsScreen extends TabPageScreen<SettingsScreen> {
    public static final FSkinFont DESC_FONT = FSkinFont.get(11);
    public static final FSkinColor DESC_COLOR = FSkinColor.get(Colors.CLR_TEXT).alphaColor(0.5f);
    public static final float SETTING_HEIGHT = Utils.AVG_FINGER_HEIGHT + Utils.scale(12);
    public static final float SETTING_PADDING = Utils.scale(5);
    private static final float INSETS_FACTOR = 0.025f;
    private static final float MAX_INSETS = SETTING_HEIGHT * 0.15f;

    private static SettingsScreen settingsScreen; //keep settings screen around so scroll positions maintained

    public static void show() {
        if (settingsScreen == null) {
            settingsScreen = new SettingsScreen();
        }
        Forge.openScreen(settingsScreen);
    }

    public static float getInsets(float itemWidth) {
        float insets = itemWidth * INSETS_FACTOR;
        if (insets > MAX_INSETS) {
            insets = MAX_INSETS;
        }
        return insets;
    }

    @SuppressWarnings("unchecked")
    private SettingsScreen() {
        super(new TabPage[] {
                new SettingsPage(),
                new FilesPage()
        });
    }

    @Override
    public void showMenu() {
        Forge.back(); //hide settings screen when menu button pressed
    }
}
