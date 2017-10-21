package ua.napps.scorekeeper.app;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v7.app.AppCompatDelegate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.lang.reflect.Type;
import java.util.ArrayList;
import timber.log.Timber;
import ua.com.napps.scorekeeper.BuildConfig;
import ua.napps.scorekeeper.counters.Counter;
import ua.napps.scorekeeper.counters.CountersRepository;
import ua.napps.scorekeeper.counters.OldCounter;
import ua.napps.scorekeeper.storage.DatabaseHolder;
import ua.napps.scorekeeper.storage.TinyDB;
import ua.napps.scorekeeper.utils.ColorUtil;

public class ScoreKeeperApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        DatabaseHolder.init(this);

        final int versionCode = getVersionCode();
        if (versionCode == 30000) {
            String activeCountersJson = new TinyDB(getApplicationContext()).getString("active_counters");
            Type listType = new TypeToken<ArrayList<OldCounter>>() {
            }.getType();
            ArrayList<OldCounter> oldCounters = new Gson().fromJson(activeCountersJson, listType);
            if (oldCounters != null) {
                ArrayList<Counter> newCounters = new ArrayList<>(oldCounters.size());
                for (final OldCounter oldCounter : oldCounters) {
                    final Counter c = new Counter(oldCounter.getCaption(),
                            ColorUtil.intColorToString(oldCounter.getColor()));
                    c.setDefaultValue(oldCounter.getDefValue());
                    c.setStep(oldCounter.getStep());
                    c.setValue(oldCounter.getValue());
                    newCounters.add(c);
                }
                if (!newCounters.isEmpty()) {
                    final CountersRepository repository = new CountersRepository(
                            DatabaseHolder.database().countersDao());
                    repository.insertAll(newCounters)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new CompletableObserver() {
                                @Override
                                public void onComplete() {
                                }

                                @Override
                                public void onError(Throwable e) {
                                }

                                @Override
                                public void onSubscribe(Disposable d) {

                                }
                            });
                }
            }
        }
    }

    private int getVersionCode() {
        int result = 0;
        try {
            result = getApplicationContext().getPackageManager().getPackageInfo(
                    getApplicationContext().getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // squelch
        }
        return result;
    }
}
