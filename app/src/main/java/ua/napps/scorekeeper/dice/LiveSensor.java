package ua.napps.scorekeeper.dice;

import androidx.lifecycle.LiveData;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

class LiveSensor extends LiveData<SensorEvent> implements SensorEventListener {

    private final SensorManager sensorManager;

    private final Sensor sensor;

    LiveSensor(SensorManager sensorManager) {
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