package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
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

    EditCounterViewModel(CountersRepository repository, final int counterId) {
        id = counterId;
        countersRepository = repository;
        counterLiveData = repository.loadCounter(counterId);
    }

    public void setCounter(@NonNull Counter c) {
        counter = c;
    }

    void deleteCounter() {
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
                        Timber.e(e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    LiveData<Counter> getCounterLiveData() {
        return counterLiveData;
    }

    void updateColor(String hex) {
        if (hex.equals(counter.getColor())) {
            return;
        }
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
                        Timber.e(e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    void updateDefaultValue(int defaultValue) {
        if (defaultValue == counter.getDefaultValue()) {
            return;
        }
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
                        Timber.e(e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    void updateName(@NonNull String newName) {
        if (newName.equals(counter.getName())) {
            return;
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
                        Timber.e(e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    void updateStep(int step) {
        if (step == counter.getStep()) {
            return;
        }
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
                        Timber.e(e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    void updateValue(int value) {
        if (value == counter.getValue()) {
            return;
        }
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
                        Timber.e(e);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }
}
