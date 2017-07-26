package ua.napps.scorekeeper.app;

import android.app.Application;
import io.paperdb.Paper;

public class ScoreKeeperApp extends Application {

  @Override public void onCreate() {
    super.onCreate();
    Paper.init(this);
  }
}
