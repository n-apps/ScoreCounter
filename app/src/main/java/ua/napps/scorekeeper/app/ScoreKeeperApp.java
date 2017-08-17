package ua.napps.scorekeeper.app;

import android.app.Application;
import io.paperdb.Paper;
import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;

public class ScoreKeeperApp extends Application {

  @Override public void onCreate() {
    super.onCreate();
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
    Paper.init(this);
  }
}
