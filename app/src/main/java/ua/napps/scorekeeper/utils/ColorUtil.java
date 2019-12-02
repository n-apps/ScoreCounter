package ua.napps.scorekeeper.utils;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

public final class ColorUtil {

    public static String intColorToString(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    public static boolean isDarkBackground(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) < 0.5;
    }

    private ColorUtil() {
    }
}
