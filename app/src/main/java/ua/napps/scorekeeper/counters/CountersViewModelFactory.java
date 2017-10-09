package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import ua.napps.scorekeeper.app.ScoreKeeperApp;

public class CountersViewModelFactory extends ViewModelProvider.NewInstanceFactory {

  private ScoreKeeperApp app;
  private final CountersRepository repository;

  CountersViewModelFactory(ScoreKeeperApp scoreKeeperApp, CountersDao countersDao) {
    app = scoreKeeperApp;
    repository = new CountersRepository(countersDao);
  }

  @NonNull @Override public <T extends ViewModel> T create(Class<T> modelClass) {
    //noinspection unchecked
    return (T) new CountersViewModel(app, repository);
  }
}
