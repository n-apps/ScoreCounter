package ua.napps.scorekeeper.Models;

import java.util.ArrayList;
import java.util.Iterator;

public final class CurrentSet {

    private ArrayList<Counter> counters;
    private static CurrentSet sCurrentSet;

    public synchronized static CurrentSet getInstance() {
        if (sCurrentSet == null) {
            sCurrentSet = new CurrentSet();
        }
        return sCurrentSet;
    }

    private CurrentSet() {
        counters = new ArrayList<>();
    }

    public int getSize() {
        return counters.size();
    }

    @SuppressWarnings("unchecked")
    public void setCounters(ArrayList counters) {
        this.counters = counters;
    }

    public Counter getCounter(int position) {
        return counters.get(position);
    }

    public ArrayList<Counter> getCounters() {
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

        Iterator<Counter> iterator;
        iterator = counters.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(o)) {
                iterator.remove();
                return;
            }
        }
    }
}
