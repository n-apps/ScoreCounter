package ua.napps.scorekeeper.dice;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import androidx.annotation.IntRange;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import ua.napps.scorekeeper.utils.LiveSensor;

class DiceViewModel extends ViewModel {

    private final DiceLiveData diceResult = new DiceLiveData();
    private LiveSensor sensorLiveData;

    DiceViewModel(@IntRange(from = 1, to = 100) int diceVariant) {
        setDiceVariant(diceVariant);
    }

    public void setDiceVariant(@IntRange(from = 1, to = 100) int diceVariant) {
        diceResult.setDiceVariant(diceVariant);
    }

    public LiveData<SensorEvent> getSensorLiveData(Context context) {
        if (sensorLiveData == null) {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                sensorLiveData = new LiveSensor(sensorManager);
            }
        }
        return sensorLiveData;
    }

    public void rollDice() {
        diceResult.rollDice();
    }

    DiceLiveData getDiceLiveData() {
        return diceResult;
    }
}
