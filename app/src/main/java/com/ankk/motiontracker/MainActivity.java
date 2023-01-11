package com.ankk.motiontracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
//import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Button startButton;
    private Button stopButton;
    private Button runButton;
    private Button walkButton;
    private Button pauseButton;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    private FileWriter writer;
    private boolean recording;
    private int currentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        runButton = findViewById(R.id.run_button);
        walkButton = findViewById(R.id.walk_button);
        pauseButton = findViewById(R.id.pause_button);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
            }
        });

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentActivity = 2;
            }
        });

        walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentActivity = 1;
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentActivity = 0;
            }
        });

        //in order to ask the permissions whatever
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    private void startRecording() {
        if (!recording) {
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "sensor_data");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
                String currentDateAndTime = sdf.format(new Date());
                File file = new File(dir, "data_" +currentDateAndTime + ".csv");
                writer = new FileWriter(file);
                writer.append("Timestamp,Acc_X,Acc_Y,Acc_Z,Gyr_X,Gyr_Y,Gyr_Z,Activity\n");
                recording = true;

                String filePath = file.getAbsolutePath();
                Toast.makeText(MainActivity.this, "Started recording sensor data in file : " + filePath, Toast.LENGTH_SHORT).show();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        if (recording) {
            try {
                writer.flush();
                writer.close();
                recording = false;
                Toast.makeText(MainActivity.this, "Stopped recording sensor data", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (recording) {
            try {
                long timestamp = event.timestamp;
                float acc_x = event.values[0];
                float acc_y = event.values[1];
                float acc_z = event.values[2];
                float gyr_x = 0.0f;
                float gyr_y = 0.0f;
                float gyr_z = 0.0f;
                if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    gyr_x = event.values[0];
                    gyr_y = event.values[1];
                    gyr_z = event.values[2];
                }
                writer.append(timestamp + "," + acc_x + "," + acc_y + "," + acc_z + "," + gyr_x + "," + gyr_y + "," + gyr_z + "," + currentActivity + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}

