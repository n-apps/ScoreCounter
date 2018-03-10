package ua.napps.scorekeeper.settings;

import ua.napps.scorekeeper.app.App;
import ua.napps.scorekeeper.app.Constants;

public class LocalSettings {

    public static final String DARK_THEME = "dark_theme";
    public static final String KEEP_SCREEN_ON = "keep_screen_on";

    private static final String SHAKE_TO_ROLL = "shake_to_roll";
    private static final String DICE_MAX_SIDE = "dice_max_side";

    public static boolean isDarkTheme() {
        return App.getTinyDB().getBoolean(DARK_THEME, false);
    }

    public static void saveDarkTheme(boolean isChecked) {
        App.getTinyDB().putBoolean(DARK_THEME, isChecked);
    }

    public static boolean isKeepScreenOnEnabled() {
        return App.getTinyDB().getBoolean(KEEP_SCREEN_ON, true);
    }

    public static void saveKeepScreenOn(boolean isChecked) {
        App.getTinyDB().putBoolean(KEEP_SCREEN_ON, isChecked);
    }

    public static boolean isShakeToRollEnabled() {
        return App.getTinyDB().getBoolean(SHAKE_TO_ROLL, true);
    }

    public static void saveShakeToRoll(boolean isChecked) {
        App.getTinyDB().putBoolean(SHAKE_TO_ROLL, isChecked);
    }

    public static int getDiceMaxSide() {
        return App.getTinyDB().getInt(DICE_MAX_SIDE, 6);
    }

    public static void saveDiceMaxSide(int lastSelectedBottomTab) {
        App.getTinyDB().putInt(DICE_MAX_SIDE, lastSelectedBottomTab);
    }

    public static int getLastSelectedBottomTab() {
        return App.getTinyDB().getInt(Constants.LAST_SELECTED_BOTTOM_TAB);
    }

    public static void saveLastSelectedBottomTab(int lastSelectedBottomTab) {
        App.getTinyDB().putInt(Constants.LAST_SELECTED_BOTTOM_TAB, lastSelectedBottomTab);
    }

}
