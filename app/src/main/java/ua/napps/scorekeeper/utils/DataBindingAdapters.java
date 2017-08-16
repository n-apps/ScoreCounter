package ua.napps.scorekeeper.utils;

import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import ua.napps.scorekeeper.counters.Counter;
import ua.napps.scorekeeper.counters.CountersAdapter;

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

  @BindingAdapter("bind:items")
  public static void items(RecyclerView rv, ObservableArrayList<Counter> observableArrayList) {
    CountersAdapter rvAdapter = (CountersAdapter) rv.getAdapter();
    if (rvAdapter != null) {
      rvAdapter.changeItems(observableArrayList, rv.getHeight());
    }
  }
}
