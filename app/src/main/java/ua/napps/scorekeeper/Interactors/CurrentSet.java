package ua.napps.scorekeeper.Interactors;

import java.util.ArrayList;

import ua.napps.scorekeeper.Models.Counter;

/**
 * Created by novo on 10-Dec-15.
 */
public class CurrentSet {

    private ArrayList<Counter> mCounters;
    private static CurrentSet instance;

    public static CurrentSet getCurrentSet() {
        if (instance == null) {
            instance = new CurrentSet();
        }
        return instance;
    }

    private CurrentSet() {
        mCounters = new ArrayList<>();
    }

    public int getSize() {
        return mCounters.size();
    }

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
