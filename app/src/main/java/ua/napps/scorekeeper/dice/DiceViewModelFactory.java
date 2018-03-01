package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

class DiceViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final int diceVariant;
    private final int previousResult;

    DiceViewModelFactory(@IntRange(from = 0, to = 100) int diceVariant, int previousResult) {
        this.diceVariant = diceVariant;
        this.previousResult = previousResult;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new DiceViewModel(diceVariant, previousResult);
    }
}
