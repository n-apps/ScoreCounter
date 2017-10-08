package ua.napps.scorekeeper.utils;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DataBindingAdapters {

  public static final String TAG = "DataBindingAdapters";

  /**
   * Prevent instantiation
   */
  private DataBindingAdapters() {
  }

  @BindingAdapter({ "bind:repeatableCallback" })
  public static void setRepeatableClick(View view, View.OnClickListener onClickListener) {
    view.setOnTouchListener(new RepeatListener(onClickListener, null));
  }

  @BindingAdapter("visibleGone") public static void showHide(View view, boolean show) {
    view.setVisibility(show ? View.VISIBLE : View.GONE);
  }

  @BindingAdapter({ "android:drawableLeft" })
  public static void setDrawableLeft(TextView view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(
        ContextCompat.getDrawable(view.getContext(), resourceId), null, null, null);
  }

  @BindingAdapter({ "android:drawableStart" })
  public static void setDrawableStart(TextView view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(
        ContextCompat.getDrawable(view.getContext(), resourceId), null, null, null);
  }

  @BindingAdapter({ "android:drawableRight" })
  public static void setDrawableRight(TextView view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(
        ContextCompat.getDrawable(view.getContext(), resourceId), null, null, null);
  }

  @BindingAdapter({ "android:drawableEnd" })
  public static void setDrawableEnd(TextView view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(null, null,
        ContextCompat.getDrawable(view.getContext(), resourceId), null);
  }

  @BindingAdapter({ "android:drawableTop" })
  public static void setDrawableTop(TextView view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(null,
        ContextCompat.getDrawable(view.getContext(), resourceId), null, null);
  }

  @BindingAdapter({ "android:drawableBottom" })
  public static void setDrawableBottom(TextView view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
        ContextCompat.getDrawable(view.getContext(), resourceId));
  }

  @BindingAdapter({ "android:drawableLeft" })
  public static void setDrawableLeft(Button view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(
        ContextCompat.getDrawable(view.getContext(), resourceId), null, null, null);
  }

  @BindingAdapter({ "android:drawableStart" })
  public static void setDrawableStart(Button view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(
        ContextCompat.getDrawable(view.getContext(), resourceId), null, null, null);
  }

  @BindingAdapter({ "android:drawableRight" })
  public static void setDrawableRight(Button view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(
        ContextCompat.getDrawable(view.getContext(), resourceId), null, null, null);
  }

  @BindingAdapter({ "android:drawableEnd" })
  public static void setDrawableEnd(Button view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(null, null,
        ContextCompat.getDrawable(view.getContext(), resourceId), null);
  }

  @BindingAdapter({ "android:drawableTop" })
  public static void setDrawableTop(Button view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(null,
        ContextCompat.getDrawable(view.getContext(), resourceId), null, null);
  }

  @BindingAdapter({ "android:drawableBottom" })
  public static void setDrawableBottom(Button view, int resourceId) {
    view.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
        ContextCompat.getDrawable(view.getContext(), resourceId));
  }

  @BindingAdapter({ "android:text" })
  public static void setTextFromInt(TextInputEditText editText, int value) {
    if (getTextAsInt(editText) != value) {
      editText.setText(String.valueOf(value));
    }
  }

  @InverseBindingAdapter(attribute = "android:text")
  public static int getTextAsInt(TextInputEditText editText) {
    try {
      return Integer.parseInt(editText.getText().toString());
    } catch (Exception e) {
      return 0;
    }
  }
}
