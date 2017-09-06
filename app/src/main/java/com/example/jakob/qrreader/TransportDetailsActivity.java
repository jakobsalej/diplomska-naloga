package com.example.jakob.qrreader;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.data;
import static android.R.attr.entries;
import static android.R.attr.fragment;
import static android.graphics.Color.rgb;
import static android.os.Build.VERSION_CODES.N;


public class TransportDetailsActivity extends AppCompatActivity implements CommonItemFragment.OnFragmentInteractionListener, OnMapReadyCallback {

    private LineChart lc, lc2;
    private long baseTime;
    private MapView mapView;
    private GoogleMap map;
    private TextView textViewDelivered, textViewStarted, textViewEnded, textViewDuration,
            textViewVehicle, textViewDriver, textViewComment;
    private JSONArray alertsArray;
    private String data;


    @Override
    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_details);

        lc = (LineChart) findViewById(R.id.chart_first);
        lc2 = (LineChart) findViewById(R.id.chart_second);
        textViewDelivered = (TextView) findViewById(R.id.textView_delivered);
        textViewStarted = (TextView) findViewById(R.id.textView_time_started);
        textViewEnded = (TextView) findViewById(R.id.textView_time_ended);
        //textViewDuration = (TextView) findViewById(R.id.textView_time_duration);
        textViewVehicle = (TextView) findViewById(R.id.textView_vehicle_info);
        textViewDriver = (TextView) findViewById(R.id.textView_driver_info);
        textViewComment = (TextView) findViewById(R.id.textView_comment);

        // MAP
        GoogleMapOptions options = new GoogleMapOptions();
        options.ambientEnabled(true)
                .scrollGesturesEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapF);
        mapFragment.getMapAsync(this);


        Intent intent = getIntent();
        data = intent.getStringExtra("transport");
        double minTemp = intent.getDoubleExtra("minTemp", -10);
        double maxTemp = intent.getDoubleExtra("maxTemp", 30);
        Log.v("TRANSPORT", data);

        // prepare data
        JSONArray measurements = null;
        try {
            JSONObject obj = new JSONObject(data);
            measurements = obj.getJSONArray("measurements");

            // set view
            int delivered = obj.getInt("delivered");
            long startDate = obj.getLong("startDate");
            long endDate = obj.getLong("endDate");
            long duration = obj.getLong("duration");
            int vehicleInfo = obj.getInt("vehicleType");
            String vehicle = null;
            if (vehicleInfo == 0) {
                vehicle = "normal";
            } else {
                vehicle = "with cooler";
            }
            String vehicleNo = obj.getString("vehicleReg");
            String vehicleData = vehicleNo + " (type: " + vehicle + ")";
            String driverInfo = obj.getString("driverName") + " (ID: " + obj.getInt("driverID") + ")";
            String comment = obj.getString("text");
            setTextData(delivered, startDate, endDate, duration, vehicleData, driverInfo, comment);

            // get alerts
            alertsArray = obj.getJSONArray("alerts");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<Entry> entriesTemperature = new ArrayList<Entry>();
        List<Entry> entriesHumidity = new ArrayList<Entry>();
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
                entriesTemperature.add(new Entry(obj.getLong("time") - baseTime, (float) obj.getJSONObject("weather").getDouble("temperature")));
                entriesHumidity.add(new Entry(obj.getLong("time") - baseTime, (float) obj.getJSONObject("weather").getDouble("humidity")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.v("ENTRIES", String.valueOf(entriesTemperature));
        Log.v("COMPARISON", String.valueOf(entriesTemperature.size()) + " " + measurements.length());

        // CHART SETTINGS TEMP CHART
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
        LineDataSet dataSetTemp = new LineDataSet(entriesTemperature, "Temperature over time"); // add entriesTemperature to dataset
        dataSetTemp.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //dataSetTemp.setDrawFilled(true);
        dataSetTemp.setDrawCircles(false);
        dataSetTemp.setLineWidth(2f);
        dataSetTemp.setColor(rgb(3,169,244));
        //dataSetTemp.setFillColor(Color.BLUE);
        //dataSetTemp.setFillAlpha(100);
        dataSetTemp.setDrawHorizontalHighlightIndicator(false);
        dataSetTemp.setDrawVerticalHighlightIndicator(false);
        dataSetTemp.setDrawValues(false);

        LineData lineData = new LineData(dataSetTemp);
        lc.setData(lineData);
        lc.invalidate(); // refresh


        // CHART SETTINGS HUMIDITY CHART
        lc2.getDescription().setEnabled(false);
        lc2.getLegend().setEnabled(false);
        lc2.setDrawGridBackground(false);
        lc2.getAxisRight().setEnabled(false);

        // Y-axis
        YAxis y2 = lc2.getAxisLeft();
        y2.setDrawGridLines(false);
        y2.setAxisMinimum(0);
        y2.setAxisMaximum(100);
        
        // X-axis
        XAxis x2 = lc2.getXAxis();
        x2.setValueFormatter(new DateAxisValueFormatter(null, baseTime));
        x2.setDrawGridLines(false);
        x2.setPosition(XAxis.XAxisPosition.BOTTOM);

        // data set
        LineDataSet dataSetHum = new LineDataSet(entriesHumidity, "Humidity level over time"); // add entriesTemperature to dataset
        dataSetHum.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetHum.setDrawFilled(true);
        dataSetHum.setDrawCircles(false);
        dataSetHum.setLineWidth(2f);
        dataSetHum.setColor(rgb(3,169,244));
        dataSetHum.setFillColor(rgb(3,169,244));
        dataSetHum.setFillAlpha(100);
        dataSetHum.setDrawHorizontalHighlightIndicator(false);
        dataSetHum.setDrawVerticalHighlightIndicator(false);
        dataSetHum.setDrawValues(false);

        LineData lineData2 = new LineData(dataSetHum);
        lc2.setData(lineData2);
        lc2.invalidate(); // refresh
        
        
        // alerts fragment
        if (alertsArray != null && alertsArray.length() > 0) {
            CommonItemFragment fragment = CommonItemFragment.newInstance("alerts", alertsArray.toString());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_holder_alerts, fragment).commit();
        }
    }


    private void setTextData(int delivered, long startDate, long endDate, long duration, String vehicleData, String driverInfo, String comment) {

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
        //textViewDuration.setText(String.valueOf(duration) + " h");
        textViewVehicle.setText(vehicleData);
        textViewDriver.setText(driverInfo);
        textViewComment.setText(comment);
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


    @Override
    public void onMapReady(GoogleMap map) {
        LatLng lj = new LatLng(46.0569, 14.5058);
        map.moveCamera(CameraUpdateFactory.newLatLng(lj));

        // display alerts
        addAlertsToMap(map);

    }


    private void addAlertsToMap(GoogleMap map) {

        // bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        try {
            JSONObject obj = new JSONObject(data);
            JSONArray alerts = obj.getJSONArray("alerts");

            for (int i = 0; i < alerts.length(); i++) {
                JSONObject alert = alerts.getJSONObject(i);
                JSONObject loc = alert.getJSONObject("location");
                loc.getDouble("x");

                LatLng position = new LatLng(loc.getDouble("x"), loc.getDouble("y"));
                map.addMarker(new MarkerOptions()
                        .position(position)
                        .title("ALERT " + (i+1))
                        .snippet("Temp: " + alert.getDouble("measurementValue"))
                );

                builder.include(position);
            }

            LatLngBounds bounds = builder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 300);
            map.animateCamera(cameraUpdate);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
