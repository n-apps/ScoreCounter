package ua.napps.scorekeeper.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by novo on 2016-01-09.
 */
public final class ToastUtils {

  private static ToastUtils toastUtils;
  private Toast toast;

  private ToastUtils() {

  }

  public static synchronized ToastUtils getInstance() {
    if (toastUtils == null) {
      toastUtils = new ToastUtils();
    }
    return toastUtils;
  }

  public void showToast(Context context, String msg, int duration) {
    if (toast == null) {
      toast = Toast.makeText(context, msg, duration);
    } else {
      toast.setText(msg);
      toast.setDuration(duration);
    }
    toast.show();
  }
}