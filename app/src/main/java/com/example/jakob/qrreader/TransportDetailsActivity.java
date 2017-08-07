package com.example.jakob.qrreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.jakob.qrreader.R.id.chart;

public class TransportDetailsActivity extends AppCompatActivity {

    private LineChart lc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_details);

        lc = (LineChart) findViewById(R.id.chart_first);

        Intent intent = getIntent();
        String data = intent.getStringExtra("transport");
        Log.v("TRANSPORT", data);

        // prepare data for charts
        JSONArray measurements = null;
        try {
            JSONObject obj = new JSONObject(data);
            measurements = obj.getJSONArray("measurements");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<Entry> entries = new ArrayList<Entry>();

        for (int i = 0; i < measurements.length(); i++) {

            // turn your data into Entry objects
            try {
                JSONObject obj = (JSONObject) measurements.get(i);
                entries.add(new Entry(obj.getLong("time"), (float) obj.getJSONObject("weather").getDouble("temperature")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.v("ENTRIES", String.valueOf(entries));

        LineDataSet dataSet = new LineDataSet(entries, "Temperature over time"); // add entries to dataset
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //dataSet.setColor(...);
        //dataSet.setValueTextColor(...); // styling, ...

        LineData lineData = new LineData(dataSet);
        lc.setData(lineData);
        lc.invalidate(); // refresh


    }

    public String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }
}
