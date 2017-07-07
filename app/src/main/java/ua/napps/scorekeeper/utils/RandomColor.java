package ua.napps.scorekeeper.utils;

import android.graphics.Color;
import java.util.Random;

public final class RandomColor {

  public RandomColor() {
  }

  public static String getContrastColor(String background) {
    final int argbColor = Color.parseColor(background);
    int r = Color.red(argbColor);
    int g = Color.green(argbColor);
    int b = Color.blue(argbColor);
    int o = ((r * 299) + (g * 587) + (b * 114)) / 1000;
    return o > 125 ? "#de000000" : "#ffffffff";
  }

  public static String getRandomColor() {
    double goldenRatioConj = (1.0 + Math.sqrt(5.0)) / 2.0;
    float saturation;
    float hue;
    long seed = new Random().nextLong();
    saturation = randFloat(0.5f, 0.9f, seed);
    hue = new Random(seed).nextFloat();
    hue += goldenRatioConj;
    hue = hue % 1;

    final int color = Color.HSVToColor(new float[] { hue * 360, saturation, 0.9f });
    return intColorToString(color);
  }

  public static String intColorToString(int color) {
    return String.format("#%06X", 0xFFFFFF & color);
  }

  private static float randFloat(float min, float max, long seed) {
    Random generator = new Random(seed);
    if (generator.nextDouble() < 0.5) {
      return (float) (((1 - generator.nextDouble()) * (max - min)) + min);
    }

    return (float) ((generator.nextDouble() * (max - min)) + min);
  }
}
