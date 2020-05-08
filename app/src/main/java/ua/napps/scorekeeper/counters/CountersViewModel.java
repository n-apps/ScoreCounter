package ua.napps.scorekeeper.counters;

import android.app.Application;
import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.SparseIntArray;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.Arrays;
import java.util.Collections;
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
import ua.napps.scorekeeper.utils.LiveSensor;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.SnackbarMessage;

class CountersViewModel extends AndroidViewModel {

    private final CountersRepository repository;
    private final LiveData<List<Counter>> counters;
    private final String[] colors;
    private final String[] names;
    private SnackbarMessage snackbarMessage = new SnackbarMessage();
    private LiveSensor sensorLiveData;

    CountersViewModel(Application application, CountersRepository countersRepository) {
        super(application);
        repository = countersRepository;
        counters = countersRepository.getCounters();
        colors = application.getResources().getStringArray(LocalSettings.isLightTheme() ? R.array.light : R.array.dark);
        names = application.getResources().getStringArray(R.array.names);
        shuffleInitialDataArrays();
    }

    public LiveData<Counter> getCounterLiveData(int counterID) {
        return repository.loadCounter(counterID);
    }

    void addCounter() {
        List<Counter> value = counters.getValue();
        if (value != null) {
            int size = value.size() + 1;
            repository.createCounter(getNextName(size), getNextColor(size), size)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onComplete() {
                            showSnackbarMessage(R.string.counter_added);
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

    void modifyName(Counter counter, @NonNull String newName) {

        if (newName.toLowerCase().equals("roman") |
                newName.toLowerCase().equals("роман") |
                newName.toLowerCase().equals("рома")) {
            showSnackbarMessage(R.string.easter_wave);
        }
        repository.modifyName(counter.getId(), newName)
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

    void modifyCurrentValue(Counter counter, @IntRange(from = 0, to = Integer.MAX_VALUE) int newValue) {
        if (newValue == counter.getValue()) {
            return;
        }

        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.SET, newValue, counter.getValue()));

        repository.setCount(counter.getId(), newValue)
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

    void modifyPosition(Counter counter, int fromIndex, int toIndex) {

        if (fromIndex == toIndex) return;

        List<Counter> counterList = counters.getValue();
        if (counterList == null) return;

        // Counter's position starts from 1, not from 0, so we should make + 1
        int fromPosition = fromIndex + 1;
        int toPosition = toIndex + 1;

        int movedCounterId = counter.getId();
        final SparseIntArray positionMap = buildPositionUpdate(counterList, movedCounterId, fromPosition, toPosition);

        repository.modifyPositionBatch(positionMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnError(e -> Timber.e(e, "modifyPosition counter"))
                .onErrorComplete()
                .subscribe();

    }

    private SparseIntArray buildPositionUpdate(@NonNull List<Counter> counterList, int movedCounterId, int fromPosition, int toPosition) {
        int smallerPosition = Math.min(fromPosition, toPosition);
        int largerPosition = Math.max(fromPosition, toPosition);
        int moveStep = toPosition > fromPosition ? -1 : 1;
        final SparseIntArray positionMap = new SparseIntArray();

        for (int i = 0; i < counterList.size(); i++) {

            int position = counterList.get(i).getPosition();

            if (position >= smallerPosition && position <= largerPosition) {
                int id = counterList.get(i).getId();
                int newPosition = id == movedCounterId ? toPosition : position + moveStep;
                positionMap.append(id, newPosition);
            }
        }
        return positionMap;
    }

    void removeAll() {
        repository.deleteAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                        Singleton.getInstance().clearLogEntries();
                        shuffleInitialDataArrays();
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

    void resetCounter(Counter counter) {
        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.RST, counter.getDefaultValue(), counter.getValue()));

        repository.setCount(counter.getId(), counter.getDefaultValue())
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

    private void shuffleInitialDataArrays() {
        Collections.shuffle(Arrays.asList(colors));
        Collections.shuffle(Arrays.asList(names));
    }

    private String getNextColor(int size) {
        if (size < colors.length) {
            return colors[size];
        } else {
            return colors[size % colors.length];
        }
    }

    private String getNextName(int size) {
        if (size < names.length) {
            return names[size].toLowerCase();
        } else {
            return names[size % names.length].toLowerCase();
        }
    }

    public SnackbarMessage getSnackbarMessage() {
        return snackbarMessage;
    }

    private void showSnackbarMessage(int value) {
        snackbarMessage.setValue(value);
    }

    public LiveData<SensorEvent> getSensorLiveData(Context context) {
        if (sensorLiveData == null) {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                sensorLiveData = new LiveSensor(sensorManager);
            }
        }
        return sensorLiveData;
    }

}
