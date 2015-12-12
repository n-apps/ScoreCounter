package ua.napps.scorekeeper.Presenter;

import java.util.ArrayList;

import ua.napps.scorekeeper.AwesomeLayout;
import ua.napps.scorekeeper.Models.Counter;

/**
 * Created by Roman on 23/11/2015.
 */
public interface MainPresenter {

    void onStart();

    void onStop();

    void onResume();

    void loadSettings();

    void saveSettings();

    void onSwipe(Counter counter, int direction, boolean isSwipe);

    void loadFragment();

    void showDiceDialog();

    void setCounters(ArrayList<Counter> counters);

    void addCounter();

    void removeCounter(Counter counter);

    void loadCurrentSet();
}
