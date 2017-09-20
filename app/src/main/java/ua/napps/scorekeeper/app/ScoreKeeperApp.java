package ua.napps.scorekeeper.app;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;
import io.paperdb.Paper;
import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;

public class ScoreKeeperApp extends Application {

  @Override public void onCreate() {
    super.onCreate();
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
    Paper.init(this);
  }
}
