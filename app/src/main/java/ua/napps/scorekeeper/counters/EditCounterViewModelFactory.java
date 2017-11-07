package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

class EditCounterViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final int counterId;

    private final CountersRepository repository;

    EditCounterViewModelFactory(int counterId, CountersDao countersDao) {
        repository = new CountersRepository(countersDao);
        this.counterId = counterId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EditCounterViewModel(repository, counterId);
    }
}
