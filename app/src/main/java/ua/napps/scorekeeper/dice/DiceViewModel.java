package ua.napps.scorekeeper.dice;

import androidx.annotation.IntRange;
import androidx.lifecycle.ViewModel;

class DiceViewModel extends ViewModel {

    private final DiceLiveData diceResult = new DiceLiveData();

    DiceViewModel(@IntRange(from = 1, to = 100) int diceVariant, @IntRange(from = 1, to = 100) int diceCount) {
        setDiceMaxSide(diceVariant);
        setDiceCount(diceCount);
    }

    public void setDiceMaxSide(@IntRange(from = 1, to = 100) int diceVariant) {
        diceResult.setDiceSides(diceVariant);
    }

    public void setDiceCount(@IntRange(from = 1, to = 100) int diceCount) {
        diceResult.setDiceCount(diceCount);
    }

    public void rollDice() {
        diceResult.rollDice();
    }

    DiceLiveData getDiceLiveData() {
        return diceResult;
    }
}
