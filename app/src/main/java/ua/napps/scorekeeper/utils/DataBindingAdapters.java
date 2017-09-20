package ua.napps.scorekeeper.utils;

import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
      recyclerView.smoothScrollToPosition(size - 1);
    }
  }
}
