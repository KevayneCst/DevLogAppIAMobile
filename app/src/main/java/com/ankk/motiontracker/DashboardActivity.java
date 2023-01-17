package com.ankk.motiontracker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

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

        // Get the predictions from the intent
        Bundle extras = getIntent().getExtras();
        int walkingSeconds = extras.getInt("walking_seconds");
        int runningSeconds = extras.getInt("running_seconds");
        int nothingSeconds = extras.getInt("nothing_seconds");
        int totalSeconds = walkingSeconds + runningSeconds + nothingSeconds;

        // Format the predictions as minutes and seconds
        String walkingTime = String.format("%d min, %d sec",
                TimeUnit.SECONDS.toMinutes(walkingSeconds),
                walkingSeconds % 60);
        String runningTime = String.format("%d min, %d sec",
                TimeUnit.SECONDS.toMinutes(runningSeconds),
                runningSeconds % 60);
        String nothingTime = String.format("%d min, %d sec",
                TimeUnit.SECONDS.toMinutes(nothingSeconds),
                nothingSeconds % 60);

        // Display the predictions on the dashboard
        walkingTextView.setText(walkingTime);
        runningTextView.setText(runningTime);
        nothingTextView.setText(nothingTime);

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
    }
}
