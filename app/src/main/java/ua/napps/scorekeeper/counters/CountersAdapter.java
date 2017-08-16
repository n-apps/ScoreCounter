package ua.napps.scorekeeper.counters;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.google.android.flexbox.FlexboxLayoutManager;
import java.util.List;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ItemCounterBinding;

public class CountersAdapter extends RecyclerView.Adapter<CountersAdapter.ViewHolder> {

  private final CounterActionCallback callback;
  private List<Counter> counters;
  private int height;
  private final int minHeight;

  public CountersAdapter(CounterActionCallback callback, int minHeight) {
    this.callback = callback;
    this.minHeight = minHeight;
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
      flexboxLp.setMinHeight(height / getItemCount());
    }
  }

  @Override public int getItemCount() {
    return counters.size();
  }

  public void changeItems(ObservableArrayList<Counter> list, int height) {
    this.counters = list;
    this.height = height;
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    ItemCounterBinding binding;

    ViewHolder(ItemCounterBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
