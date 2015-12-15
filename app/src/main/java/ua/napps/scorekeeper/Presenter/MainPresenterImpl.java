package ua.napps.scorekeeper.Presenter;

import android.content.Context;
import android.content.SharedPreferences;

import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.DiceDialog;
import ua.napps.scorekeeper.Events.CounterCaptionClick;
import ua.napps.scorekeeper.Events.FavoriteSetLoaded;
import ua.napps.scorekeeper.Events.FavoritesUpdated;
import ua.napps.scorekeeper.Helpers.Constants;
import ua.napps.scorekeeper.Interactors.CurrentSetInteractor;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.Interactors.Dice;
import ua.napps.scorekeeper.View.FragmentFav;
import ua.napps.scorekeeper.View.MainView;

import static ua.napps.scorekeeper.Helpers.Constants.ACTIVE_COUNTERS;
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

    public MainPresenterImpl(MainView iMainView) {
        this.mView = iMainView;
        this.mContext = mView.getContext();
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
        String activeCountersJson = sp.getString(Constants.ACTIVE_COUNTERS, "");
        Type listType = new TypeToken<ArrayList<Counter>>() {
        }.getType();
        ArrayList<Counter> counters = new Gson().fromJson(activeCountersJson, listType);
        LogUtils.i(String.format("activeCountersJson: %s", activeCountersJson));
        if (counters == null) {
            counters = new ArrayList<>();
            counters.add(new Counter(mContext.getResources().getString(R.string.counter_title_default)));
        }
        CurrentSetInteractor.getInstance().setCounters(counters);
        LogUtils.i("setCurrentSetFromJSON");
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
        CurrentSetInteractor.getInstance().setCounters(mCounters);
        String activeCountersJson = new Gson().toJson(mCounters);
        LogUtils.i("access to SharedPreferences");
        SharedPreferences.Editor editor = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(PREFS_DICE_AMOUNT, Dice.getInstance().getAmount());
        editor.putInt(PREFS_DICE_MIN_EDGE, Dice.getInstance().getMinEdge());
        editor.putInt(PREFS_DICE_MAX_EDGE, Dice.getInstance().getMaxEdge());
        editor.putInt(PREFS_DICE_BONUS, Dice.getInstance().getBonus());

        editor.putString(ACTIVE_COUNTERS, activeCountersJson);
        editor.apply();
    }


    @Override
    public void onSwipe(Counter counter, int direction, boolean isSwipe) {
        LogUtils.i(String.format("onSwipe %d %s", direction, isSwipe));
        counter.step(direction, isSwipe);
    }

    @Override
    public void loadFragment() {
        mView.loadFragment(new FragmentFav()); 
        /*
        To hit this window, Android programmers follow a convention of adding a static method named
newInstance() to the Fragment class. This method creates the fragment instance and bundles up and
sets its arguments.

  public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }
        */
        LogUtils.i("loadFragment");
    }

    @Override
    public void showDiceDialog() {
        new DiceDialog(mContext);
        LogUtils.i("showDiceDialog");
    }

    @Override
    public void removeCounter(Counter counter) {
        LogUtils.i(String.format("removeCounter %s", counter.getCaption()));
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
    public void loadCurrentSet() {
        LogUtils.i("loadCurrentSet");
        mCounters = CurrentSetInteractor.getInstance().getCounters();
        LogUtils.i(String.format("counters: %d", mCounters.size()));
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

    public void onEvent(FavoriteSetLoaded event) {
        LogUtils.i("onFavoriteSetLoaded");

        if (event != null) {
            mView.closeFragment();
            mView.clearViews();
            mCounters.clear();
            setCounters(event.getSet().getCounters());
            LogUtils.i(String.format("counters size: %d", event.getSet().getCounters().size()));
            for (Counter c : mCounters) addCounterView(c);
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

    public void onEvent(FavoritesUpdated event) {
    //TODO: onEventBackgroundThread
        if (event != null) {
            String favSetsJson = new Gson().toJson(event.getFavorites());
            SharedPreferences.Editor editor = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(FAV_ARRAY, favSetsJson);
            editor.apply();
        }
    }

    private String getCounterCaption() {
        return String.format("%s %d", mContext.getString(R.string.counter_title_default), mCounters.size() + 1);
    }


}