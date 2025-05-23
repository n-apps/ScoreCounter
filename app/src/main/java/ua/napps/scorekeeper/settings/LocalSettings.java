package ua.napps.scorekeeper.settings;

import androidx.annotation.IntRange;

import ua.napps.scorekeeper.app.App;

public class LocalSettings {

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    public static final String APP_THEME_MODE = "app_theme_mode";
    public static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String LONG_PRESS_TIP_SHOWED = "long_click_tip_showed";
    private static final String SHAKE_TO_ROLL = "shake_to_roll";
    private static final String DICE_MAX_SIDE = "dice_max_side";
    private static final String DICE_COUNT = "dice_count";
    private static final String DICE_ANIMATE = "dice_animate";
    private static final String SOUND_ROLL = "sound_roll";

    private static final String IS_AUTOSORT_ENABLED = "is_autosort_enabled";
    private static final String IS_AUTO_SORT_DESC = "is_auto_sort_desc";

    public static final String IS_COUNTERS_VIBRATE = "is_counters_vibrate";
    public static final String IS_SWAP_PRESS_LOGIC = "is_swap_press_logic";

    private static final String KEY_FIRST_HIT_DATE = "key_first_hit_date";
    private static final String KEY_LAUNCH_TIMES = "key_launch_times";
    private static final String KEY_DONATED = "key_donated";

    public static final String CUSTOM_COUNTER_1 = "custom_counter_1";
    public static final String CUSTOM_COUNTER_2 = "custom_counter_2";
    public static final String CUSTOM_COUNTER_3 = "custom_counter_3";
    public static final String CUSTOM_COUNTER_4 = "custom_counter_4";
    public static final String CUSTOM_COUNTER_5 = "custom_counter_5";
    public static final String CUSTOM_COUNTER_6 = "custom_counter_6";
    public static final String CUSTOM_COUNTER_7 = "custom_counter_7";

    public static boolean isRelevantToCounters(String key) {
        return IS_SWAP_PRESS_LOGIC.equals(key) ||
                IS_COUNTERS_VIBRATE.equals(key) ||
                IS_AUTOSORT_ENABLED.equals(key) ||
                IS_AUTO_SORT_DESC.equals(key) ||
                CUSTOM_COUNTER_1.equals(key) ||
                CUSTOM_COUNTER_2.equals(key) ||
                CUSTOM_COUNTER_3.equals(key) ||
                CUSTOM_COUNTER_4.equals(key) ||
                CUSTOM_COUNTER_5.equals(key) ||
                CUSTOM_COUNTER_6.equals(key) ||
                CUSTOM_COUNTER_7.equals(key);
    }

    public static int getDefaultTheme() {
        return App.getTinyDB().getInt(APP_THEME_MODE, THEME_SYSTEM);
    }

    public static void saveDefaultTheme(int theme) {
        App.getTinyDB().putInt(APP_THEME_MODE, theme);
    }

    public static boolean isKeepScreenOnEnabled() {
        return App.getTinyDB().getBoolean(KEEP_SCREEN_ON, true);
    }

    public static void saveKeepScreenOn(boolean enabled) {
        App.getTinyDB().putBoolean(KEEP_SCREEN_ON, enabled);
    }

    public static void saveSoundRoll(boolean enabled) {
        App.getTinyDB().putBoolean(SOUND_ROLL, enabled);
    }

    public static boolean isSoundRollEnabled() {
        return App.getTinyDB().getBoolean(SOUND_ROLL, true);
    }

    public static boolean isShakeToRollEnabled() {
        return App.getTinyDB().getBoolean(SHAKE_TO_ROLL, false);
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

    public static boolean getLongPressTipShowed() {
        return App.getTinyDB().getBoolean(LONG_PRESS_TIP_SHOWED);
    }

    public static void setLongPressTipShowed() {
        App.getTinyDB().putBoolean(LONG_PRESS_TIP_SHOWED, true);
    }

    public static void markDonated() {
        App.getTinyDB().putBoolean(KEY_DONATED, true);
    }

    public static boolean didDonate() {
        return App.getTinyDB().getBoolean(KEY_DONATED, false);
    }

    public static int getAppLaunchTimes() {
        return App.getTinyDB().getInt(KEY_LAUNCH_TIMES, 0);
    }

    public static void saveAppLaunchTimes(int hitCount) {
        App.getTinyDB().putInt(KEY_LAUNCH_TIMES, hitCount);
    }

    public static long getFirstHitDate() {
        long firstLaunch = App.getTinyDB().getLong(KEY_FIRST_HIT_DATE, 0);
        if (firstLaunch == 0) {
            firstLaunch = System.currentTimeMillis();
            App.getTinyDB().putLong(KEY_FIRST_HIT_DATE, firstLaunch);
        }
        return firstLaunch;
    }

    public static int getCustomCounter(@IntRange(from = 1, to = 7) int counterId) {
        switch (counterId) {
            case 2:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_2, 10);
            case 3:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_3, 15);
            case 4:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_4, 20);
            case 5:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_5, 50);
            case 6:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_6, 100);
            case 7:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_7, 200);
            default:
                return App.getTinyDB().getInt(CUSTOM_COUNTER_1, 5);
        }
    }

    public static void saveCustomCounter(@IntRange(from = 1, to = 7) int counterId, int counterValue) {
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
            case 5:
                save_id = CUSTOM_COUNTER_5;
                break;
            case 6:
                save_id = CUSTOM_COUNTER_6;
                break;
            case 7:
                save_id = CUSTOM_COUNTER_7;
                break;
        }
        if (!save_id.isEmpty()) {
            App.getTinyDB().putInt(save_id, counterValue);
        }
    }

    public static int getDiceCount() {
        return App.getTinyDB().getInt(DICE_COUNT, 1);
    }

    public static void saveDiceCount(int diceCount) {
        App.getTinyDB().putInt(DICE_COUNT, diceCount);
    }

    public static boolean isDiceAnimated() {
        return App.getTinyDB().getBoolean(DICE_ANIMATE, true);
    }

    public static void saveDiceAnimate(boolean enabled) {
        App.getTinyDB().putBoolean(DICE_ANIMATE, enabled);
    }

    public static boolean isCountersVibrate() {
        return App.getTinyDB().getBoolean(IS_COUNTERS_VIBRATE, true);
    }

    public static void saveCountersVibrate(boolean enabled) {
        App.getTinyDB().putBoolean(IS_COUNTERS_VIBRATE, enabled);
    }

    public static boolean isAutoSortEnabled() {
        return App.getTinyDB().getBoolean(IS_AUTOSORT_ENABLED, false);
    }

    public static void saveAutoSortEnabled(boolean enabled) {
        App.getTinyDB().putBoolean(IS_AUTOSORT_ENABLED, enabled);
    }

    public static boolean isAutoSortDescending() {
        return App.getTinyDB().getBoolean(IS_AUTO_SORT_DESC, true);
    }

    public static void saveAutoSortDescending(boolean enabled) {
        App.getTinyDB().putBoolean(IS_AUTO_SORT_DESC, enabled);
    }

    public static void saveSwapPressLogic(boolean incrementByOneTap) {
        App.getTinyDB().putBoolean(IS_SWAP_PRESS_LOGIC, incrementByOneTap);
    }

    public static boolean isSwapPressLogicEnabled() {
        return App.getTinyDB().getBoolean(IS_SWAP_PRESS_LOGIC, false);
    }
}
