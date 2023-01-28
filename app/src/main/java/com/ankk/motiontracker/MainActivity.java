package com.ankk.motiontracker;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
//import android.support.v7.app.AppCompatActivity;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import org.tensorflow.lite.Interpreter;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private Button stopButton;
    private Button runButton;
    private Button walkButton;
    private Button pauseButton;
    private Button processButton;

    private TextView predText;

    /*
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    private FileWriter writer;
    private FileWriter writer_pred;
    private boolean recording;
    private int currentActivity;
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intentDashboard = new Intent(this, DashboardActivity.class);


        /**
         * Buttons
         */
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        runButton = findViewById(R.id.run_button);
        walkButton = findViewById(R.id.walk_button);
        pauseButton = findViewById(R.id.pause_button);
        processButton = findViewById(R.id.process);


        predText = findViewById(R.id.result_text);

        /*
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
*/

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("[DEBUG] : I'm on onClick startButton");
                Intent serviceIntent = new Intent(MainActivity.this, MySensorService.class);
                startService(serviceIntent);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(MainActivity.this, MySensorService.class);
                stopService(serviceIntent);
            }
        });

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(MainActivity.this, MySensorService.class);
                serviceIntent.putExtra("currentActivity", 2);
                startService(serviceIntent);
            }
        });

        walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(MainActivity.this, MySensorService.class);
                serviceIntent.putExtra("currentActivity", 1);
                startService(serviceIntent);
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(MainActivity.this, MySensorService.class);
                serviceIntent.putExtra("currentActivity", 0);
            }
        });
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(MainActivity.this, MySensorService.class);
                stopService(serviceIntent);
                startActivity(intentDashboard);
            }
        });

        //in order to ask the permissions whatever
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
    }

}

