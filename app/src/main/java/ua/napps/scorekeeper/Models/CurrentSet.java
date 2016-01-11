package ua.napps.scorekeeper.Models;

import java.util.ArrayList;

/**
 * Created by novo on 10-Dec-15.
 */
public final class CurrentSet {

    private ArrayList<Counter> mCounters;
    private static CurrentSet sCurrentSet;

    public synchronized static CurrentSet getInstance() {
        if (sCurrentSet == null) {
            sCurrentSet = new CurrentSet();
        }
        return sCurrentSet;
    }

    private CurrentSet() {
        mCounters = new ArrayList<>();
    }

    public int getSize() {
        return mCounters.size();
    }

    @SuppressWarnings("unchecked")
    public void setCounters(ArrayList counters) {
        this.mCounters = counters;
    }

    public Counter getCounter(int position) {
        return mCounters.get(position);
    }

    public ArrayList<Counter> getCounters() {
        return mCounters;
    }

    public void removeCounter(Counter item) {
        mCounters.remove(item);
    }

    public void addCounter(Counter item) {
        mCounters.add(item);
    }

    public void removeAllCounters() {
        mCounters.clear();
    }

}
