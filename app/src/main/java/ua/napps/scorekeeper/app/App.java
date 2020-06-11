package ua.napps.scorekeeper.app;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.Nullable;

import timber.log.Timber;
import ua.napps.scorekeeper.BuildConfig;
import ua.napps.scorekeeper.storage.DatabaseHolder;
import ua.napps.scorekeeper.storage.TinyDB;

public class App extends Application {

    private static TinyDB tinyDB;
    private static App instance;

    public static TinyDB getTinyDB() {
        if (tinyDB == null) {
            tinyDB = new TinyDB(App.getInstance());
        }
        return tinyDB;
    }

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.instance = this;
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Timber.plant(BuildConfig.DEBUG ? new Timber.DebugTree() : new CrashlyticsTree());
        DatabaseHolder.init(this);
    }

    private static class CrashlyticsTree extends Timber.Tree {

        private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
        private static final String CRASHLYTICS_KEY_TAG = "tag";
        private static final String CRASHLYTICS_KEY_MESSAGE = "message";

        @Override
        protected boolean isLoggable(@Nullable String tag, int priority) {
            return priority >= Log.WARN;
        }

        @Override
        protected void log(int priority, String tag, @NonNull String message, @Nullable Throwable t) {
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey(CRASHLYTICS_KEY_PRIORITY, priority);
            crashlytics.setCustomKey(CRASHLYTICS_KEY_TAG, tag);
            crashlytics.setCustomKey(CRASHLYTICS_KEY_MESSAGE, message);

            if (t != null) {
                crashlytics.recordException(t);
            } else {
                crashlytics.recordException(new RuntimeException(message));
            }
        }
    }
}
