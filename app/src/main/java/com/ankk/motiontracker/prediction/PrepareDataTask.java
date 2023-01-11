package com.ankk.motiontracker.prediction;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;



import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PrepareDataTask extends AsyncTask<Void, Void, float[][][]> {
    private Context context;
    private int n_past;

    public PrepareDataTask(Context context, int n_past) {
        this.context = context;
        this.n_past = n_past;
    }

    @Override
    public float[][][] doInBackground(Void... voids) {
        try {
            // Load the data from the CSV file
            FileReader fileReader = new FileReader(Environment.getExternalStorageDirectory() + "sensor_data/prediction_file.csv");
            CSVReader csvReader = new CSVReader(fileReader);

            List<String[]> allRows = csvReader.readAll();
            int n = allRows.size();
            float[][][] data = new float[n][n_past][6];

            for (int i = this.n_past; i < n; i++) {
                for (int j=i-this.n_past;j<i;j++){
                    String[] row = allRows.get(j);
                    for (int k = 0; k < 6; k++) {
                        data[j][j+this.n_past-i][k] = Float.parseFloat(row[k]);
                    }
                }
            }


            return data;
        } catch (Exception e) {
            Log.e(TAG, "Error reading CSV file", e);
            return null;
        }
    }
}