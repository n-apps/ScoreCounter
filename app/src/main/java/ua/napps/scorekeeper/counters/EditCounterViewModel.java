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
        if (newName.equals(counter.getName()) ) {
            return;
        }

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

        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CHARACTER, counter.getName());
        AndroidFirebaseAnalytics.logEvent("counter_name_submit", params);
    }

    void updateStep(int step) {
        if (step == counter.getStep() || step == 0) {
            return;
        }
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

    void updatePosition(int toPosition) {
        if (toPosition == counter.getPosition()) {
            return;
        }
        int fromPosition = counter.getPosition();

        List<Counter> countersList = countersLiveData.getValue();
        if (countersList != null) {
            if (toPosition > countersList.size() - 1) {
                toPosition = countersList.size() - 1;
            }

            int smallerIndex = Math.min(fromPosition, toPosition);
            int largerIndex = Math.max(fromPosition, toPosition);
            int moveStep;
            if (toPosition > fromPosition) {
                moveStep = -1;
            } else {
                moveStep = 1;
            }

            for (int i = 0; i < countersList.size(); i++) {
                if (countersList.get(i).getId() == counter.getId()) {
                    countersRepository.modifyPosition(counter.getId(), toPosition)
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
                } else if (countersList.get(i).getPosition() >= smallerIndex && countersList.get(i).getPosition() <= largerIndex) {
                    countersRepository.modifyPosition(countersList.get(i).getId(), countersList.get(i).getPosition() + moveStep)
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
}
