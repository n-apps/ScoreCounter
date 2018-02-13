package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

class EditCounterViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final int counterId;

    private final CountersRepository repository;

    private final EditCounterViewModel.EditCounterViewModelCallback callback;

    EditCounterViewModelFactory(int counterId, CountersDao countersDao, EditCounterViewModel.EditCounterViewModelCallback callback) {
        repository = new CountersRepository(countersDao);
        this.counterId = counterId;
        this.callback = callback;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EditCounterViewModel(repository, counterId, callback);
    }
}
