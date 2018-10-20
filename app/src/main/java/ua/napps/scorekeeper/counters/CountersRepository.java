package ua.napps.scorekeeper.counters;

import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import java.util.List;

import io.reactivex.Completable;

class CountersRepository {

    @NonNull
    private final CountersDao countersDao;

    CountersRepository(@NonNull CountersDao countersDao) {
        this.countersDao = countersDao;
    }

    public Completable createCounter(String name, String color) {

        return Completable.fromAction(() -> {
            final Counter counter = new Counter(name, color);
            countersDao.insert(counter);
        });
    }

    public Completable delete(Counter counter) {
        return Completable.fromAction(() -> countersDao.deleteCounter(counter));
    }

    public Completable deleteAll() {
        return Completable.fromAction(countersDao::deleteAll);
    }

    public LiveData<List<Counter>> getCounters() {
        return countersDao.loadAllCounters();
    }

    public LiveData<Counter> loadCounter(int counterId) {
        return countersDao.loadCounter(counterId);
    }

    public Completable modifyColor(int counterId, String hex) {
        return Completable.fromAction(() -> countersDao.modifyColor(counterId, hex));
    }

    public Completable modifyCount(int counterId, int difference) {
        return Completable.fromAction(() -> countersDao.modifyValue(counterId, difference));
    }

    public Completable modifyDefaultValue(int counterId, int defaultValue) {
        return Completable.fromAction(() -> countersDao.modifyDefaultValue(counterId, defaultValue));
    }

    public Completable modifyName(int counterId, String name) {
        return Completable.fromAction(() -> countersDao.modifyName(counterId, name));
    }

    public Completable modifyStep(int counterId, int step) {
        return Completable.fromAction(() -> countersDao.modifyStep(counterId, step));
    }

    public Completable resetAll() {
        return Completable.fromAction(countersDao::resetValues);
    }

    public Completable setCount(int counterId, int value) {
        return Completable.fromAction(() -> countersDao.setValue(counterId, value));
    }
}
