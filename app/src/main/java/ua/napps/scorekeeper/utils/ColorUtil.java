package ua.napps.scorekeeper.utils;

import android.graphics.Color;

public final class ColorUtil {

    public static String intColorToString(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    public static boolean isDarkBackground(String background) {
        final int argbColor = Color.parseColor(background);
        int r = Color.red(argbColor);
        int g = Color.green(argbColor);
        int b = Color.blue(argbColor);
        int o = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return o < 125;
    }

    public ColorUtil() {
    }
}
