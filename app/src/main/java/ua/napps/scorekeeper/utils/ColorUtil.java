package ua.napps.scorekeeper.utils;

import android.graphics.Color;
import java.util.Random;

/**
 * Created by novo on 2016-01-02.
 */
public final class ColorUtil {

    private ColorUtil() {
    }

    public static int getContrastColor(int background) {
        int r = Color.red(background);
        int g = Color.green(background);
        int b = Color.blue(background);
        int o = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return o > 125 ?  Color.parseColor("#de000000") : Color.WHITE;
    }

    public static int getRandomColor() {
        double goldenRatioConj = (1.0 + Math.sqrt(5.0)) / 2.0;
        float saturation;
        float hue;
        long seed = new Random().nextLong();
        saturation = randFloat(0.5f, 0.7f, seed);
        hue = new Random(seed).nextFloat();
        hue += goldenRatioConj;
        hue = hue % 1;

        return Color.HSVToColor(new float[] {hue * 360, saturation, 0.9f});
    }

    private static float randFloat(float min, float max, long seed) {
        Random generator = new Random(seed);
        if (generator.nextDouble() < 0.5) {
            return (float) (((1 - generator.nextDouble()) * (max - min)) + min);
        }

        return (float) ((generator.nextDouble() * (max - min)) + min);
    }
}
