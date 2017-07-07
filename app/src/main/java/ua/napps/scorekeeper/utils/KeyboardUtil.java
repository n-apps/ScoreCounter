package ua.napps.scorekeeper.utils;

/**
 * Created by Roman on 23/11/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

@SuppressWarnings("unused") public final class KeyboardUtil {
  private KeyboardUtil() {
  }

  public static void showKeyboard(Activity activity, View view) {
    if (activity != null) {
      if (view != null) {
        view.requestFocus();
      }
      InputMethodManager imm =
          (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      if (imm != null) {
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
      }
    }
  }

  public static void hideKeyboard(Activity activity) {
    if (activity != null) {
      InputMethodManager imm =
          (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      if (imm != null && activity.getCurrentFocus() != null) {
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        activity.getCurrentFocus().clearFocus();
      }
    }
  }

  public static void hideKeyboard(Activity activity, View view) {
    if (activity != null) {
      if (view != null) {
        InputMethodManager imm =
            (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
          imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
      } else {
        hideKeyboard(activity);
      }
    }
  }
}