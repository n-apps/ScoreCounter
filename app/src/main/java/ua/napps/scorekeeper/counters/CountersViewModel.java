package ua.napps.scorekeeper.counters;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.firebase.analytics.FirebaseAnalytics.Param;

import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.Singleton;

class CountersViewModel extends AndroidViewModel {

    private final CountersRepository repository;
    private final LiveData<List<Counter>> counters;
    private final String[] colors;

    CountersViewModel(Application application, CountersRepository countersRepository) {
        super(application);
        repository = countersRepository;
        counters = countersRepository.getCounters();
        colors = application.getResources().getStringArray(LocalSettings.isLightTheme() ? R.array.light : R.array.dark);
    }

    public LiveData<Counter> getCounterLiveData(int counterID) {
        return repository.loadCounter(counterID);
    }

    void addCounter() {
        List<Counter> value = counters.getValue();
        if (value != null) {
            int size = value.size() + 1;
            repository.createCounter('#' + String.valueOf(size), getNextColor(size), size)
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
    }

    LiveData<List<Counter>> getCounters() {
        return counters;
    }

    void decreaseCounter(Counter counter, @IntRange(from = Integer.MIN_VALUE, to = 0) int amount) {
        if (amount != -counter.getStep()) {
            Bundle params = new Bundle();
            params.putString(Param.CHARACTER, "" + amount);
            AndroidFirebaseAnalytics.logEvent("CountersScreenCounterDecreased", params);
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

    void increaseCounter(Counter counter, @IntRange(from = 0, to = Integer.MAX_VALUE) int amount) {
        if (amount != counter.getStep()) {
            Bundle params = new Bundle();
            params.putString(Param.CHARACTER, "" + amount);
            AndroidFirebaseAnalytics.logEvent("CountersScreenCounterIncreased", params);
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
            params.putString(Param.CHARACTER, name);
            AndroidFirebaseAnalytics.logEvent("CountersScreenCounterNewNameSubmit", params);
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

    void setPositionAfterDBMigration(Counter counter, int position) {
        repository.modifyPosition(counter.getId(), position)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "modifyPosition counter");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    void modifyPosition(Counter counter, int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        if (toPosition == counter.getPosition()) {
            return;
        }

        if (toPosition > counters.getValue().size() - 1) {
            toPosition = counters.getValue().size() - 1;
        }

        AndroidFirebaseAnalytics.logEvent("CountersScreenCounterDragNDropped");

        int smallerIndex = Math.min(fromPosition, toPosition);
        int largerIndex = Math.max(fromPosition, toPosition);
        int moveStep;

        if (toPosition > fromPosition) {
            moveStep = -1;
        } else {
            moveStep = 1;
        }

        List<Counter> counterList = counters.getValue();
        if (counterList != null) {
            for (int i = 0; i < counterList.size(); i++) {
                if (counterList.get(i).getId() == counter.getId()) {
                    repository.modifyPosition(counter.getId(), toPosition)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new CompletableObserver() {
                                @Override
                                public void onComplete() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Timber.e(e, "modifyPosition counter");
                                }

                                @Override
                                public void onSubscribe(Disposable d) {

                                }
                            });
                } else if (counterList.get(i).getPosition() >= smallerIndex && counterList.get(i).getPosition() <= largerIndex) {
                    repository.modifyPosition(counterList.get(i).getId(), counterList.get(i).getPosition() + moveStep)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new CompletableObserver() {
                                @Override
                                public void onComplete() {
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Timber.e(e, "modifyPosition counter");
                                }

                                @Override
                                public void onSubscribe(Disposable d) {

                                }
                            });
                }
            }
        }

    }

    void removeAll() {
        repository.deleteAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                        Singleton.getInstance().clearLogEntries();
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

    private String getNextColor(int size) {
        if (size < colors.length) {
            return colors[size];
        } else {
            return colors[size % colors.length];
        }
    }
}
