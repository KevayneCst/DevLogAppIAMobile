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

private class PrepareDataTask extends AsyncTask<Void, Void, float[][]> {
    private Context context;
    private int n_past;

    public PrepareDataTask(Context context, int n_past) {
        this.context = context;
        this.n_past = n_past;
    }

    @Override
    protected float[][] doInBackground(Void... voids) {
        try (CSVReader reader = new CSVReader(new FileReader(Environment.getExternalStorageDirectory()+"sensor_data/prediction_file.csv"))){
            // Load the data from the CSV file
            List<String[]> r  =  reader.readAll();

            r.forEach(x -> {});

            // Select the last 100 lines of the data
            int nbr_line = n_past * 10;

            // Extract the features and target
            float[][] X = new float[records.size()][6];
            int[] y = new int[records.size()];
            for (int i = 0; i < records.size(); i++) {
                CSVRecord record = records.get(i);
                for (int j = 0; j < 6; j++) {
                    X[i][j] = Float.parseFloat(record.get(j + 1));
                }
                y[i] = Integer.parseInt(record.get(7));
            }

            // Reshape the data to match the input shape of the model
            int[] shape = {1, n_past, 6};
            X = Arrays.copyOf(X, shape);

            return X;
        } catch (Exception e) {
            Log.e(TAG, "Error reading CSV file", e);
        return null;
    }
