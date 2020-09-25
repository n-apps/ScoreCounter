package ua.napps.scorekeeper.dice;

import androidx.annotation.IntRange;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;

class DiceLiveData extends LiveData<Integer> {

    private int diceSides;
    private int diceCount;
    private ArrayList<Integer> rolls;

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
        this.rolls = new ArrayList<>();
        int total = 0;

        for (int i = 0; i < diceCount; i++) {
            int roll = ((int) (Math.random() * diceSides)) + 1;
            total += roll;
            this.rolls.add(roll);
        }
        setValue(total);
    }

    public ArrayList<Integer> getRolls(){
        return this.rolls;
    }

    public void setDiceSides(@IntRange(from = 1, to = 100) int diceSides) {
        this.diceSides = diceSides;
    }

    public void setDiceCount(@IntRange(from = 1, to = 100) int diceCount) {
        this.diceCount = diceCount;
    }
}
