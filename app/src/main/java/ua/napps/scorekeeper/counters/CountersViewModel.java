package ua.napps.scorekeeper.counters;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics.Param;

import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.Singleton;

class CountersViewModel extends AndroidViewModel {

    private final CountersRepository repository;
    private final LiveData<List<Counter>> counters;
    private final String[] colors;
    private int listSize;
    private int nextCounterColor;

    CountersViewModel(Application application, CountersRepository countersRepository) {
        super(application);
        repository = countersRepository;
        counters = countersRepository.getCounters();
        colors = application.getResources().getStringArray(LocalSettings.isDarkTheme() ? R.array.dark : R.array.light);
    }

    public LiveData<Counter> getCounterLiveData(int counterID) {
        return repository.loadCounter(counterID);
    }

    public void setListSize(final int listSize) {
        this.listSize = listSize;
    }

    void addCounter() {
        repository.createCounter(String.valueOf(nextCounterColor + 1), getNextColor())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "create counter");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
        nextCounterColor++;
    }

    void decreaseCounter(Counter counter) {
        decreaseCounter(counter, -counter.getStep());
    }

    void decreaseCounter(Counter counter, @IntRange(from = Integer.MIN_VALUE, to = 0) int amount) {
        if (amount != -counter.getStep()) {
            Bundle params = new Bundle();
            params.putString(Param.CHARACTER, "" + amount);
            AndroidFirebaseAnalytics.logEvent("decrease_counter", params);
        }
        repository.modifyCount(counter.getId(), amount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "modifyCount counter");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    LiveData<List<Counter>> getCounters() {
        return counters;
    }

    void increaseCounter(Counter counter) {
        increaseCounter(counter, counter.getStep());
    }

    void increaseCounter(Counter counter, @IntRange(from = 0, to = Integer.MAX_VALUE) int amount) {
        if (amount != counter.getStep()) {
            Bundle params = new Bundle();
            params.putString(Param.CHARACTER, "" + amount);
            AndroidFirebaseAnalytics.logEvent("increase_counter", params);
        }
        repository.modifyCount(counter.getId(), amount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "modifyCount counter");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    void modifyName(Counter counter, @NonNull String name) {
        if (!name.equals(counter.getName())) {
            Bundle params = new Bundle();
            params.putString(Param.CHARACTER, counter.getName());
            AndroidFirebaseAnalytics.logEvent("counter_name_submit", params);
        }
        repository.modifyName(counter.getId(), name)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "modifyName counter");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }


    void removeAll() {
        List<Counter> counterList = counters.getValue();
        if (counterList != null) {
            for (int i = 0; i < counterList.size(); i++) {
                Singleton.getInstance().addLogEntry(new LogEntry(counterList.get(i), LogType.RMV, 0, counterList.get(i).getValue()));
            }
        }

        repository.deleteAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "remove all");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    void resetAll() {
        List<Counter> counterList = counters.getValue();
        if (counterList != null) {
            for (int i = 0; i < counterList.size(); i++) {
                Singleton.getInstance().addLogEntry(new LogEntry(counterList.get(i), LogType.RST, 0, counterList.get(i).getValue()));
            }
        }

        repository.resetAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "resetAll");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    private String getNextColor() {
        if (nextCounterColor < colors.length) {
            return colors[nextCounterColor];
        } else {
            return colors[nextCounterColor % colors.length];
        }
    }
}
