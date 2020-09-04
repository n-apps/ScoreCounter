package ua.napps.scorekeeper.dice;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

class DiceViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final int diceVariant;
    private final int diceCount;

    DiceViewModelFactory(@IntRange(from = 0, to = 100) int diceVariant, @IntRange(from = 1, to = 100) int diceCount) {
        this.diceVariant = diceVariant;
        this.diceCount = diceCount;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new DiceViewModel(diceVariant, diceCount);
    }
}
