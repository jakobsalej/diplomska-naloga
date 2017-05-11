package com.example.jakob.qrreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.data;

public class ShowGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_graph);

        final LineChart chart = (LineChart) findViewById(R.id.chart);

        // adding some fake data
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry(0, (float) 14.6));
        entries.add(new Entry(1,(float) 12));
        entries.add(new Entry(2, (float) 12.8));
        entries.add(new Entry(3, (float) 13.6));
        entries.add(new Entry(4, (float) 12.6));
        entries.add(new Entry(5, (float) 15.6));

        List<String> labels = new ArrayList<String>();
        labels.add("12:50");
        labels.add("12:55");
        labels.add("13:00");
        labels.add("13:05");
        labels.add("13:10");
        labels.add("13:15");

        // set data
        LineDataSet dataSet = new LineDataSet(entries, "Temperature");

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh




    }
}
