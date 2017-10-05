package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.LiveData;
import io.reactivex.Completable;
import java.util.List;

class CounterRepositoryImpl implements CountersRepository {

  CountersDatabase countersDatabase;

  public CounterRepositoryImpl(CountersDatabase db) {
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

  @Override public Completable delete(int counterId) {
    //return Completable.fromAction(() -> countersDatabase.counterDao().deleteCounter(counterId));
    return Completable.complete();
  }
}
