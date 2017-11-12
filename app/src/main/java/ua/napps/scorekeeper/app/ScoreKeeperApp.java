package ua.napps.scorekeeper.app;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
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
            getFirebaseAnalytics().setAnalyticsCollectionEnabled(false);
        } else {
            Timber.plant(new CrashlyticsTree());
            getFirebaseAnalytics().setAnalyticsCollectionEnabled(true);
        }
        DatabaseHolder.init(this);
    }

    synchronized public FirebaseAnalytics getFirebaseAnalytics() {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }
        return firebaseAnalytics;
    }


    private static class CrashlyticsTree extends Timber.Tree {

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }
            if (!TextUtils.isEmpty(message)) {
                Crashlytics.log(priority, tag, message);
            }
            if (t != null) {
                Crashlytics.logException(t);
            }
        }
    }
}
