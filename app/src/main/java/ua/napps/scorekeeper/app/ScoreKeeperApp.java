package ua.napps.scorekeeper.app;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;
import com.google.firebase.analytics.FirebaseAnalytics;
import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;
import ua.napps.scorekeeper.storage.DatabaseHolder;

public class ScoreKeeperApp extends Application {

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        DatabaseHolder.init(this);

        getFirebaseAnalytics();
    }

    synchronized public FirebaseAnalytics getFirebaseAnalytics() {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
        return firebaseAnalytics;
    }
}
