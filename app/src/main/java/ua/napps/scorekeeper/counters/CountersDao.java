package ua.napps.scorekeeper.counters;

import android.util.SparseIntArray;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import io.reactivex.Completable;

@Dao
public abstract class CountersDao {

    @Query("DELETE FROM counters")
    public abstract Completable deleteAll();

    @Delete
    public abstract Completable deleteCounter(Counter counter);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract Completable insert(Counter counter);

    @Query("SELECT * FROM counters ORDER BY position")
    public abstract LiveData<List<Counter>> loadAllCounters();

    @Query("SELECT * FROM counters ORDER BY position")
    public abstract List<Counter> loadAllCountersSync();

    @Query("SELECT COUNT(*) FROM counters")
    public abstract int count();

    @Query("select * from counters where id = :counterId")
    public abstract LiveData<Counter> loadCounter(int counterId);

    @Query("select * from counters where id = :counterId")
    public abstract Counter loadCounterSync(int counterId);

    @Query("UPDATE counters " + "SET color = :hex WHERE id = :counterId")
    public abstract Completable modifyColor(int counterId, String hex);

    @Query("UPDATE counters " + "SET defaultValue = :defaultValue WHERE id = :counterId")
    public abstract Completable modifyDefaultValue(int counterId, int defaultValue);

    @Query("UPDATE counters " + "SET name = :counterName WHERE id = :counterId")
    public abstract Completable modifyName(int counterId, String counterName);

    @Query("UPDATE counters " + "SET step = :step WHERE id = :counterId")
    public abstract Completable modifyStep(int counterId, int step);

    @Query("UPDATE counters SET value =(value +:difference) WHERE id ==:counterId")
    public abstract Completable modifyValue(int counterId, int difference);

    @Query("UPDATE counters " + "SET position = :position WHERE id = :counterId")
    public abstract void modifyPosition(int counterId, int position);

    @Transaction
    public void modifyPositionBatch(@NonNull SparseIntArray positionMap) {
        int size = positionMap.size();
        for (int i = 0; i < size; i++) {
            int counterId = positionMap.keyAt(i);
            int newPosition = positionMap.valueAt(i);
            modifyPosition(counterId, newPosition);
        }
    }

    @Query("UPDATE counters " + "SET value = defaultValue")
    public abstract Completable resetValues();

    @Query("UPDATE counters " + "SET value = :value WHERE id = :counterId")
    public abstract Completable setValue(int counterId, int value);
}