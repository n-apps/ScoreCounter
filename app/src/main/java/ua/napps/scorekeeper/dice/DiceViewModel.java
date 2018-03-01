package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.annotation.IntRange;

class DiceViewModel extends ViewModel {

    private final DiceLiveData diceResult = new DiceLiveData();

    private LiveSensor sensorLiveData;

    DiceViewModel(@IntRange(from = 0, to = 100) int diceVariant, int previousResult) {
        diceResult.setDiceVariant(diceVariant);
        diceResult.setPreviousResult(previousResult);
    }

    public LiveData<SensorEvent> getSensorLiveData(Context context) {
        enableLiveSensor(context);
        return sensorLiveData;
    }

    public void enableLiveSensor(Context context) {
        if (sensorLiveData == null) {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                sensorLiveData = new LiveSensor(sensorManager);
            }
        }
    }

    public void rollDice() {
        diceResult.rollDice();
    }

    public void updateDiceVariant(int diceVariant) {
        diceResult.setDiceVariant(diceVariant);
    }

    public void disableSensor() {
        if (sensorLiveData != null) {
            sensorLiveData.disableSensor();
        }
        sensorLiveData = null;
    }

    DiceLiveData getDiceLiveData() {
        return diceResult;
    }
}
