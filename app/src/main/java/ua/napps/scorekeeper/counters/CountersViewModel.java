package ua.napps.scorekeeper.counters;

import android.app.Application;
import android.util.SparseIntArray;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.SnackbarMessage;
import ua.napps.scorekeeper.utils.livedata.SingleShotEvent;
import ua.napps.scorekeeper.utils.livedata.VibrateIntent;

class CountersViewModel extends AndroidViewModel {

    public final MutableLiveData<SingleShotEvent> eventBus = new MutableLiveData<>();

    private final CountersRepository repository;
    private final LiveData<List<Counter>> counters;
    private final String[] initialColors;
    private final String[] initialNames;
    private final Set<String> colorSet = new HashSet<>();
    private final Set<String> namesSet = new HashSet<>();
    private final SnackbarMessage snackbarMessage = new SnackbarMessage();

    CountersViewModel(Application application, CountersRepository countersRepository) {
        super(application);
        repository = countersRepository;
        counters = countersRepository.getCounters();
        initialColors = application.getResources().getStringArray(R.array.default_color_list);
        initialNames = application.getResources().getStringArray(R.array.names);
        shuffleInitialDataArrays();
    }

    public LiveData<Counter> getCounterLiveData(int counterID) {
        return repository.loadCounter(counterID);
    }

    void addCounter() {
        List<Counter> value = counters.getValue();
        if (value != null) {
            int size = value.size() + 1;
            String nextName = getNextName();
            String nextColor = getNextColor();
            repository.createCounter(nextName, nextColor, size)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onComplete() {
                            showSnackbarMessage(R.string.counter_added);
                            eventBus.postValue(new SingleShotEvent<>(new VibrateIntent()));
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Timber.e(e, "create counter");
                        }

                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }
                    });
        }
    }

    LiveData<List<Counter>> getCounters() {
        return counters;
    }

    void decreaseCounter(Counter counter, @IntRange(from = 0, to = Integer.MAX_VALUE) int amount) {
        repository.modifyCount(counter.getId(), -amount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                        eventBus.postValue(new SingleShotEvent<>(new VibrateIntent()));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e, "modifyCount counter");
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

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
                        eventBus.postValue(new SingleShotEvent<>(new VibrateIntent()));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e, "modifyCount counter");
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }
                });
    }

    void modifyName(Counter counter, @NonNull String newName) {
        if ((newName.equalsIgnoreCase("roman") |
                newName.equalsIgnoreCase("roma") |
                newName.equalsIgnoreCase("роман") |
                newName.equalsIgnoreCase("рома"))) {
            showSnackbarMessage(R.string.easter_wave);
        }
        repository.modifyName(counter.getId(), newName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onComplete() {
                        eventBus.postValue(new SingleShotEvent<>(new VibrateIntent()));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e, "modifyName counter");
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

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
                        eventBus.postValue(new SingleShotEvent<>(new VibrateIntent()));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

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
                        eventBus.postValue(new SingleShotEvent<>(new VibrateIntent()));
                        Singleton.getInstance().clearLogEntries();
                        shuffleInitialDataArrays();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e, "remove all");
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

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
                        eventBus.postValue(new SingleShotEvent<>(new VibrateIntent()));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e, "resetAll");
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

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
                        eventBus.postValue(new SingleShotEvent<>(new VibrateIntent()));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }
                });
    }

    private void shuffleInitialDataArrays() {
        Collections.shuffle(Arrays.asList(initialColors));
        Collections.shuffle(Arrays.asList(initialNames));

        colorSet.addAll(Arrays.asList(initialColors));
        namesSet.addAll(Arrays.asList(initialNames));
    }

    private String getNextColor() {
        if (colorSet.isEmpty()) {
            Collections.shuffle(Arrays.asList(initialColors));
            colorSet.addAll(Arrays.asList(initialColors));
        }

        String[] array = colorSet.toArray(new String[0]);

        Random rndm = new Random();
        int rndmNumber = rndm.nextInt(colorSet.size());
        String value = array[rndmNumber];
        colorSet.remove(value);
        return value;
    }

    private String getNextName() {
        if (namesSet.isEmpty()) {
            Collections.shuffle(Arrays.asList(initialNames));
            namesSet.addAll(Arrays.asList(initialNames));
        }

        String[] array = namesSet.toArray(new String[0]);

        Random rndm = new Random();
        int rndmNumber = rndm.nextInt(namesSet.size());
        String value = array[rndmNumber];
        namesSet.remove(value);

        return value;
    }

    public SnackbarMessage getSnackbarMessage() {
        return snackbarMessage;
    }

    private void showSnackbarMessage(int value) {
        snackbarMessage.setValue(value);
    }

    public void updatePositions() {
        List<Counter> counterList = counters.getValue();
        if (counterList == null) return;

        final SparseIntArray positionMap = new SparseIntArray();

        for (int i = 0; i < counterList.size(); i++) {
            int id = counterList.get(i).getId();
            positionMap.append(id, i + 1);
        }

        repository.modifyPositionBatch(positionMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnError(e -> Timber.e(e, "modifyPosition counter"))
                .onErrorComplete()
                .subscribe();
    }
}
