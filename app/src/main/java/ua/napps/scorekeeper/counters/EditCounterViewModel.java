package ua.napps.scorekeeper.counters;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class EditCounterViewModel extends AndroidViewModel {

  public ObservableField<String> counterName = new ObservableField<>();
  public ObservableInt counterValue = new ObservableInt();

  private LiveData<Counter> counter = new MutableLiveData<>();
  private final CountersRepository countersRepository;
  private final int counterId;

  public EditCounterViewModel(Application application, final int counterId) {
    super(application);
    this.counterId = counterId;
    countersRepository =
        new CounterRepositoryImpl(CountersDatabase.getDatabaseInstance(application));
    counter = countersRepository.loadCounter(counterId);
    counterName.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override public void onPropertyChanged(Observable observable, int i) {
        final String n = counterName.get();
        if (Objects.equals(counter.getValue().getName(), n)) {
          return;
        }
        updateName(n);
      }
    });
    counterValue.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override public void onPropertyChanged(Observable observable, int i) {
        final int v = counterValue.get();
        if (Objects.equals(counter.getValue().getValue(), v)) {
          return;
        }
        updateValue(v);
      }
    });
  }

  private void updateValue(int value) {
    countersRepository.setCount(counterId, value)
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

  /**
   * Expose the LiveData Products query so the UI can observe it.
   */
  public LiveData<Counter> getCounter() {
    return counter;
  }

  public void updateName(String newName) {
    if (TextUtils.isEmpty(newName)) return; // TODO: 05-Oct-17 show snackbar
    countersRepository.modifyName(counterId, newName)
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

  public void increaseCounter(Counter counter) {
    countersRepository.modifyCount(counter.getId(), counter.getStep())
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
   * A creator is used to inject the product ID into the ViewModel
   * <p>
   * This creator is to showcase how to inject dependencies into ViewModels. It's not
   * actually necessary in this case, as the product ID can be passed in a public method.
   */
  public static class Factory extends ViewModelProvider.NewInstanceFactory {

    @NonNull private final Application mApplication;

    private final int mCounterId;

    public Factory(@NonNull Application application, int productId) {
      mApplication = application;
      mCounterId = productId;
    }

    @Override public <T extends ViewModel> T create(Class<T> modelClass) {
      //noinspection unchecked
      return (T) new EditCounterViewModel(mApplication, mCounterId);
    }
  }

  @BindingAdapter({ "android:text" })
  public static void setTextFromInt(TextInputEditText editText, int value) {
    if (getTextAsInt(editText) != value) {
      editText.setText(String.valueOf(value));
    }
  }

  @InverseBindingAdapter(attribute = "android:text")
  public static int getTextAsInt(TextInputEditText editText) {
    try {
      return Integer.parseInt(editText.getText().toString());
    } catch (Exception e) {
      return 0;
    }
  }
}
