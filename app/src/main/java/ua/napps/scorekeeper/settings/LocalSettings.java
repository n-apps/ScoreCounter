package ua.napps.scorekeeper.settings;

import ua.napps.scorekeeper.app.App;

public class LocalSettings {

    public static final String DARK_THEME = "dark_theme";
    public static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String LAST_SELECTED_BOTTOM_TAB = "last_selected_bottom_tab";
    private static final String LONG_PRESS_TIP_SHOWED = "long_click_tip_showed";
    private static final String SHAKE_TO_ROLL = "shake_to_roll";
    private static final String DICE_MAX_SIDE = "dice_max_side";

    public static boolean isDarkTheme() {
        return App.getTinyDB().getBoolean(DARK_THEME, false);
    }

    public static void saveDarkTheme(boolean enabled) {
        App.getTinyDB().putBoolean(DARK_THEME, enabled);
    }

    public static boolean isKeepScreenOnEnabled() {
        return App.getTinyDB().getBoolean(KEEP_SCREEN_ON, true);
    }

    public static void saveKeepScreenOn(boolean enabled) {
        App.getTinyDB().putBoolean(KEEP_SCREEN_ON, enabled);
    }

    public static boolean isShakeToRollEnabled() {
        return App.getTinyDB().getBoolean(SHAKE_TO_ROLL, true);
    }

    public static void saveShakeToRoll(boolean enabled) {
        App.getTinyDB().putBoolean(SHAKE_TO_ROLL, enabled);
    }

    public static int getDiceMaxSide() {
        return App.getTinyDB().getInt(DICE_MAX_SIDE, 6);
    }

    public static void saveDiceMaxSide(int lastSelectedBottomTab) {
        App.getTinyDB().putInt(DICE_MAX_SIDE, lastSelectedBottomTab);
    }

    public static int getLastSelectedBottomTab() {
        return App.getTinyDB().getInt(LAST_SELECTED_BOTTOM_TAB);
    }

    public static void saveLastSelectedBottomTab(int lastSelectedBottomTab) {
        App.getTinyDB().putInt(LAST_SELECTED_BOTTOM_TAB, lastSelectedBottomTab);
    }

    public static boolean getLongPressTipShowed() {
        return App.getTinyDB().getBoolean(LONG_PRESS_TIP_SHOWED);
    }

    public static void setLongPressTipShowed() {
        App.getTinyDB().putBoolean(LONG_PRESS_TIP_SHOWED, true);
    }
}
