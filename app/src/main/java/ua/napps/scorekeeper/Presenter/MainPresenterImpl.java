package ua.napps.scorekeeper.Presenter;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Events.FavoriteSetLoaded;
import ua.napps.scorekeeper.Events.FavoritesUpdated;
import ua.napps.scorekeeper.Helpers.Constants;
import ua.napps.scorekeeper.Interactors.Dice;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.View.MainView;

import static ua.napps.scorekeeper.Helpers.Constants.ACTIVE_COUNTERS;
import static ua.napps.scorekeeper.Helpers.Constants.FAV_ARRAY;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_AMOUNT;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_BONUS;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_MAX_EDGE;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_MIN_EDGE;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_SUM;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_NAME;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_SHOW_ALL_COUNTERS;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_SHOW_DICES;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_STAY_AWAKE;
import static ua.napps.scorekeeper.Interactors.CurrentSet.getCurrentSet;

/**
 * Created by Roman on 23/11/2015.
 */
public class MainPresenterImpl implements MainPresenter {

    Context mContext;
    MainView mView;

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
    }

    @Override
    public void loadSettings() {
        
        /*
                PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_RESULT_ID,
                .apply();
        */

        SharedPreferences sp = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String activeCountersJson = sp.getString(Constants.ACTIVE_COUNTERS, "");
        Type listType = new TypeToken<ArrayList<Counter>>() {
        }.getType();
        ArrayList<Counter> counters = new Gson().fromJson(activeCountersJson, listType);
        if (counters == null) {
            counters = new ArrayList<>();
            counters.add(new Counter(mContext.getResources().getString(R.string.counter_title_default)));
        }
        getCurrentSet().setCounters(counters);
        if (sp.getBoolean(PREFS_STAY_AWAKE, true))
            mView.toggleKeepScreenOn(true);
        else mView.toggleKeepScreenOn(false);

        if (sp.getBoolean(PREFS_SHOW_ALL_COUNTERS, true)) {
            mView.showAllCountersOnScreen(true);
            mView.updateView();
        } else {
            mView.showAllCountersOnScreen(false);
            mView.updateView();
        }

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
        String activeCountersJson = new Gson().toJson(getCurrentSet().getCounters());
        SharedPreferences.Editor editor = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(PREFS_DICE_AMOUNT, Dice.getInstance().getAmount());
        editor.putInt(PREFS_DICE_MIN_EDGE, Dice.getInstance().getMinEdge());
        editor.putInt(PREFS_DICE_MAX_EDGE, Dice.getInstance().getMaxEdge());
        editor.putInt(PREFS_DICE_BONUS, Dice.getInstance().getBonus());

        editor.putString(ACTIVE_COUNTERS, activeCountersJson);
        editor.apply();
    }

    public void onEvent(FavoriteSetLoaded event) {

        if (event != null) {
            mView.closeFragment("favorites");
        }
    }

    public void onEventBackgroundThread(FavoritesUpdated event) {
        if (event != null) {
            String favSetsJson = new Gson().toJson(event.getFavorites());
            SharedPreferences.Editor editor = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(FAV_ARRAY, favSetsJson);
            editor.apply();
        }
    }

    private String getCounterCaption() {
        return String.format("%s %d", mContext.getString(R.string.counter_title_default), getCurrentSet().getSize() + 1);
    }
}