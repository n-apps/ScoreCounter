package ua.napps.scorekeeper.counters;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import timber.log.Timber;

public class CountersViewModel extends AndroidViewModel {

  private LiveData<List<Counter>> counters = new MutableLiveData<>();
  private final CountersRepository countersRepository;

  public CountersViewModel(Application application) {
    super(application);
    countersRepository = new CounterRepositoryImpl(
        Room.databaseBuilder(application.getApplicationContext(), CountersDatabase.class,
            "counters_db").build());
    counters = countersRepository.getCounters();
  }

  public void addCounter() {
    final Counter counter = new Counter("LIVE");
    countersRepository.addCounter(counter)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new CompletableObserver() {
          @Override public void onSubscribe(Disposable d) {

          }

          @Override public void onComplete() {
            Timber.d("onComplete - successfully added event");
          }

          @Override public void onError(Throwable e) {
            Timber.d("onError - add:", e);
          }
        });
  }

  /**
   * Expose the LiveData Products query so the UI can observe it.
   */
  public LiveData<List<Counter>> getProducts() {
    return counters;
  }
}
