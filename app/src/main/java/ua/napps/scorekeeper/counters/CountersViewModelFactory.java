package ua.napps.scorekeeper.counters;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import ua.napps.scorekeeper.storage.DatabaseHolder;

class CountersViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final Application app;
    private final CountersRepository repository;

    CountersViewModelFactory(Application scoreKeeperApp) {
        app = scoreKeeperApp;
        repository = new CountersRepository(DatabaseHolder.database().countersDao());
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new CountersViewModel(app, repository);
    }
}
