package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import io.reactivex.Completable;
import java.util.List;

class CountersRepository {

  @NonNull private final CountersDao countersDao;

  public CountersRepository(@NonNull CountersDao countersDao) {
    this.countersDao = countersDao;
  }

  public LiveData<List<Counter>> getCounters() {
    //Here is where we would do more complex logic, like getting events from a cache
    //then inserting into the database etc. In this example we just go straight to the dao.
    return countersDao.loadAllCounters();
  }

  public LiveData<Counter> loadCounter(int counterId) {
    return countersDao.loadCounter(counterId);
  }

  public Completable createCounter(String name, String color) {

    return Completable.fromAction(() -> {
      final Counter counter = new Counter(name, color);
      countersDao.insertCounter(counter);
    });
  }

  public Completable modifyCount(int counterId, int difference) {
    return Completable.fromAction(() -> countersDao.modifyValue(counterId, difference));
  }

  public Completable setCount(int counterId, int value) {
    return Completable.fromAction(() -> countersDao.setValue(counterId, value));
  }

  public Completable modifyName(int counterId, String name) {
    return Completable.fromAction(() -> countersDao.modifyName(counterId, name));
  }

  public Completable modifyDefaultValue(int counterId, int defaultValue) {
    return Completable.fromAction(() -> countersDao.modifyDefaultValue(counterId, defaultValue));
  }

  public Completable modifyStep(int counterId, int step) {
    return Completable.fromAction(() -> countersDao.modifyStep(counterId, step));
  }

  public Completable modifyColor(int counterId, String hex) {
    return Completable.fromAction(() -> countersDao.modifyColor(counterId, hex));
  }

  public Completable resetAll() {
    return Completable.fromAction(countersDao::resetValues);
  }

  public Completable delete(Counter counter) {
    return Completable.fromAction(() -> countersDao.deleteCounter(counter));
  }

  public Completable deleteAll() {
    return Completable.fromAction(countersDao::deleteAll);
  }
}
