package ua.napps.scorekeeper.Utils;

import android.graphics.Color;

import com.github.lzyzsd.randomcolor.RandomColor;

/**
 * Created by novo on 2016-01-02.
 */
public class ColorUtil {

    private ColorUtil() {
    }
    public static int getContrastColor(int background) {
        int r = Color.red(background);
        int g = Color.green(background);
        int b = Color.blue(background);
        int o = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return o > 125 ? Color.BLACK : Color.WHITE;
    }

    public static int getRandomColor() {
        RandomColor randomColor = new RandomColor();
        int color = randomColor.randomColor();
        return color;
    }
}
