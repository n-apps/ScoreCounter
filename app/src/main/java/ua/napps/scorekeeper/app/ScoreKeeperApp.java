package ua.napps.scorekeeper.app;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

public class ScoreKeeperApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}
