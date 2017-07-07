package ua.napps.scorekeeper.app;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;
import io.paperdb.Paper;

public class ScoreKeeperApp extends Application {

  @Override public void onCreate() {
    super.onCreate();
    Paper.init(this);
  }

  static {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
  }
}
