package ua.napps.scorekeeper.dice;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

class DiceViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final int diceVariant;

    DiceViewModelFactory(@IntRange(from = 0, to = 100) int diceVariant) {
        this.diceVariant = diceVariant;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new DiceViewModel(diceVariant);
    }
}
