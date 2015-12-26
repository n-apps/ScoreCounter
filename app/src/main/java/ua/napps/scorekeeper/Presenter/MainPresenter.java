package ua.napps.scorekeeper.Presenter;

/**
 * Created by Roman on 23/11/2015.
 */
public interface MainPresenter {

    void onStart();

    void onStop();

    void onResume();

    void loadSettings();

    void saveSettings();

}
