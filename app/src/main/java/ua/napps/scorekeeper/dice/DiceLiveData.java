package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.LiveData;

class DiceLiveData extends LiveData<Integer> {

    private int previousValue;

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
        return ((int) (Math.random() * 6)) + 1;
    }

}
