package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.LiveData;
import io.reactivex.Completable;
import java.util.List;

class CountersRepositoryImpl implements CountersRepository {

  CountersDatabase countersDatabase;

  public CountersRepositoryImpl(CountersDatabase db) {
    countersDatabase = db;
  }

  @Override public LiveData<List<Counter>> getCounters() {
    //Here is where we would do more complex logic, like getting events from a cache
    //then inserting into the database etc. In this example we just go straight to the dao.
    return countersDatabase.counterDao().loadAllCounters();
  }

  @Override public LiveData<Counter> loadCounter(int counterId) {
    return countersDatabase.counterDao().loadCounter(counterId);
  }

  @Override public Completable createCounter(String name) {

    return Completable.fromAction(() -> {
      final Counter counter = new Counter(name);
      countersDatabase.counterDao().insertCounter(counter);
    });
  }

  @Override public Completable modifyCount(int counterId, int difference) {
    return Completable.fromAction(
        () -> countersDatabase.counterDao().modifyValue(counterId, difference));
  }

  @Override public Completable setCount(int counterId, int value) {
    return Completable.fromAction(() -> countersDatabase.counterDao().setValue(counterId, value));
  }

  @Override public Completable modifyName(int counterId, String name) {
    return Completable.fromAction(() -> countersDatabase.counterDao().modifyName(counterId, name));
  }

  @Override public Completable modifyDefaultValue(int counterId, int defaultValue) {
    return Completable.fromAction(
        () -> countersDatabase.counterDao().modifyDefaultValue(counterId, defaultValue));
  }

  @Override public Completable modifyStep(int counterId, int step) {
    return Completable.fromAction(() -> countersDatabase.counterDao().modifyStep(counterId, step));
  }

  @Override public Completable modifyColor(int counterId, String hex) {
    return Completable.fromAction(() -> countersDatabase.counterDao().modifyColor(counterId, hex));
  }

  @Override public Completable delete(Counter counter) {
    return Completable.fromAction(() -> countersDatabase.counterDao().deleteCounter(counter));
  }
}
