package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.LiveData;
import android.support.annotation.IntRange;

class DiceLiveData extends LiveData<Integer> {

    private int diceVariant;

    DiceLiveData() {
        setValue(0);
    }

    @Override
    protected void onActive() {
    }

    @Override
    protected void onInactive() {
    }

    void rollDice() {
        setValue(((int) (Math.random() * diceVariant)) + 1);
    }

    public void setDiceVariant(@IntRange(from = 1, to = 100) int diceVariant) {
        this.diceVariant = diceVariant;
    }
}
