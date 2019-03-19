package ua.napps.scorekeeper.settings;

import ua.napps.scorekeeper.app.App;

public class LocalSettings {

    public static final String DARK_THEME = "dark_theme";
    public static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String LAST_SELECTED_BOTTOM_TAB = "last_selected_bottom_tab";
    private static final String LONG_PRESS_TIP_SHOWED = "long_click_tip_showed";
    private static final String SHAKE_TO_ROLL = "shake_to_roll";
    private static final String DICE_MAX_SIDE = "dice_max_side";

    private static final String CUSTOM_COUNTER_1 = "custom_counter_1";
    private static final String CUSTOM_COUNTER_2 = "custom_counter_2";
    private static final String CUSTOM_COUNTER_3 = "custom_counter_3";
    private static final String CUSTOM_COUNTER_4 = "custom_counter_4";

    public static boolean isLightTheme() {
        return !App.getTinyDB().getBoolean(DARK_THEME, true);
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


    public static int getCustomCounter(int counterId) {
        switch (counterId) {
            case 1:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_1, 5);
            case 2:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_2, 10);
            case 3:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_3, 15);
            case 4:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_4, 30);
            default:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_1, 5);
        }
    }

    public static void saveCustomCounter(int counterId, int counterValue) {
        String save_id = "";
        switch (counterId) {
            case 1:
                save_id = CUSTOM_COUNTER_1;
                break;
            case 2:
                save_id = CUSTOM_COUNTER_2;
                break;
            case 3:
                save_id = CUSTOM_COUNTER_3;
                break;
            case 4:
                save_id = CUSTOM_COUNTER_4;
                break;
        }
        if(!save_id.equals("")){
            App.getTinyDB().putInt(save_id, counterValue);
        }
    }
}
