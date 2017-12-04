package ua.napps.scorekeeper.counters;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.support.annotation.IntRange;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import timber.log.Timber;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;

class CountersViewModel extends AndroidViewModel {

    private LiveData<List<Counter>> counters = new MutableLiveData<>();

    private int listSize;

    private final CountersRepository repository;

    CountersViewModel(Application application, CountersRepository countersRepository) {
        super(application);
        repository = countersRepository;
        counters = countersRepository.getCounters();
    }

    public LiveData<Counter> getCounterLiveData(int counterID) {
        return repository.loadCounter(counterID);
    }

    public void setListSize(final int listSize) {
        this.listSize = listSize;
    }

    void addCounter() {
        repository.createCounter(String.valueOf(listSize + 1),
                getRandomColor())
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
    }

    void decreaseCounter(Counter counter) {
        decreaseCounter(counter, -counter.getStep());
    }

    void decreaseCounter(Counter counter, @IntRange(from = Integer.MIN_VALUE, to = 0) int amount) {
        if (amount != -counter.getStep()) {
            Bundle params = new Bundle();
            params.putLong(Param.SCORE, amount);
            AndroidFirebaseAnalytics.logEvent(getApplication(), "decrease_counter", params);
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
            params.putLong(Param.SCORE, amount);
            AndroidFirebaseAnalytics.logEvent(getApplication(), "increase_counter", params);
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

    void removeAll() {
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

    private String getRandomColor() {
        String[] colors = getApplication().getResources().getStringArray(R.array.winter_palette);
        final int presetSize = colors.length;
        if (listSize < presetSize) {
            return colors[listSize];
        } else {
            return colors[listSize % presetSize];
        }

    }
}
