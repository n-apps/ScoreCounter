package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class EditCounterViewModel extends ViewModel {

    private Counter counter;

    private LiveData<Counter> counterLiveData = new MutableLiveData<>();

    private final CountersRepository countersRepository;

    private final int id;

    public EditCounterViewModel(CountersRepository repository, final int counterId) {
        id = counterId;
        countersRepository = repository;
        counterLiveData = repository.loadCounter(counterId);
    }

    public void deleteCounter() {
        countersRepository.delete(counter)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                        Timber.d("onComplete - successfully deleted counter");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("onError - add:", e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    public LiveData<Counter> getCounterLiveData() {
        return counterLiveData;
    }

    public void setCounter(Counter c) {

    }

    public void updateColor(String hex) {
        countersRepository.modifyColor(id, hex)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                        Timber.d("onComplete - successfully deleted counter");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("onError - add:", e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    private void updateDefaultValue(int defaultValue) {
        countersRepository.modifyDefaultValue(id, defaultValue)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                        Timber.d("onComplete - successfully added event");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("onError - add:", e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    private void updateName(String newName) {
        if (TextUtils.isEmpty(newName)) {
            return; // TODO: 05-Oct-17 show snackbar
        }
        countersRepository.modifyName(id, newName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                        Timber.d("onComplete - successfully added event");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("onError - add:", e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    private void updateStep(int step) {
        countersRepository.modifyStep(id, step)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                        Timber.d("onComplete - successfully added event");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("onError - add:", e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    private void updateValue(int value) {
        countersRepository.setCount(id, value)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                        Timber.d("onComplete - successfully added event");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("onError - add:", e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }
}
