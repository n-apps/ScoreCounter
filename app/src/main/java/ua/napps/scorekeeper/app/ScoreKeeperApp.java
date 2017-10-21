package ua.napps.scorekeeper.app;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v7.app.AppCompatDelegate;
import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;
import ua.napps.scorekeeper.storage.DatabaseHolder;

public class ScoreKeeperApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        DatabaseHolder.init(this);

        if (getVersionCode() < 30000) {

        }

    }

    private int getVersionCode() {
        int result = 0;
        try {
            result = getApplicationContext().getPackageManager().getPackageInfo(
                    getApplicationContext().getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // squelch
        }
        return result;
    }
}
