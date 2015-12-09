package ua.napps.scorekeeper.Helpers;

import android.view.ViewConfiguration;

public final class Constants {
    public static final String PREFS_NAME = "prefs";
    public static final String PREFS_SHOW_DICES = "show_dices";
    public static final String PREFS_STAY_AWAKE = "stay_awake";
    public static final String PREFS_DICE_AMOUNT = "dice_amount";
    public static final String PREFS_DICE_MIN_EDGE = "dice_min_edge";
    public static final String PREFS_DICE_MAX_EDGE = "dice_max_edge";
    public static final String PREFS_DICE_BONUS = "dice_bonus";
    public static final String SEND_REPORT_EMAIL = "roman.novodvorskiy@gmail.com";
    public static final int MAX_COUNTERS = 8;
    public static final String FAV_ARRAY = "fav_array";
    public static final String PREFS_DICE_SUM = "dice_sum";
    public static final int RECENT_LIST_SIZE = 4;
    public static final int[] colsArr = {1, 1, 1, 2, 2, 2, 2, 2};
    public static final int[] rowsArr = {1, 2, 3, 2, 3, 3, 4, 4};
    public static final int LEFT = -1;
    public static final int RIGHT = 1;
    public static final int SWIPE_STEP = 1;
    public static final int SWIPE_THRESHOLD = 16;
    public static final long LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    public static final float CAPTION_TEXT_RATIO = 0.07f;
    public static final float PLUS_MINUS_RATIO = CAPTION_TEXT_RATIO;
    public static final float VALUE_TEXT_RATIO = 0.23f;
    public static final int PREV_VALUE_SHOW_DURATION = 1000;
    public static final float MINUS_SYMBOL_SCALE = 0.5f;
}
