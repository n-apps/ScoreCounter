package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.ViewModel;

public class DiceViewModel extends ViewModel {

    private DiceLiveData diceResult = new DiceLiveData();

    public void rollDice() {
        diceResult.rollDice();
    }


    public DiceLiveData getDiceLiveData() {

        return diceResult;
    }
}
