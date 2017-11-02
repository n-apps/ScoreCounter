package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.LiveData;

class DiceLiveData extends LiveData<Integer> {

    private int previousValue;

    DiceLiveData() {
        setValue(0);
    }

    public int getPreviousValue() {
        return previousValue;
    }

    @Override
    protected void onActive() {

    }

    @Override
    protected void onInactive() {

    }


    void rollDice() {
        previousValue = getValue();
        setValue(generateDieResult());
    }

    public int generateDieResult() {
        return ((int) (Math.random() * 6)) + 1;
    }

}
