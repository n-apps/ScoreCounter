package ua.napps.scorekeeper.utils;

import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.support.v7.widget.RecyclerView;
import ua.napps.scorekeeper.counters.Counter;
import ua.napps.scorekeeper.counters.CounterActionCallback;
import ua.napps.scorekeeper.counters.CountersFlexAdapter;

public class DataBindingAdapters {

    public static final String TAG = "DataBindingAdapters";

    /**
     * Prevent instantiation
     */
    private DataBindingAdapters() {
    }

    @BindingAdapter({ "items", "callback" })
    public static void setCounters(RecyclerView recyclerView,
            ObservableArrayList<Counter> arrayList, CounterActionCallback callback) {
        if (arrayList != null && arrayList.size() != 0) {
            CountersFlexAdapter adapter = new CountersFlexAdapter(arrayList, callback);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}
