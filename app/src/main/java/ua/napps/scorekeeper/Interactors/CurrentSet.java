package ua.napps.scorekeeper.Interactors;

import com.apkfuns.logutils.LogUtils;

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
            LogUtils.i("new CurrentSet instance");
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
        LogUtils.i(String.format("setCounters: %d", mCounters.size()));
    }

    public ArrayList<Counter> getCounters() {
        LogUtils.i(String.format("getCounters: counters size: %d", mCounters.size()));
        return mCounters;
    }

    public void removeCounter(Counter item) {
        mCounters.remove(item);
    }

    public void addCounter(Counter item) {
        mCounters.add(item);
    }

}
