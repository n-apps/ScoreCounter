package ua.napps.scorekeeper.dice;

import androidx.annotation.IntRange;
import androidx.lifecycle.LiveData;

class DiceLiveData extends LiveData<Integer> {

    private int diceSides;
    private int diceCount;

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
        int total = 0;
        for (int i = 0; i < diceCount; i++) {
            int roll = ((int) (Math.random() * diceSides)) + 1;
            total += roll;
        }
        setValue(total);
    }

    public void setDiceSides(@IntRange(from = 1, to = 100) int diceSides) {
        this.diceSides = diceSides;
    }

    public void setDiceCount(@IntRange(from = 1, to = 100) int diceCount) {
        this.diceCount = diceCount;
    }
}
