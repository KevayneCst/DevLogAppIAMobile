package com.ankk.motiontracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private TextView myText = null;
    private AccelerometerValues accVal = new AccelerometerValues(0, 0, 0);
    private GyroscopeValues gyrVal = new GyroscopeValues(0, 0, 0);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener(accListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(gyrListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //This is in the onCreate method
        LinearLayout lView = new LinearLayout(this);

        myText = new TextView(this);


        lView.addView(myText);

        setContentView(lView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(accListener);
        }
    }

    //Accelerometer Listener
    private SensorEventListener accListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // The acceleration may be negative, so take their absolute value
            float xAccValue = Math.abs(event.values[0]);
            float yAccValue = Math.abs(event.values[1]);
            float zAccValue = Math.abs(event.values[2]);

            accVal.setX(xAccValue);
            accVal.setY(yAccValue);
            accVal.setZ(zAccValue);

            myText.setText(accVal.toString() + "\n" + gyrVal.toString());
          }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    //Gyroscope Listener
    private SensorEventListener gyrListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // The acceleration may be negative, so take their absolute value
            float xGyrValue = Math.abs(event.values[0]);
            float yGyrValue = Math.abs(event.values[1]);
            float zGyrValue = Math.abs(event.values[2]);

            gyrVal.setX(xGyrValue);
            gyrVal.setY(yGyrValue);
            gyrVal.setZ(zGyrValue);

         }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}