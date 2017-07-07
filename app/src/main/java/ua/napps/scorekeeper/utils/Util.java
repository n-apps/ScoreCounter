package ua.napps.scorekeeper.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class Util {
  /**
   * Convert pixel to dp. Preserve the negative value as it's used for representing
   * MATCH_PARENT(-1) and WRAP_CONTENT(-2).
   * Ignore the round error that might happen in dividing the pixel by the density.
   *
   * @param context the context
   * @param pixel the value in pixel
   * @return the converted value in dp
   */
  public static int pixelToDp(Context context, int pixel) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    return pixel < 0 ? pixel : Math.round(pixel / displayMetrics.density);
  }

  /**
   * Convert dp to pixel. Preserve the negative value as it's used for representing
   * MATCH_PARENT(-1) and WRAP_CONTENT(-2).
   *
   * @param context the context
   * @param dp the value in dp
   * @return the converted value in pixel
   */
  public static int dpToPixel(Context context, int dp) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    return dp < 0 ? dp : Math.round(dp * displayMetrics.density);
  }
}
