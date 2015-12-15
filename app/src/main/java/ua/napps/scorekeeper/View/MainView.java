package ua.napps.scorekeeper.View;

import android.content.Context;
import android.support.v4.app.Fragment;

import ua.napps.scorekeeper.Models.Counter;

/**
 * Created by Roman on 23/11/2015.
 */
public interface MainView {

    Context getContext();

    void clearViews();

    void addCounter(Counter counter);

    void removeCounter(Counter counter);

    void toggleKeepScreenOn(boolean isEnabled);

    void toggleDicesBar(boolean isShowing);

    void setDiceSum(String sum);

    void setDiceFormula(String formula);

    void loadFragment(Fragment fragment);

    void closeFragment(String tag);

    void CounterCaptionClick(Counter counter, boolean isNeutralButtonEnabled);

    void changeAddCounterButtonState(boolean isVisible);
}
