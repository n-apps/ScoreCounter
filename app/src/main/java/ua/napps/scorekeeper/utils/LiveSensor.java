package ua.napps.scorekeeper.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.lifecycle.LiveData;

public class LiveSensor extends LiveData<SensorEvent> implements SensorEventListener {

    private final SensorManager sensorManager;

    private final Sensor sensor;

    public LiveSensor(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        this.sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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