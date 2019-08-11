package ua.napps.scorekeeper.counters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;

class EditCounterViewModel extends ViewModel {

    private final CountersRepository countersRepository;
    private final LiveData<List<Counter>> countersLiveData;
    private final LiveData<Counter> counterLiveData;
    private final int id;
    private Counter counter;

    EditCounterViewModel(CountersRepository repository, final int counterId) {
        id = counterId;
        countersRepository = repository;
        counterLiveData = repository.loadCounter(counterId);
        countersLiveData = countersRepository.getCounters();
    }

    LiveData<Counter> getCounterLiveData() {
        return counterLiveData;
    }

    LiveData<List<Counter>> getCounters() {
        return countersLiveData;
    }

    public void setCounter(@NonNull Counter c) {
        counter = c;
    }

    void deleteCounter() {
        AndroidFirebaseAnalytics.logEvent("EditCounterScreenDeleteCounterClick");

        countersRepository.delete(counter)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {

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

    void updateColor(@Nullable String hex) {
        if (hex == null || hex.equals(counter.getColor())) {
            return;
        }
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CHARACTER, hex);
        AndroidFirebaseAnalytics.logEvent("EditCounterScreenNewColorSelected", params);
        countersRepository.modifyColor(id, hex)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {

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
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CHARACTER, "" + defaultValue);
        AndroidFirebaseAnalytics.logEvent("EditCounterScreenNewDefaultValueSubmit", params);

        countersRepository.modifyDefaultValue(id, defaultValue)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
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

        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CHARACTER, newName);
        AndroidFirebaseAnalytics.logEvent("EditCounterScreenNewNameSubmit", params);

        countersRepository.modifyName(id, newName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
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
        if (step == counter.getStep() || step == 0) {
            return;
        }
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CHARACTER, "" + step);
        AndroidFirebaseAnalytics.logEvent("EditCounterScreenNewStepSubmit", params);

        countersRepository.modifyStep(id, step)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {

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
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CHARACTER, "" + value);
        AndroidFirebaseAnalytics.logEvent("EditCounterScreenNewValueSubmit", params);

        countersRepository.setCount(id, value)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
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
