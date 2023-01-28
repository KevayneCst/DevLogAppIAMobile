package com.ankk.motiontracker;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.opencsv.CSVReader;

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

public class MySensorService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private FileWriter writer;
    private boolean recording = false;

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "start recording", Toast.LENGTH_SHORT).show();
        if(intent.getExtras()!=null){
            int currentActivity = intent.getExtras().getInt("currentActivity");
            startRecording(currentActivity);
        }else {
            System.out.println("[DEBUG] : onStartCommand - 'intent.getExtras()!=null'");
            startRecording();
        }
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Enregistrement des valeurs du capteur dans un fichier CSV
        try {
            writer.append(String.valueOf(event.timestamp));
            writer.append(",");
            for (float val : event.values) {
                writer.append(String.valueOf(val));
                writer.append(",");
            }
            writer.append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void startRecording(int currentActivity) {
        if (!recording) {
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "sensor_data");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
                String currentDateAndTime = sdf.format(new Date());
                File file = new File(dir, "data_" + currentDateAndTime + ".csv");
                writer = new FileWriter(file);
                writer.append("Timestamp,Acc_X,Acc_Y,Acc_Z,Activity\n");
                recording = true;
                writer.append(String.valueOf(currentActivity));
                writer.append(",");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startRecording() {
        System.out.println("[DEBUG] : I'm in startRecording");
        if (!recording) {
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "sensor_data");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
                String currentDateAndTime = sdf.format(new Date());
                File file = new File(dir, "data_" + currentDateAndTime + ".csv");
                writer = new FileWriter(file);
                writer.append("Timestamp,Acc_X,Acc_Y,Acc_Z,Activity\n");
                recording = true;
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

