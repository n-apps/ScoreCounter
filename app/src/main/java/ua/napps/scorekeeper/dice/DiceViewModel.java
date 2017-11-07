package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.ViewModel;

class DiceViewModel extends ViewModel {

    private final DiceLiveData diceResult = new DiceLiveData();

    DiceLiveData getDiceLiveData() {

        return diceResult;
    }

    void rollDice() {
        diceResult.rollDice();
    }
}
