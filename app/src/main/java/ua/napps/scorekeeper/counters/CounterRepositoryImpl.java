package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.LiveData;
import io.reactivex.Completable;
import java.util.List;

class CounterRepositoryImpl implements CountersRepository {

  CountersDatabase countersDatabase;

  public CounterRepositoryImpl(CountersDatabase db) {
    countersDatabase = db;
  }

  @Override public Completable addCounter(Counter counter) {
    return Completable.fromAction(() -> countersDatabase.counterDao().addCounter(counter));
  }

  @Override public LiveData<List<Counter>> getCounters() {
    //Here is where we would do more complex logic, like getting events from a cache
    //then inserting into the database etc. In this example we just go straight to the dao.
    return countersDatabase.counterDao().loadAllCounters();
  }

  @Override public Completable deleteCounter(Counter counter) {
    return Completable.fromAction(() -> countersDatabase.counterDao().deleteCounter(counter));
  }
}
