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

    void rollDice() {
        this.rolls = DiceRoller.rollDice(diceSides, diceCount);
        setValue(rolls.stream().mapToInt(Integer::intValue).sum());
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
