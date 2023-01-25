package com.ankk.motiontracker;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.opencsv.CSVReader;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DashboardActivity extends AppCompatActivity {
    private TextView walkingTextView;
    private TextView runningTextView;
    private TextView nothingTextView;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        walkingTextView = findViewById(R.id.walking_text_view);
        runningTextView = findViewById(R.id.running_text_view);
        nothingTextView = findViewById(R.id.nothing_text_view);
        pieChart = findViewById(R.id.pie_chart);

        PrepareDataTask dataprocess = new PrepareDataTask(DashboardActivity.this,10);
        dataprocess.execute();

        /**
        // Display the predictions as a pie chart
        List<PieEntry> entries = new ArrayList<>();
        if (walkingSeconds > 0) {
            float walkingPercentage = (float) walkingSeconds / totalSeconds;
            entries.add(new PieEntry(walkingPercentage, "Walking"));
        }
        if (runningSeconds > 0) {
            float runningPercentage = (float) runningSeconds / totalSeconds;
            entries.add(new PieEntry(runningPercentage, "Running"));
        }
        if (nothingSeconds > 0) {
            float nothingPercentage = (float) nothingSeconds / totalSeconds;
            entries.add(new PieEntry(nothingPercentage, "Nothing"));
        }

        //contiuation
        PieDataSet dataSet = new PieDataSet(entries, "Activities");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDescription(null);
        pieChart.setHoleRadius(0);
        pieChart.setTransparentCircleRadius(0);
        pieChart.animateY(1000);
        pieChart.invalidate();
         **/
    }
    private class PrepareDataTask extends AsyncTask<Void, Void, float[][][]> {
        private Context context;
        private int n_past;

        public PrepareDataTask(Context context, int n_past) {
            this.context = context;
            this.n_past = n_past;
        }
        @Override
        protected float[][][] doInBackground(Void... voids) {
            int quantityWalking=0;
            int quantityRunning=0;
            int quantityNothing = 0;
            int sizeDataset = 0;
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

                sizeDataset = data.length;
                for (int j = 0;j<data.length;j++){
                    float[][] inputArray = data[j];
                    ActivityClassifier activityClassifier = new ActivityClassifier(DashboardActivity.this);
                    ActivityClassifier.ActivityType pred = activityClassifier.classifyActivity(inputArray);
                    switch (pred){
                        case NOTHING:
                            quantityNothing++;
                            break;
                        case WALKING:
                            quantityWalking++;
                            break;
                        case RUNNING:
                            quantityRunning++;
                            break;
                        default: //DO NOTHING;
                    }
                    System.out.println("j:"+j+"n:"+quantityNothing+" w:"+quantityWalking+" r:"+quantityRunning+" tot:"+sizeDataset);
                }
                List<PieEntry> entries = new ArrayList<>();
                if (quantityNothing > 0) {
                    float nothingPercentage = (float) quantityNothing / sizeDataset;
                    System.out.println(nothingPercentage);
                    entries.add(new PieEntry(nothingPercentage, "Nothing"));
                }
                if (quantityWalking > 0) {
                    float walkingPercentage = (float) quantityWalking / sizeDataset;
                    System.out.println(walkingPercentage);
                    entries.add(new PieEntry(walkingPercentage, "Walking"));
                }
                if (quantityRunning > 0) {
                    float  runningPercentage = (float) quantityRunning / sizeDataset;
                    System.out.println(runningPercentage);
                    entries.add(new PieEntry(runningPercentage, "Running"));
                }

                //contiuation
                PieDataSet dataSet = new PieDataSet(entries, "Activities");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                PieData dataPie = new PieData(dataSet);
                pieChart.setData(dataPie);
                pieChart.setDescription(null);
                pieChart.setHoleRadius(0);
                pieChart.setTransparentCircleRadius(0);
                pieChart.invalidate();

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
