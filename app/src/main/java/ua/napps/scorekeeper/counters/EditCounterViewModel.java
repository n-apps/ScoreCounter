package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Color;
import android.text.TextUtils;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class EditCounterViewModel extends ViewModel {

  public final ObservableField<Counter> counter = new ObservableField<>();
  public final ObservableField<String> counterName = new ObservableField<>();
  public final ObservableInt counterValue = new ObservableInt();
  public final ObservableInt counterColor = new ObservableInt();
  public final ObservableInt counterStep = new ObservableInt();
  public final ObservableInt counterDefaultValue = new ObservableInt();

  private LiveData<Counter> counterLiveData = new MutableLiveData<>();
  private final CountersRepository countersRepository;
  private final int id;

  public EditCounterViewModel(CountersRepository repository, final int counterId) {
    id = counterId;
    countersRepository = repository;
    counterLiveData = repository.loadCounter(counterId);
    counterName.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override public void onPropertyChanged(Observable observable, int i) {
        final String n = counterName.get();
        if (Objects.equals(counterLiveData.getValue().getName(), n)) {
          return;
        }
        updateName(n);
      }
    });
    counterValue.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override public void onPropertyChanged(Observable observable, int i) {
        final int v = counterValue.get();
        if (Objects.equals(counterLiveData.getValue().getValue(), v)) {
          return;
        }
        updateValue(v);
      }
    });
    counterDefaultValue.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override public void onPropertyChanged(Observable observable, int i) {
        final int v = counterDefaultValue.get();
        if (Objects.equals(counterLiveData.getValue().getDefaultValue(), v)) {
          return;
        }
        updateDefaultValue(v);
      }
    });

    counterStep.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override public void onPropertyChanged(Observable observable, int i) {
        final int v = counterStep.get();
        if (Objects.equals(counterLiveData.getValue().getStep(), v)) {
          return;
        }
        updateStep(v);
      }
    });
  }

  private void updateDefaultValue(int defaultValue) {
    countersRepository.modifyDefaultValue(id, defaultValue)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .delay(1, TimeUnit.SECONDS)
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

  private void updateStep(int step) {
    countersRepository.modifyStep(id, step)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .delay(1, TimeUnit.SECONDS)
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

  private void updateValue(int value) {
    countersRepository.setCount(id, value)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .delay(1, TimeUnit.SECONDS)
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

  public void updateColor(String hex) {
    countersRepository.modifyColor(id, hex)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .delay(1, TimeUnit.SECONDS)
        .subscribe(new CompletableObserver() {
          @Override public void onSubscribe(Disposable d) {

          }

          @Override public void onComplete() {
            Timber.d("onComplete - successfully deleted counter");
          }

          @Override public void onError(Throwable e) {
            Timber.d("onError - add:", e);
          }
        });
  }

  public LiveData<Counter> getCounterLiveData() {
    return counterLiveData;
  }

  public void setCounter(Counter c) {
    this.counter.set(c);
    counterName.set(c.getName());
    counterValue.set(c.getValue());
    counterStep.set(c.getStep());
    counterDefaultValue.set(c.getDefaultValue());
    counterColor.set(Color.parseColor(c.getColor()));
  }

  private void updateName(String newName) {
    if (TextUtils.isEmpty(newName)) return; // TODO: 05-Oct-17 show snackbar
    countersRepository.modifyName(id, newName)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .delay(1, TimeUnit.SECONDS)
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

  public void deleteCounter() {
    countersRepository.delete(counter.get())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new CompletableObserver() {
          @Override public void onSubscribe(Disposable d) {

          }

          @Override public void onComplete() {
            Timber.d("onComplete - successfully deleted counter");
          }

          @Override public void onError(Throwable e) {
            Timber.d("onError - add:", e);
          }
        });
  }
}
