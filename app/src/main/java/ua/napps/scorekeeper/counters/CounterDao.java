package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import java.util.List;

@Dao public interface CounterDao {
  @Query("SELECT * FROM counters") LiveData<List<Counter>> loadAllCounters();

  @Insert(onConflict = OnConflictStrategy.REPLACE) void insertAll(List<Counter> counters);

  @Insert(onConflict = OnConflictStrategy.REPLACE) void addCounter(Counter counter);

  @Delete void deleteCounter(Counter counter);

  @Update(onConflict = OnConflictStrategy.REPLACE) void updateCounter(Counter event);

  @Query("select * from counters where id = :counterId") LiveData<Counter> loadCounter(
      int counterId);

  @Query("select * from counters where id = :counterId") Counter loadCounterSync(int counterId);
}