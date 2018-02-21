package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.LiveData;
import android.support.annotation.IntRange;

class DiceLiveData extends LiveData<Integer> {

    private int previousValue;
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

    int getPreviousValue() {
        return previousValue;
    }

    void rollDice() {
        previousValue = getValue();
        setValue(generateDieResult());
    }

    private int generateDieResult() {
        return ((int) (Math.random() * diceVariant)) + 1;
    }

    public void setDiceVariant(@IntRange(from = 0, to = 100) int diceVariant) {
        this.diceVariant = diceVariant;
    }
}
