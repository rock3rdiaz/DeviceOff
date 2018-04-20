package com.example.rdiazb.deviceoff;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SensorsActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = SensorsActivity.class.getName();

    private static final float RELATIVE_HUMIDITY_VALUE = 99.5f;
    private static final int PERMISSION_REQUEST = 200;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor pressureSensor;
    private Sensor relativeHumiditySensor;

    private TextView aCoordX;
    private TextView aCoordY;
    private TextView aCoordZ;

    private TextView relativeHumidity;

    private Logger logger;
    private FileHandler loggerHandler;

    private boolean loggerEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        aCoordX = findViewById(R.id.aCoordX);
        aCoordY = findViewById(R.id.aCoordY);
        aCoordZ = findViewById(R.id.aCoordZ);

        relativeHumidity = findViewById(R.id.relativeHumidity);

        initLogger();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showAllSensorsPresent();
    }

    @Override
    protected void onResume() {
        sensorValidation();
        super.onResume();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            showAccelerometerData(event);
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE)
            showPressureData(event);
        if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY)
            showRelativeHumidityData(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "onAccuracyChanged");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST:
                if (grantResults.length > 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    initLogger();
        }
    }

    private void sensorValidation() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            Toast.makeText(this, "This devices not have a accelerometer sensor :(", Toast.LENGTH_SHORT).show();
        } else {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
            Toast.makeText(this, "This devices not have a pressure sensor :(", Toast.LENGTH_SHORT).show();
        } else {
            pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) == null) {
            Toast.makeText(this, "This devices not have a relative humidity sensor :(", Toast.LENGTH_SHORT).show();
        } else {
            relativeHumiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            sensorManager.registerListener(this, relativeHumiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void initLogger() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        } else {
            try {
                loggerHandler = new FileHandler(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                        "/data.txt", 100 * 1024, 1, true);
                logger = Logger.getLogger(TAG);
                logger.setLevel(Level.INFO);
                logger.addHandler(loggerHandler);

                loggerEnable = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAllSensorsPresent() {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor i : sensors) {
            Log.i(TAG, "sensor name: " + i.getName() + ", sensor vendor: " +
                    i.getVendor() + ", sensor version: " + i.getVersion() + ", sensor type: " + i.getType());
        }
    }

    private void showRelativeHumidityData(SensorEvent event) {
        Log.i(TAG, "relative humidity: " + event.values[0]);
        relativeHumidity.setText("RH: " + String.valueOf(event.values[0]));
        if (event.values[0] >= RELATIVE_HUMIDITY_VALUE)
            Toast.makeText(this, "water!!", Toast.LENGTH_SHORT).show();

    }

    private void showAccelerometerData(SensorEvent event) {
        aCoordX.setText("x: " + String.valueOf(event.values[0]));
        aCoordY.setText("y: " + String.valueOf(event.values[1]));
        aCoordZ.setText("z: " + String.valueOf(event.values[2]));
        if (isFalling(event.values))
            Log.i(TAG, "This device is falling!");
    }

    private void showPressureData(SensorEvent event) {
        //Log.i(TAG, "onSensorChanged. The sensor " + event.sensor.getName() + " has changed" +
        //       "The new valued are: " + event.values[0]);
    }

    private boolean isFalling(float[] axies) {
        double result = Math.sqrt(Math.pow(axies[0], 2) + Math.pow(axies[1], 2) + Math.pow(axies[2], 2));
        if (loggerEnable)
            logger.log(Level.INFO, String.valueOf(result));
        return result <= 0.05 && result >= 0.00;
    }
}
