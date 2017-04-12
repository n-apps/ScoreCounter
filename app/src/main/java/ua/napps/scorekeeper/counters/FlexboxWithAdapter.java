package ua.napps.scorekeeper.counters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import com.google.android.flexbox.FlexboxLayout;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ItemCounterBinding;

public class FlexboxWithAdapter extends FlexboxLayout {

    private ObservableArrayList<Counter> items;
    private CounterActionCallback callback;

    public FlexboxWithAdapter(Context context) {
        super(context);
        init();
    }

    public FlexboxWithAdapter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlexboxWithAdapter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        items.addOnListChangedCallback(
                new ObservableList.OnListChangedCallback<ObservableList<Counter>>() {
                    @Override public void onChanged(ObservableList<Counter> counters) {
                        changeViews();
                    }

                    @Override
                    public void onItemRangeChanged(ObservableList<Counter> counters, int i,
                            int i1) {
                        changeViews();
                    }

                    @Override
                    public void onItemRangeInserted(ObservableList<Counter> counters, int i,
                            int i1) {
                        changeViews();
                    }

                    @Override
                    public void onItemRangeMoved(ObservableList<Counter> counters, int i, int i1,
                            int i2) {
                        changeViews();
                    }

                    @Override
                    public void onItemRangeRemoved(ObservableList<Counter> counters, int i,
                            int i1) {
                        changeViews();
                    }
                });
    }

    private void changeViews() {
        removeAllViews();
        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < items.size(); i++) {
            ItemCounterBinding binding =
                    DataBindingUtil.inflate(inflater, R.layout.item_counter, this, false);
            binding.setData(items.get(i));
            binding.setCallback(callback);
            addView(binding.getRoot());
        }
    }

    public void setItems(ObservableArrayList<Counter> items) {
        this.items = items;
        changeViews();
    }

    public void setCallback(CounterActionCallback callback) {
        this.callback = callback;
    }
}
