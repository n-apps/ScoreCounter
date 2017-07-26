package ua.napps.scorekeeper.utils;

import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;

public class DataBindingAdapters {

  public static final String TAG = "DataBindingAdapters";

  /**
   * Prevent instantiation
   */
  private DataBindingAdapters() {
  }

  @BindingAdapter({ "app:srcCompat", "bind:colorTint" })
  public static void setColorTint(View view, @DrawableRes int drawableId, String color) {
    Drawable drawable = AppCompatResources.getDrawable(view.getContext(), drawableId);
    DrawableCompat.setTint(drawable, Color.parseColor(color));
  }

  @BindingAdapter({ "bind:repeatableCallback" })
  public static void setRepeatableClick(View view, View.OnClickListener onClickListener) {
    view.setOnTouchListener(new RepeatListener(onClickListener, null));
  }
}
