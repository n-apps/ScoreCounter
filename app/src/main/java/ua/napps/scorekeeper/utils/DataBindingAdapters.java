package ua.napps.scorekeeper.utils;

import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import ua.napps.scorekeeper.counters.Counter;
import ua.napps.scorekeeper.counters.CounterActionCallback;
import ua.napps.scorekeeper.counters.CountersAdapter;
import ua.napps.scorekeeper.data.CurrentSet;

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

  @BindingAdapter({ "bind:items", "bind:callback" })
  public static void items(RecyclerView recyclerView, ObservableArrayList<Counter> newEntries,
      CounterActionCallback callback) {
    CountersAdapter rvAdapter = (CountersAdapter) recyclerView.getAdapter();

    if (rvAdapter == null) {
      CountersAdapter adapter =
          new CountersAdapter(callback, CurrentSet.getInstance().getCounters());
      if (CurrentSet.getInstance().getSize() > 5) {
        FlexboxLayoutManager layoutManager =
            new FlexboxLayoutManager(recyclerView.getContext(), FlexDirection.COLUMN,
                FlexWrap.NOWRAP);
        recyclerView.setLayoutManager(layoutManager);
      } else {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(layoutManager);
      }
      recyclerView.setAdapter(adapter);
    }

    final int size = newEntries.size();
    if (size <= 4) {
      if (((FlexboxLayoutManager) recyclerView.getLayoutManager()).getFlexWrap()
          != FlexWrap.NOWRAP) {
        FlexboxLayoutManager layoutManager =
            new FlexboxLayoutManager(recyclerView.getContext(), FlexDirection.COLUMN,
                FlexWrap.NOWRAP);
        recyclerView.setLayoutManager(layoutManager);
      }
    } else {
      if (((FlexboxLayoutManager) recyclerView.getLayoutManager()).getFlexWrap() != FlexWrap.WRAP) {
        FlexboxLayoutManager layoutManager =
            new FlexboxLayoutManager(recyclerView.getContext(), FlexDirection.ROW, FlexWrap.WRAP);
        recyclerView.setLayoutManager(layoutManager);
      }
    }
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
}
