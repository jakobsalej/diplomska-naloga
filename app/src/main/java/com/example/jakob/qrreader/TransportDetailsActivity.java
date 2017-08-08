package com.example.jakob.qrreader;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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
import static java.lang.Long.getLong;

public class TransportDetailsActivity extends AppCompatActivity {

    private LineChart lc;
    private long baseTime;
    private TextView textViewDelivered, textViewStarted, textViewEnded, textViewDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_details);

        lc = (LineChart) findViewById(R.id.chart_first);
        textViewDelivered = (TextView) findViewById(R.id.textView_delivered);
        textViewStarted = (TextView) findViewById(R.id.textView_time_started);
        textViewEnded = (TextView) findViewById(R.id.textView_time_ended);
        textViewDuration = (TextView) findViewById(R.id.textView_time_duration);

        Intent intent = getIntent();
        String data = intent.getStringExtra("transport");
        double minTemp = intent.getDoubleExtra("minTemp", -10);
        double maxTemp = intent.getDoubleExtra("maxTemp", 30);
        Log.v("TRANSPORT", data);

        // prepare data
        JSONArray measurements = null;
        try {
            JSONObject obj = new JSONObject(data);
            measurements = obj.getJSONArray("measurements");
            int delivered = obj.getInt("delivered");
            long startDate = obj.getLong("startDate");
            long endDate = obj.getLong("endDate");
            long duration = obj.getLong("duration");
            setTextData(delivered, startDate, endDate, duration);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < measurements.length(); i++) {
            // turn your data into Entry objects
            try {
                JSONObject obj = (JSONObject) measurements.get(i);
                // get base time to later calculate time, float cant store long?
                if (i == 0) {
                    baseTime = obj.getLong("time");
                }

                String time = convertTime(obj.getLong("time"));
                Log.v("TIME", time);
                entries.add(new Entry(obj.getLong("time") - baseTime, (float) obj.getJSONObject("weather").getDouble("temperature")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.v("ENTRIES", String.valueOf(entries));
        Log.v("COMPARISON", String.valueOf(entries.size()) + " " + measurements.length());

        // CHART SETTINGS
        lc.getDescription().setEnabled(false);
        lc.getLegend().setEnabled(false);
        lc.setDrawGridBackground(false);
        lc.getAxisRight().setEnabled(false);

        // Y-axis
        YAxis y = lc.getAxisLeft();
        y.setDrawGridLines(false);
        y.setAxisMinimum((float) minTemp - 10);
        y.setAxisMaximum((float) maxTemp + 10);

        // Limit lines
        LimitLine lmax = new LimitLine((float) maxTemp);
        lmax.setLineColor(Color.RED);
        lmax.enableDashedLine(10f, 10f, 10f);
        lmax.setLabel("max");
        lmax.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        y.addLimitLine(lmax);

        LimitLine lmin = new LimitLine((float) minTemp);
        lmin.setLineColor(Color.RED);
        lmin.enableDashedLine(10f, 10f, 10f);
        lmin.setLabel("min");
        lmin.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        y.addLimitLine(lmin);

        // X-axis
        XAxis x = lc.getXAxis();
        x.setValueFormatter(new DateAxisValueFormatter(null, baseTime));
        x.setDrawGridLines(false);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);

        // data set
        LineDataSet dataSet = new LineDataSet(entries, "Temperature over time"); // add entries to dataset
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(2f);
        //dataSet.setColor(Color.rgb(244, 117, 117));
        //dataSet.setFillColor(Color.BLUE);
        //dataSet.setFillAlpha(100);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setDrawVerticalHighlightIndicator(false);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        lc.setData(lineData);
        lc.invalidate(); // refresh
    }

    private void setTextData(int delivered, long startDate, long endDate, long duration) {

        // delivered status
        if (delivered == 1) {
            textViewDelivered.setText("DELIVERED");
            textViewDelivered.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_check_black_24px, 0, 0, 0);
        } else {
            textViewDelivered.setText("NOT DELIVERED");
            textViewDelivered.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_clear_black_24px, 0, 0, 0);
        }

        // time
        textViewStarted.setText(convertTime(startDate));
        textViewEnded.setText(convertTime(endDate));
        textViewDuration.setText(String.valueOf(duration) + " h");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("HH:mm, dd.MM.yyyy");
        return format.format(date);
    }
}
