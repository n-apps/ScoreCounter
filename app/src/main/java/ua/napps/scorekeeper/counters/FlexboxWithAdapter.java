package ua.napps.scorekeeper.counters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
import android.databinding.adapters.ListenerUtil;
import android.os.Build;
import android.support.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;
import java.util.List;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ItemCounterBinding;

public class FlexboxWithAdapter extends FlexboxLayout {

  private static final String TAG = "FlexboxWithAdapter";

  private CounterActionCallback callback;
  private List<Counter> counters;

  public FlexboxWithAdapter(Context context) {
    super(context);
  }

  public FlexboxWithAdapter(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FlexboxWithAdapter(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setEntries(List<Counter> newEntries) {
    if (counters == newEntries) {
      return;
    }
    counters = newEntries;

    EntryChangeListener listener = ListenerUtil.getListener(this, R.id.entryListener);
    if (listener != null && counters instanceof ObservableList) {
      ((ObservableList) counters).removeOnListChangedCallback(listener);
    }

    if (newEntries == null) {
      removeAllViews();
    } else {
      if (newEntries instanceof ObservableList) {
        if (listener == null) {
          listener = new EntryChangeListener(this);
          ListenerUtil.trackListener(this, listener, R.id.entryListener);
        }
        ((ObservableList) newEntries).addOnListChangedCallback(listener);
      }
      resetViews();
    }
  }

  public void setCallback(CounterActionCallback callback) {
    this.callback = callback;
  }

  private void startTransition(ViewGroup root) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      TransitionManager.beginDelayedTransition(root);
    }
  }

  private void resetViews() {
    removeAllViews();

    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    for (int i = 0; i < counters.size(); i++) {
      ItemCounterBinding binding = bindLayout(inflater, counters.get(i), callback);
      final View root = binding.getRoot();
      ViewGroup.LayoutParams lp = root.getLayoutParams();
      if (lp instanceof FlexboxLayoutManager.LayoutParams) {
        FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
        flexboxLp.setFlexGrow(1.0f);
      }
      addView(root);
    }
  }

  private ItemCounterBinding bindLayout(LayoutInflater inflater, Object counter,
      CounterActionCallback callback) {
    ItemCounterBinding binding =
        DataBindingUtil.inflate(inflater, R.layout.item_counter, this, false);
    if (!binding.setVariable(ua.com.napps.scorekeeper.BR.data, counter)) {
      String layoutName = getResources().getResourceEntryName(R.layout.item_counter);
      Log.w(TAG, "There is no variable 'data' in layout " + layoutName);
    }
    if (!binding.setVariable(ua.com.napps.scorekeeper.BR.callback, callback)) {
      String layoutName = getResources().getResourceEntryName(R.layout.item_counter);
      Log.w(TAG, "There is no variable 'callback' in layout " + layoutName);
    }
    return binding;
  }

  private class EntryChangeListener extends ObservableList.OnListChangedCallback {
    private final ViewGroup mTarget;

    EntryChangeListener(ViewGroup target) {
      mTarget = target;
    }

    @Override public void onChanged(ObservableList observableList) {
      resetViews();
    }

    @Override public void onItemRangeChanged(ObservableList observableList, int start, int count) {
      LayoutInflater inflater =
          (LayoutInflater) mTarget.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      startTransition(mTarget);
      final int end = start + count;
      for (int i = start; i < end; i++) {
        Object data = observableList.get(i);
        ViewDataBinding binding = bindLayout(inflater, data, callback);
        binding.setVariable(ua.com.napps.scorekeeper.BR.data, observableList.get(i));
        mTarget.removeViewAt(i);
        mTarget.addView(binding.getRoot(), i);
      }
    }

    @Override public void onItemRangeInserted(ObservableList observableList, int start, int count) {
      startTransition(mTarget);
      final int end = start + count;
      LayoutInflater inflater =
          (LayoutInflater) mTarget.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      for (int i = end - 1; i >= start; i--) {
        Object entry = observableList.get(i);
        ViewDataBinding binding = bindLayout(inflater, entry, callback);
        final View view = binding.getRoot();
        mTarget.addView(view, start);
        callback.onCounterAdded(view);
      }
    }

    @Override
    public void onItemRangeMoved(ObservableList observableList, int from, int to, int count) {
      startTransition(mTarget);
      for (int i = 0; i < count; i++) {
        View view = mTarget.getChildAt(from);
        mTarget.removeViewAt(from);
        int destination = (from > to) ? to + i : to;
        mTarget.addView(view, destination);
        callback.onCounterAdded(view);
      }
    }

    @Override public void onItemRangeRemoved(ObservableList observableList, int start, int count) {
      startTransition(mTarget);
      for (int i = 0; i < count; i++) {
        mTarget.removeViewAt(start);
      }
    }
  }
}
