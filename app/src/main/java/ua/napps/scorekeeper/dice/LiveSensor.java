package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.LiveData;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class LiveSensor extends LiveData<SensorEvent> implements SensorEventListener {

    public final SensorManager sensorManager;

    private final Sensor sensor;

    public LiveSensor(SensorManager sensorManager, int type) {
        this.sensorManager = sensorManager;
        this.sensor = sensorManager.getDefaultSensor(type);
    }

    @Override
    protected void onActive() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onInactive() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        setValue(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}