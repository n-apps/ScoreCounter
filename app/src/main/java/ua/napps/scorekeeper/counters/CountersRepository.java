package ua.napps.scorekeeper.counters;

import android.util.SparseIntArray;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.reactivex.Completable;

class CountersRepository {

    @NonNull
    private final CountersDao countersDao;

    CountersRepository(@NonNull CountersDao countersDao) {
        this.countersDao = countersDao;
    }

    public Completable createCounter(String name, String color, int position) {
        final Counter counter = new Counter(name, color,position);
        return countersDao.insert(counter);
    }

    public Completable delete(Counter counter) {
        return countersDao.deleteCounter(counter);
    }

    public Completable deleteAll() {
        return countersDao.deleteAll();
    }

    public LiveData<List<Counter>> getCounters() {
        return countersDao.loadAllCounters();
    }

    public LiveData<Counter> loadCounter(int counterId) {
        return countersDao.loadCounter(counterId);
    }

    public Completable modifyColor(int counterId, String hex) {
        return countersDao.modifyColor(counterId, hex);
    }

    public Completable modifyCount(int counterId, int difference) {
        return countersDao.modifyValue(counterId, difference);
    }

    public Completable modifyDefaultValue(int counterId, int defaultValue) {
        return countersDao.modifyDefaultValue(counterId, defaultValue);
    }

    public Completable modifyName(int counterId, String name) {
        return countersDao.modifyName(counterId, name);
    }

    public Completable modifyStep(int counterId, int step) {
        return countersDao.modifyStep(counterId, step);
    }

    public Completable modifyPositionBatch(@NonNull SparseIntArray positionMap) {
        return Completable.fromAction(() -> countersDao.modifyPositionBatch(positionMap));
    }

    public Completable resetAll() {
        return countersDao.resetValues();
    }

    public Completable setCount(int counterId, int value) {
        return countersDao.setValue(counterId, value);
    }
}
