package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.ViewModel;

public class DiceViewModel extends ViewModel {

    private final DiceLiveData diceResult = new DiceLiveData();

    DiceLiveData getDiceLiveData() {

        return diceResult;
    }

    public void rollDice() {
        diceResult.rollDice();
    }
}
