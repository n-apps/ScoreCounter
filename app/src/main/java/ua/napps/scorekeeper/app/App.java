package ua.napps.scorekeeper.app;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;
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
        Timber.plant(BuildConfig.DEBUG ? new Timber.DebugTree() : new CrashlyticsTree(this));
        DatabaseHolder.init(this);
    }

    private static class CrashlyticsTree extends Timber.Tree {

        CrashlyticsTree(Context context) {
            Fabric.with(context, new Crashlytics());
        }

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
}
