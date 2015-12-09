package ua.napps.scorekeeper.Presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.greenrobot.event.EventBus;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.AwesomeLayout;
import ua.napps.scorekeeper.DiceDialog;
import ua.napps.scorekeeper.Events.CounterCaptionClick;
import ua.napps.scorekeeper.Events.DiceDialogClosed;
import ua.napps.scorekeeper.Events.FavoriteSetLoaded;
import ua.napps.scorekeeper.Interactors.FavoritesInteractor;
import ua.napps.scorekeeper.Interactors.FavoritesInteractorImpl;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.Models.Dice;
import ua.napps.scorekeeper.Models.FavoriteSet;
import ua.napps.scorekeeper.View.FragmentFav;
import ua.napps.scorekeeper.View.MainView;

import static ua.napps.scorekeeper.Helpers.Constants.FAV_ARRAY;
import static ua.napps.scorekeeper.Helpers.Constants.MAX_COUNTERS;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_AMOUNT;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_BONUS;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_MAX_EDGE;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_MIN_EDGE;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_SUM;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_NAME;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_SHOW_DICES;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_STAY_AWAKE;

/**
 * Created by Roman on 23/11/2015.
 */
public class MainPresenterImpl implements MainPresenter {

    Context mContext;
    MainView mView;
    ArrayList<Counter> mCounters;
    FavoritesInteractor mFavoritesInteractor;

    public MainPresenterImpl(MainView iMainView) {
        this.mView = iMainView;
        this.mContext = mView.getContext();
        this.mFavoritesInteractor = FavoritesInteractorImpl.getInstance(mContext);
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        saveSettings();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        loadSettings();
        mView.clearViews();
    }

    @Override
    public void loadSettings() {
        LogUtils.i("loadSettings");
        LogUtils.i("access to SharedPreferences");
        SharedPreferences sp = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (sp.getBoolean(PREFS_STAY_AWAKE, true))
            mView.toggleKeepScreenOn(true);
        else mView.toggleKeepScreenOn(false);

        if (sp.getBoolean(PREFS_SHOW_DICES, false)) {
            mView.toggleDicesBar(true);
            Dice.getInstance().setAmount(sp.getInt(PREFS_DICE_AMOUNT, 1));
            Dice.getInstance().setMinEdge(sp.getInt(PREFS_DICE_MIN_EDGE, 1));
            Dice.getInstance().setMaxEdge(sp.getInt(PREFS_DICE_MAX_EDGE, 6));
            Dice.getInstance().setBonus(sp.getInt(PREFS_DICE_BONUS, 0));

            mView.setDiceSum(sp.getString(PREFS_DICE_SUM, "0"));
            mView.setDiceFormula(Dice.getInstance().toString());
        } else {
            mView.toggleDicesBar(false);
        }

    }

    @Override
    public void saveSettings() {
        LogUtils.i("saveSettings");
        LogUtils.i("access to SharedPreferences");
        String json = new Gson().toJson(mFavoritesInteractor.getFavorites());
        SharedPreferences.Editor editor = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(PREFS_DICE_AMOUNT, Dice.getInstance().getAmount());
        editor.putInt(PREFS_DICE_MIN_EDGE, Dice.getInstance().getMinEdge());
        editor.putInt(PREFS_DICE_MAX_EDGE, Dice.getInstance().getMaxEdge());
        editor.putInt(PREFS_DICE_BONUS, Dice.getInstance().getBonus());
        editor.putString(FAV_ARRAY, json);
        editor.apply();
    }


    @Override
    public void onSwipe(Counter counter, int direction, boolean isSwipe) {
        LogUtils.i("onSwipe");
        counter.step(direction, isSwipe);
    }

    @Override
    public void loadFragment() {
        mView.loadFragment(new FragmentFav());
        LogUtils.i("loadFragment");
    }

    @Override
    public void showDiceDialog() {
        new DiceDialog(mContext);
        LogUtils.i("showDiceDialog");
    }

    @Override
    public void removeCounter(Counter counter) {
        LogUtils.i("removeCounter");
        mCounters.remove(counter);

        mView.removeCounter(counter);
        if (mCounters.size() < MAX_COUNTERS) mView.changeAddCounterButtonState(true);
    }

    private void addCounterView(Counter counter) {
        LogUtils.i("addCounterView");
        if (mCounters.size() == MAX_COUNTERS) mView.changeAddCounterButtonState(false);
        mView.addCounter(counter);
    }

    @Override
    public void setCounters(ArrayList<Counter> counters) {
        LogUtils.i("setCounters");
        this.mCounters = counters;
    }

    @Override
    public void addCountersFromList() {
        LogUtils.i("addCountersFromList");
        mCounters = mFavoritesInteractor.getFavSet(0).getCounters();
        for (Counter c : mCounters) addCounterView(c);
    }

    @Override
    public void addCounter() {
        LogUtils.i("addCounter");

        if (mCounters.size() >= MAX_COUNTERS) {
            mView.changeAddCounterButtonState(false);
            return;
        }

        Counter c = new Counter(getCounterCaption());
        mCounters.add(c);
        addCounterView(c);
    }

    private void loadFavSet(FavoriteSet favItem) {
        LogUtils.i("loadFavSet");

        for (Counter c : favItem.getCounters()) {
            Counter tmp = Counter.getClone(c);
            tmp.setValue(tmp.getDefValue());
            mCounters.add(tmp);
        }
        setCounters(mCounters);
        addCountersFromList();

        mFavoritesInteractor.removeFav(0);
        Collections.sort(mFavoritesInteractor.getFavorites(), new Comparator<FavoriteSet>() {
            @Override
            public int compare(FavoriteSet i1, FavoriteSet i2) {
                return i1.getLastLoaded() < i2.getLastLoaded() ? 1 : -1;
            }
        });
        mFavoritesInteractor.addFav(0, favItem);
    }


    public void onEvent(DiceDialogClosed event) {
        LogUtils.i("DiceDialogClosed event");

        if (event != null) mView.setDiceFormula(Dice.getInstance().toString());
    }

    public void onEvent(FavoriteSetLoaded event) {
        LogUtils.i("FavoriteSetLoaded event");

        if (event != null) {
            mView.closeFragment();
            mView.clearViews();
            mCounters.clear();
            loadFavSet(mFavoritesInteractor.getFavSet(event.getFavoriteSetPosition()));
            //    recentRv.setAdapter(recentAdapter);
        }
    }

    public void onEvent(CounterCaptionClick event) {
        LogUtils.i("CounterCaptionClick event");
        if (event != null && event.getCounter() != null) {
            if (mCounters.size() < 2) {
                mView.CounterCaptionClick(event.getCounter(), false);
            } else {
                mView.CounterCaptionClick(event.getCounter(), true);
            }
        }
    }

    private String getCounterCaption() {
        return String.format("%s %d", mContext.getString(R.string.counter_title_default), mCounters.size() + 1);
    }


}