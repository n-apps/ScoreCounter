package ua.napps.scorekeeper.counters;

import android.app.Application;
import android.util.SparseIntArray;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Arrays;
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
import ua.napps.scorekeeper.utils.ViewUtil;
import ua.napps.scorekeeper.utils.livedata.SingleShotEvent;
import ua.napps.scorekeeper.utils.livedata.VibrateIntent;

class CountersViewModel extends AndroidViewModel {

    public final MutableLiveData<SingleShotEvent> eventBus = new MutableLiveData<>();

    private final CountersRepository repository;
    private final LiveData<List<Counter>> counters;

    private final Set<String> colorSet = new HashSet<>();
    private final Set<String> namesSet = new HashSet<>();
    private final SnackbarMessage snackbarMessage = new SnackbarMessage();

    CountersViewModel(Application application, CountersRepository countersRepository) {
        super(application);
        repository = countersRepository;
        counters = countersRepository.getCounters();
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
                    .subscribe(createDefaultObserver());
        }
    }

    LiveData<List<Counter>> getCounters() {
        return counters;
    }

    void decreaseCounter(Counter counter, @IntRange(from = 0, to = Integer.MAX_VALUE) int amount) {
        modifyCounter(counter, -amount);
    }

    void increaseCounter(Counter counter, @IntRange(from = 0, to = Integer.MAX_VALUE) int amount) {
        modifyCounter(counter, amount);
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
                .subscribe(createDefaultObserver());
    }

    void modifyCurrentValue(Counter counter, @IntRange(from = 0, to = Integer.MAX_VALUE) int newValue) {
        if (newValue == counter.getValue()) {
            return;
        }

        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.SET, newValue, counter.getValue()));

        repository.setCount(counter.getId(), newValue)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(createDefaultObserver());
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

    private void modifyCounter(Counter counter, int amount) {
        repository.modifyCount(counter.getId(), amount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(createDefaultObserver());
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
                .subscribe(createDefaultObserver());
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
                .subscribe(createDefaultObserver());
    }

    private String getNextColor() {
        return getNextRandom(colorSet, getApplication().getResources()
                .getStringArray(ViewUtil.isNightModeActive(getApplication()) ? R.array.list_of_colors_dark : R.array.list_of_colors_light), "#5A646D");
    }

    private String getNextName() {
        return getNextRandom(namesSet, getApplication().getResources()
                .getStringArray(R.array.names), getApplication().getResources().getString(R.string.counter_default_name));
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

    private void triggerVibration() {
        eventBus.postValue(new SingleShotEvent<>(new VibrateIntent()));
    }

    private CompletableObserver createDefaultObserver() {
        return new CompletableObserver() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onComplete() {
                eventBus.postValue(new SingleShotEvent<>(new VibrateIntent()));
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e(e, "subscriber error: %s", e.getMessage());
            }
        };
    }

    private String getNextRandom(Set<String> set, String[] defaultArray, String fallback) {
        if (set.isEmpty()) {
            set.addAll(Arrays.asList(defaultArray));
        }

        if (!set.isEmpty()) {
            String[] array = set.toArray(new String[0]);
            Random random = new Random();
            int index = random.nextInt(set.size());
            String value = array[index];
            set.remove(value);
            return value;
        }
        return fallback;
    }
}
