package com.ankk.motiontracker;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ankk.motiontracker.debug.DebugArray;
import com.ankk.motiontracker.prediction.PrepareDataTask;
import com.opencsv.CSVReader;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Button startButton;
    private Button stopButton;
    private Button runButton;
    private Button walkButton;
    private Button pauseButton;
    private TextView txtPrediction;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    private FileWriter writer;
    private FileWriter writer_pred;
    private boolean recording;
    private int currentActivity;

    private PrepareDataTask dataprocess;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        runButton = findViewById(R.id.run_button);
        walkButton = findViewById(R.id.walk_button);
        pauseButton = findViewById(R.id.pause_button);

        txtPrediction = findViewById(R.id.result_text);

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
                try {
                    stopRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                File predFile = new File(dir,"prediction_file.csv");
                writer = new FileWriter(file);
                writer.append("Timestamp,Acc_X,Acc_Y,Acc_Z,Gyr_X,Gyr_Y,Gyr_Z,Activity\n");

                writer_pred = new FileWriter(predFile);
                writer_pred.append("Timestamp,Acc_X,Acc_Y,Acc_Z,Gyr_X,Gyr_Y,Gyr_Z,Activity\n");
                recording = true;

                String filePath = file.getAbsolutePath();
                Toast.makeText(MainActivity.this, "Started recording sensor data in file : " + filePath, Toast.LENGTH_SHORT).show();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() throws IOException {

        Context context = this;

        if (recording) {
            try {
                writer.flush();
                writer.close();
                writer_pred.flush();
                writer_pred.close();


                PrepareDataTask dataprocess = new PrepareDataTask(MainActivity.this,10);
                dataprocess.execute();




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
                writer_pred.append(timestamp + "," + acc_x + "," + acc_y + "," + acc_z + "," + gyr_x + "," + gyr_y + "," + gyr_z + "," + currentActivity + "\n");
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


    private class PrepareDataTask extends AsyncTask<Void, Void, float[][][]>  {
        private Context context;
        private int n_past;

        public PrepareDataTask(Context context, int n_past) {
            this.context = context;
            this.n_past = n_past;
        }
        @Override
        protected float[][][] doInBackground(Void... voids) {
            try {
                // Load the data from the CSV file
                FileReader fileReader = new FileReader(Environment.getExternalStorageDirectory() + "/sensor_data/prediction_file.csv");
                CSVReader csvReader = new CSVReader(fileReader);

                List<String[]> allRows = csvReader.readAll();
                int n = allRows.size();
                float[][][] data = new float[n][n_past][6];

                for (int i = this.n_past+1; i < n; i++) {
                    for (int j=i-this.n_past;j<i;j++){
                        String[] row = allRows.get(j);
                        for (int k = 0; k < 6; k++) {
                            data[j][j+this.n_past-i][k] = Float.parseFloat(row[k+1]);
                        }
                    }
                }
                float[][] inputArray = data[10];


                DebugArray debugArray = new DebugArray(inputArray);
                debugArray.Print2DArray();
                float[][] output = new float[1][3];

                try (Interpreter interpreter = new Interpreter(loadModelFile(MainActivity.this))) {
                    interpreter.run(inputArray, output);
                }
                txtPrediction.setText(output.toString());

                return data;
            } catch (Exception e) {
                Log.e(TAG, "Error reading CSV file", e);
                return null;
            }
        }
        private MappedByteBuffer loadModelFile(Context context) throws IOException {
            AssetFileDescriptor fileDescriptor = context.getAssets().openFd("model.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }
}

