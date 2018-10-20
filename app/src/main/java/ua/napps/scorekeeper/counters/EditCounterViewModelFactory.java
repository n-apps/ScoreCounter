package ua.napps.scorekeeper.counters;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import ua.napps.scorekeeper.storage.DatabaseHolder;

class EditCounterViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final int counterId;
    private final CountersRepository repository;
    private final EditCounterViewModel.EditCounterViewModelCallback callback;

    EditCounterViewModelFactory(int counterId, EditCounterViewModel.EditCounterViewModelCallback callback) {
        repository = new CountersRepository(DatabaseHolder.database().countersDao());
        this.counterId = counterId;
        this.callback = callback;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EditCounterViewModel(repository, counterId, callback);
    }
}
