package ua.napps.scorekeeper.counters;

import android.databinding.ObservableArrayList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.flexbox.FlexboxLayoutManager;
import java.util.List;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ItemCounterBinding;

public class CountersFlexAdapter
        extends RecyclerView.Adapter<CountersFlexAdapter.FlexCounterViewHolder> {

    private final List<Counter> items;
    private final CounterActionCallback callback;

    public CountersFlexAdapter(ObservableArrayList<Counter> counters,
            CounterActionCallback callback) {
        items = counters;
        this.callback = callback;
    }

    @Override public FlexCounterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FlexCounterViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_counter, parent, false));
    }

    @Override public void onBindViewHolder(FlexCounterViewHolder holder, int position) {
        holder.binding.setData(items.get(position));
        holder.binding.setCallback(callback);
        holder.binding.executePendingBindings();
        final View view = holder.binding.getRoot();
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof FlexboxLayoutManager.LayoutParams) {
            FlexboxLayoutManager.LayoutParams params = (FlexboxLayoutManager.LayoutParams) lp;
            params.setFlexGrow(1.0f);
            params.setMinHeight(500);
        }
    }

    @Override public int getItemCount() {
        return items.size();
    }

    static class FlexCounterViewHolder extends RecyclerView.ViewHolder {

        private final ItemCounterBinding binding;

        FlexCounterViewHolder(View itemView) {
            super(itemView);
            binding = ItemCounterBinding.bind(itemView);
        }
    }
}
