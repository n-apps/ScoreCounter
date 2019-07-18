package ua.napps.scorekeeper.counters;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ua.napps.scorekeeper.storage.DatabaseHolder;

class EditCounterViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final int counterId;
    private final CountersRepository repository;

    EditCounterViewModelFactory(int counterId) {
        repository = new CountersRepository(DatabaseHolder.database().countersDao());
        this.counterId = counterId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EditCounterViewModel(repository, counterId);
    }
}
