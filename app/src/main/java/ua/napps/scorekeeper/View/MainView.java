package ua.napps.scorekeeper.View;

import android.content.Context;

/**
 * Created by Roman on 23/11/2015.
 */
public interface MainView {

    Context getContext();

    void updateView();


    void showAllCountersOnScreen(boolean isShowing);

    void closeFragment(String tag);

    void toggleKeepScreenOn(boolean isEnabled);

    void toggleDicesBar(boolean isShowing);

    void setDiceFormula(String formula);

    void setDiceSum(String sum);


}
