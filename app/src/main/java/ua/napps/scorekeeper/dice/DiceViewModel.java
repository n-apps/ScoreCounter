package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class DiceViewModel extends ViewModel {

    private final DiceLiveData diceResult = new DiceLiveData();

    private LiveSensor sensorLiveData;

    public DiceViewModel(int diceVariant) {
        diceResult.setDiceVariant(diceVariant);
    }

    public LiveData<SensorEvent> getSensorLiveData(Context context) {
        if (sensorLiveData == null) {

            sensorLiveData = new LiveSensor((SensorManager) context.getSystemService(Context.SENSOR_SERVICE),
                    Sensor.TYPE_ACCELEROMETER);
        }
        return sensorLiveData;
    }

    public void rollDice() {
        diceResult.rollDice();
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
