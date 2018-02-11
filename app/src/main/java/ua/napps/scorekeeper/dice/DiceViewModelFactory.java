package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

class DiceViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final int diceVariant;

    DiceViewModelFactory(int diceVariant) {
        this.diceVariant = diceVariant;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new DiceViewModel(diceVariant);
    }
}
