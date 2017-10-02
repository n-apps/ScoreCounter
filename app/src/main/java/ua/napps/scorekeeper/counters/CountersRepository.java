package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.LiveData;
import io.reactivex.Completable;
import java.util.List;

public interface CountersRepository {

  Completable addCounter(Counter counter);

  LiveData<List<Counter>> getCounters();

  Completable deleteCounter(Counter counter);
}
