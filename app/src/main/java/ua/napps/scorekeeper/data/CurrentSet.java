package ua.napps.scorekeeper.data;

import android.databinding.ObservableArrayList;
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

    @SuppressWarnings("unchecked") public void setCounters(ObservableArrayList counters) {
        this.counters = counters;
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

    public void addCounter(Counter item) {
        counters.add(item);
    }

    public void removeAllCounters() {
        counters.clear();
    }

    public void removeCounter(Object o) {
        if (o == null) return;

        counters.remove((Counter) o);
    }
}
