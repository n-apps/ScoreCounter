package ua.napps.scorekeeper.settings;

import ua.napps.scorekeeper.app.App;

public class LocalSettings {

    public static final String DARK_THEME = "dark_theme";
    public static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String LONG_PRESS_TIP_SHOWED = "long_click_tip_showed";
    private static final String SHAKE_TO_ROLL = "shake_to_roll";
    private static final String DICE_MAX_SIDE = "dice_max_side";
    private static final String DICE_COUNT = "dice_count";
    private static final String SOUND_ROLL = "sound_roll";
    private static final String KEY_WAS_RATED = "KEY_WAS_RATED";
    private static final String KEY_NEVER_REMINDER = "KEY_NEVER_REMINDER";
    private static final String IS_LOWEST_SCORE_WINS = "is_lowest_score_wins";
    private static final String IS_COUNTERS_VIBRATE = "is_counters_vibrate";
    private static final String NUMBER_OF_FULL_COUNTERS = "number_of_full_counters";

    private static final String CUSTOM_COUNTER_1 = "custom_counter_1";
    private static final String CUSTOM_COUNTER_2 = "custom_counter_2";
    private static final String CUSTOM_COUNTER_3 = "custom_counter_3";
    private static final String CUSTOM_COUNTER_4 = "custom_counter_4";

    public static boolean isLightTheme() {
        return !App.getTinyDB().getBoolean(DARK_THEME, false);
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

    public static void markRateApp() {
        App.getTinyDB().putBoolean(KEY_WAS_RATED, true);
    }

    public static boolean didRate() {
        return App.getTinyDB().getBoolean(KEY_WAS_RATED, false);
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

    public static boolean isLowestScoreWins() {
        return App.getTinyDB().getBoolean(IS_LOWEST_SCORE_WINS, false);
    }

    public static void saveLowestScoreWins(boolean enabled) {
        App.getTinyDB().putBoolean(IS_LOWEST_SCORE_WINS, enabled);
    }

    public static boolean isCountersVibrate() {
        return App.getTinyDB().getBoolean(IS_COUNTERS_VIBRATE, true);
    }

    public static void saveCountersVibrate(boolean enabled) {
        App.getTinyDB().putBoolean(IS_COUNTERS_VIBRATE, enabled);
    }

    public static int getNumberOfFullCounters() {
        return App.getTinyDB().getInt(NUMBER_OF_FULL_COUNTERS, 5);
    }

    public static void saveNumberOfFullCounters(int numberOfFullCounters) {
        App.getTinyDB().putInt(NUMBER_OF_FULL_COUNTERS, numberOfFullCounters);
    }
}
