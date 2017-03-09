package ua.napps.scorekeeper.data;

import android.databinding.ObservableArrayList;
import android.support.annotation.NonNull;
import java.util.ArrayList;
import ua.napps.scorekeeper.counters.Counter;

public final class CurrentSet {

    private ObservableArrayList<Counter> counters;
    private static CurrentSet sCurrentSet;

    public synchronized static CurrentSet getInstance() {
        if (sCurrentSet == null) {
            sCurrentSet = new CurrentSet();
        }
        return sCurrentSet;
    }

    private CurrentSet() {
        counters = new ObservableArrayList<>();
    }

    public int getSize() {
        return counters.size();
    }

    public void setCounters(@NonNull ObservableArrayList<Counter> counters) {
        this.counters = counters;
    }

    public void setCounters(@NonNull ArrayList<Counter> counters) {
        this.counters.addAll(counters);
    }

    public Counter getCounter(int position) {
        return counters.get(position);
    }

    public ObservableArrayList<Counter> getCounters() {
        return counters;
    }

    public void removeCounter(Counter item) {
        counters.remove(item);
    }

    public void addCounter(String caption) {
        counters.add(new Counter(caption));
    }

    public void removeAllCounters() {
        counters.clear();
    }
}
