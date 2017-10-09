package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class EditCounterViewModelFactory extends ViewModelProvider.NewInstanceFactory {

  private final int counterId;
  private final CountersRepository repository;

  EditCounterViewModelFactory(int productId, CountersDao countersDao) {
    repository = new CountersRepository(countersDao);
    counterId = productId;
  }

  @NonNull @Override public <T extends ViewModel> T create(Class<T> modelClass) {
    //noinspection unchecked
    return (T) new EditCounterViewModel(repository, counterId);
  }
}
