package ua.napps.scorekeeper.settings;

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
    private static final String SOUND_ROLL = "sound_roll";

    private static final String IS_COUNTERS_VIBRATE = "is_counters_vibrate";
    private static final String IS_SWAP_PRESS_LOGIC = "is_swap_press_logic";

    private static final String KEY_FIRST_HIT_DATE = "key_first_hit_date";
    private static final String KEY_LAUNCH_TIMES = "key_launch_times";
    private static final String KEY_DONATED = "key_donated";
    private static final String KEY_NEVER_REMINDER = "key_never_reminder";

    private static final String CUSTOM_COUNTER_1 = "custom_counter_1";
    private static final String CUSTOM_COUNTER_2 = "custom_counter_2";
    private static final String CUSTOM_COUNTER_3 = "custom_counter_3";
    private static final String CUSTOM_COUNTER_4 = "custom_counter_4";

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
        return App.getTinyDB().getLong(KEY_FIRST_HIT_DATE, -1L);
    }

    public static void saveFirstHitDate(long hitCount) {
        App.getTinyDB().putLong(KEY_FIRST_HIT_DATE, hitCount);
    }

    public static boolean didNeverReminder() {
        return App.getTinyDB().getBoolean(KEY_NEVER_REMINDER, false);
    }

    public static void neverReminder() {
        App.getTinyDB().putBoolean(KEY_NEVER_REMINDER, true);
    }

    public static int getCustomCounter(int counterId) {
        switch (counterId) {
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
        if (!save_id.equals("")) {
            App.getTinyDB().putInt(save_id, counterValue);
        }
    }

    public static int getDiceCount() {
        return App.getTinyDB().getInt(DICE_COUNT, 1);
    }

    public static void saveDiceCount(int diceCount) {
        App.getTinyDB().putInt(DICE_COUNT, diceCount);
    }

    public static boolean isCountersVibrate() {
        return App.getTinyDB().getBoolean(IS_COUNTERS_VIBRATE, true);
    }

    public static void saveCountersVibrate(boolean enabled) {
        App.getTinyDB().putBoolean(IS_COUNTERS_VIBRATE, enabled);
    }

    public static void saveSwapPressLogic(boolean incrementByOneTap) {
        App.getTinyDB().putBoolean(IS_SWAP_PRESS_LOGIC, incrementByOneTap);
    }

    public static boolean isSwapPressLogicEnabled() {
        return App.getTinyDB().getBoolean(IS_SWAP_PRESS_LOGIC, false);
    }
}
