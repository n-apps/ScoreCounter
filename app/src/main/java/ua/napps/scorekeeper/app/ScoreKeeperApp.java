package ua.napps.scorekeeper.app;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;
import ua.napps.scorekeeper.storage.DatabaseHolder;

public class ScoreKeeperApp extends Application {

    private static class CrashlyticsTree extends Timber.Tree {

        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
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

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Timber.plant(BuildConfig.DEBUG ? new Timber.DebugTree() : new CrashlyticsTree());
        DatabaseHolder.init(this);
    }
}
