package ua.napps.scorekeeper.counters;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.google.android.flexbox.FlexboxLayoutManager;
import timber.log.Timber;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ItemCounterBinding;

public class CountersAdapter extends RecyclerView.Adapter<CountersAdapter.ViewHolder> {

  private final CounterActionCallback callback;
  private final ObservableArrayList<Counter> counters;
  private int height;

  public CountersAdapter(CounterActionCallback callback, ObservableArrayList<Counter> counters) {
    this.callback = callback;
    this.counters = counters;
    this.counters.addOnListChangedCallback(
        new ObservableList.OnListChangedCallback<ObservableList<Counter>>() {
          @Override public void onChanged(ObservableList<Counter> counters) {
            notifyDataSetChanged();
          }

          @Override
          public void onItemRangeChanged(ObservableList<Counter> counters, int i, int i1) {

          }

          @Override
          public void onItemRangeInserted(ObservableList<Counter> counters, int i, int i1) {
            notifyItemInserted(i);
            callback.onCounterAdded();
          }

          @Override
          public void onItemRangeMoved(ObservableList<Counter> counters, int i, int i1, int i2) {

          }

          @Override
          public void onItemRangeRemoved(ObservableList<Counter> counters, int i, int i1) {

          }
        });
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
    ItemCounterBinding binding =
        DataBindingUtil.inflate(layoutInflater, R.layout.item_counter, parent, false);
    return new ViewHolder(binding);
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {
    holder.binding.setCallback(callback);
    holder.binding.setData(counters.get(position));
    holder.binding.executePendingBindings();
    ViewGroup.LayoutParams lp = holder.binding.getRoot().getLayoutParams();
    if (lp instanceof FlexboxLayoutManager.LayoutParams) {
      FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
      flexboxLp.setFlexGrow(1.0f);
      final int itemCount = getItemCount();

      if (itemCount > 4) {
        flexboxLp.setHeight(height == 0 ? 384 : height);
      } else {
        if (itemCount == 4) {
          height = holder.binding.getRoot().getHeight();
        }
        flexboxLp.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
      }
      Timber.d("height " + height);
    }
  }

  @Override public int getItemCount() {
    return counters.size();
  }

  //public void changeItems(ObservableArrayList<Counter> counters) {
  //  this.counters = counters;
  //  notifyDataSetChanged();
  //}

  static class ViewHolder extends RecyclerView.ViewHolder {

    ItemCounterBinding binding;

    ViewHolder(ItemCounterBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
