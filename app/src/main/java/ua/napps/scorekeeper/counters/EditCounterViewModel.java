package ua.napps.scorekeeper.counters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.utils.Singleton;

class EditCounterViewModel extends ViewModel {

    private final CountersRepository countersRepository;
    private final LiveData<Counter> counterLiveData;
    private final int id;
    private Counter counter;

    EditCounterViewModel(CountersRepository repository, final int counterId) {
        id = counterId;
        countersRepository = repository;
        counterLiveData = repository.loadCounter(counterId);
    }

    LiveData<Counter> getCounterLiveData() {
        return counterLiveData;
    }

    public void setCounter(@NonNull Counter c) {
        counter = c;
    }

    void updateColor(@Nullable String hex) {
        countersRepository.modifyColor(id, hex)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(createObserver("updateColor"));
    }

    void updateDefaultValue(int defaultValue) {
        countersRepository.modifyDefaultValue(id, defaultValue)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(createObserver("new defaultValue"));
    }

    void updateName(@NonNull String newName) {
        countersRepository.modifyName(id, newName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(createObserver("modifyName"));
    }

    void updateStep(int step) {
        countersRepository.modifyStep(id, step)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(createObserver("modifyStep"));
    }

    void updateValue(int value) {
        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.SET, value, counter.getValue()));

        countersRepository.setCount(id, value)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(createObserver("set new value"));
    }

    void deleteCounter() {
        countersRepository.delete(counter)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(createObserver("delete counter"));
    }

    private CompletableObserver createObserver(String operation) {
        return new CompletableObserver() {
            @Override
            public void onSubscribe(@NonNull Disposable d) { }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e(e, "%s failed", operation);
            }
        };
    }
}
