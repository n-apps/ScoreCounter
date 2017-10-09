package ua.napps.scorekeeper.counters;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.Random;
import timber.log.Timber;
import ua.com.napps.scorekeeper.R;

public class CountersViewModel extends AndroidViewModel {

  private LiveData<List<Counter>> counters = new MutableLiveData<>();
  private final CountersRepository repository;

  public CountersViewModel(Application application, CountersRepository countersRepository) {
    super(application);
    repository = countersRepository;
    counters = countersRepository.getCounters();
  }

  public void addCounter() {
    repository.createCounter(
        getApplication().getString(R.string.counter_default_title, counters.getValue().size() + 1),
        getRandomColor())
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

  LiveData<List<Counter>> getProducts() {
    return counters;
  }

  void increaseCounter(Counter counter) {
    repository.modifyCount(counter.getId(), counter.getStep())
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

  void decreaseCounter(Counter counter) {
    repository.modifyCount(counter.getId(), -counter.getStep())
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

  private String getRandomColor() {
    String[] colors = getApplication().getApplicationContext()
        .getResources()
        .getStringArray(R.array.color_collection);
    final int presetSize = colors.length;
    return colors[new Random().nextInt(presetSize - 1)];
  }

  void removeAll() {
    repository.deleteAll()
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

  void resetAll() {
    repository.resetAll()
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
}
