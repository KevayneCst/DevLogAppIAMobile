package com.ankk.motiontracker;



import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MySensorService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private FileWriter writer;
    private FileWriter writer_pred;
    private boolean recording = false;

    private int currentActivity;

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getExtras()!=null){
            int activitylabel = intent.getExtras().getInt("currentActivity");
            currentActivity = activitylabel;
        }else {
            startRecording();
        }
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
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
                writer_pred.append(timestamp + "," + acc_x + "," + acc_y + "," + acc_z + "," + gyr_x + "," + gyr_y + "," + gyr_z + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                File file = new File(dir, "data_" +currentDateAndTime + ".csv");
                File predFile = new File(dir,"prediction_file.csv");
                writer = new FileWriter(file);
                writer.append("Timestamp,Acc_X,Acc_Y,Acc_Z,Gyr_X,Gyr_Y,Gyr_Z,Activity\n");

                writer_pred = new FileWriter(predFile);
                writer_pred.append("Timestamp,Acc_X,Acc_Y,Acc_Z,Gyr_X,Gyr_Y,Gyr_Z,Activity\n");
                recording = true;

                String filePath = file.getAbsolutePath();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startRecording() {
        if (!recording) {
            Toast.makeText(this, "start recording", Toast.LENGTH_SHORT).show();
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), "sensor_data");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
                String currentDateAndTime = sdf.format(new Date());
                File file = new File(dir, "data_" +currentDateAndTime + ".csv");
                File predFile = new File(dir,"prediction_file.csv");
                writer = new FileWriter(file);
                writer.append("Timestamp,Acc_X,Acc_Y,Acc_Z,Gyr_X,Gyr_Y,Gyr_Z,Activity\n");

                writer_pred = new FileWriter(predFile);
                writer_pred.append("Timestamp,Acc_X,Acc_Y,Acc_Z,Gyr_X,Gyr_Y,Gyr_Z,Activity\n");
                recording = true;



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        if (recording) {
            Toast.makeText(this, "stop recording", Toast.LENGTH_SHORT).show();
            try {
                writer.flush();
                writer.close();
                writer_pred.flush();
                writer_pred.close();
                recording = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}