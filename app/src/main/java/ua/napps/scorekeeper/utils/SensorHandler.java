package ua.napps.scorekeeper.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class SensorHandler {

    private final SensorManager sensorManager;
    private final Sensor accelerometerSensor;
    private final ShakeLiveData shakeLiveData;

    private float currentAccel = SensorManager.GRAVITY_EARTH; // current acceleration including gravity
    private float accel = 0f; // acceleration apart from gravity
    private float shakeThreshold = 1.8f; // default shake threshold
    private long shakeCooldown = 1000; // cooldown in milliseconds
    private long lastShakeTimestamp = 0;

    private boolean isSensorActive = false;

    public SensorHandler(@NonNull SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        this.accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.shakeLiveData = new ShakeLiveData();
    }

    /**
     * Enables the sensor and starts listening for events.
     */
    public void enable() {
        if (!isSensorActive && accelerometerSensor != null) {
            sensorManager.registerListener(shakeLiveData, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            isSensorActive = true;
        }
    }

    /**
     * Disables the sensor and stops listening for events.
     */
    public void disable() {
        if (isSensorActive) {
            sensorManager.unregisterListener(shakeLiveData);
            isSensorActive = false;
        }
    }

    /**
     * Sets the shake detection threshold.
     *
     * @param threshold Minimum acceleration change to register as a shake.
     */
    public void setShakeThreshold(float threshold) {
        this.shakeThreshold = threshold;
    }

    /**
     * Sets the cooldown time for shake detection.
     *
     * @param cooldown Time in milliseconds between shake events.
     */
    public void setShakeCooldown(long cooldown) {
        this.shakeCooldown = cooldown;
    }

    /**
     * Provides a LiveData instance for observing shake events.
     *
     * @return LiveData to observe shake events.
     */
    public LiveData<SensorEvent> getSensorLiveData() {
        return shakeLiveData;
    }

    private class ShakeLiveData extends LiveData<SensorEvent> implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                // last acceleration including gravity
                float lastAccel = currentAccel;
                currentAccel = (float) Math.sqrt(x * x + y * y + z * z);
                float delta = currentAccel - lastAccel;
                accel = accel * 0.9f + delta; // low-pass filter

                if (accel > shakeThreshold && System.currentTimeMillis() - lastShakeTimestamp > shakeCooldown) {
                    lastShakeTimestamp = System.currentTimeMillis();
                    setValue(event); // Trigger the shake event
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // No-op
        }
    }
}