package ua.napps.scorekeeper.Interactors;

import com.apkfuns.logutils.LogUtils;

import java.util.ArrayList;

import ua.napps.scorekeeper.Models.Counter;

/**
 * Created by novo on 10-Dec-15.
 */
public class CurrentSetInteractor {

    private ArrayList<Counter> mCounters;
    private static CurrentSetInteractor instance;

    public static CurrentSetInteractor getInstance() {
        if (instance == null) {
            instance = new CurrentSetInteractor();
            LogUtils.i("new CurrentSetInteractor instance");
        }
        return instance;
    }

    public void setCounters(ArrayList counters) {
        this.mCounters = counters;
        LogUtils.i(String.format("setCounters: %d", counters.size()));
    }

    public ArrayList<Counter> getCounters() {
        LogUtils.i(String.format("getCounters: counters size: %d", mCounters.size()));
        return mCounters;
    }

}
