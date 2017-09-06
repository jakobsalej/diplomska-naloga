package com.example.jakob.qrreader;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import database.DatabaseHandler;
import database.OrderDocumentJSON;

import static android.R.attr.data;
import static android.R.attr.fragment;
import static com.example.jakob.qrreader.MonitorService.alerts;
import static com.example.jakob.qrreader.MonitorService.lastLon;
import static com.example.jakob.qrreader.RecyclerAdapterCommonAlerts.convertTime;


public class MonitoringActivity extends AppCompatActivity implements OnMapReadyCallback, AlertsFragment.OnFragmentInteractionListener {

    private static final String TAG = "MonitoringActivity";
    private static final int REQUEST_CHECK_SETTINGS = 1000;
    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    public LocationRequest mLocationRequest;
    public DateFormat mLastUpdateTime;
    public int locationInterval = 300;
    public static boolean serviceRunning = false;
    private int notificationId = 0;
    private TextView status, timeRunning, startTime, lastUpdateTime, temp, humidity, pressure, lat, lng, location;
    private GoogleMap map;
    private ArrayList<OrderDocumentJSON> dbData;
    private MonitoringActivity thisActiviy;

    private AlertsFragment fragment;
    public static String appName;

    private long timerTime = 0;
    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - timerTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timeRunning.setText(String.format("%d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);
        //setSupportActionBar(toolbar);

        thisActiviy = this;

        // get app name
        appName = getString(R.string.app_name);

        // get views
        status = (TextView) findViewById(R.id.textView_service_status);
        temp = (TextView) findViewById(R.id.textView_temp_latest);
        humidity = (TextView) findViewById(R.id.textView_humidity_latest);
        timeRunning = (TextView) findViewById(R.id.textView_time_running);
        startTime = (TextView) findViewById(R.id.textView_start_time);
        //lastUpdateTime = (TextView) findViewById(R.id.textView_last_update_time);
        location = (TextView) findViewById(R.id.textView_current_location);


        // start monitoring
        final Intent i = new Intent(this, MonitorService.class);
        startMonitoring(i, 0, null, 0);
        showNotification();
        //Snackbar.make(, "Monitor activity started!", Snackbar.LENGTH_LONG).setAction("Action", null).show();

        // timer
        if (MonitorService.serviceRunning) {
            timerTime = MonitorService.startTime;
            timerHandler.postDelayed(timerRunnable, 0);
        }

        // set location interval
        // TODO: get this from settings?
        final int timeDelta = locationInterval;

        // MAP
        GoogleMapOptions options = new GoogleMapOptions();
        options.ambientEnabled(true)
                .scrollGesturesEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapMonitoring);
        mapFragment.getMapAsync(this);

        // alerts fragment
        fragment = AlertsFragment.newInstance(null, null);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_holder_orders, fragment).commit();

        Log.v("ALERTS", String.valueOf(MonitorService.alerts.length()));

        // new service intent

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_stop_black_24px);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open scanning activity
                Intent intent = new Intent(thisActiviy, ReadQRActivity.class);
                intent.putExtra("title", "Stop order monitoring");
                intent.putExtra("text", "To stop monitoring, please scan order's QR code again or input document ID.");
                startActivity(intent);

            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        //mapView.onResume();
        Log.v(TAG, "On resume!!");

        // Register for the particular broadcast based on ACTION string
        IntentFilter ifilter = new IntentFilter(MonitorService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(monitorReceiver, ifilter);

        // set status
        serviceRunning = MonitorService.serviceRunning;
        if (serviceRunning) {
            status.setText("RUNNING");
        } else {
            status.setText("NOT RUNNING");
        }
        startTime.setText(convertTime(MonitorService.startTime));
        temp.setText("Latest temperature: " + Double.toString(MonitorService.lastTemp) + " °C");
        humidity.setText("Latest humidity: " + Double.toString(MonitorService.lastHumidity) + " %");
        location.setText(Double.toString(MonitorService.lastLat) + ", " + Double.toString(MonitorService.lastLon));

        /*
        humidity = (TextView) findViewById(R.id.textView_humidity_value);
        humidity.setText(Double.toString(MonitorService.lastHumidity));
        pressure = (TextView) findViewById(R.id.textView_pressure_value);
        pressure.setText(Double.toString(MonitorService.lastPressure));
        */

        // update alerts
        if (fragment != null) {
            fragment.updateFragmentData(null);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        //mapView.onPause();
        // Unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(this).unregisterReceiver(monitorReceiver);
    }


    // Define the callback for what to do when data is received
    private BroadcastReceiver monitorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            serviceRunning = intent.getBooleanExtra("status", false);

            // SERVICE STOPPED!!
            if (!serviceRunning) {
                status.setText("NOT RUNNING");
                timerHandler.removeCallbacks(timerRunnable);
                return;
            }
            String lastTime = intent.getStringExtra("lastTime");
            double lastLat = intent.getDoubleExtra("lastLat", -1);
            double lastLong = intent.getDoubleExtra("lastLon", -1);
            double lastTemp = intent.getDoubleExtra("lastTemp", -1);
            double lastHumidity = intent.getDoubleExtra("lastHumidity", -1);
            double lastPressure = intent.getDoubleExtra("lastPressure", -1);

            setLastValues(serviceRunning, lastTime, lastLat, lastLong, lastTemp, lastHumidity, lastPressure);
            fragment.updateFragmentData(null);
            Log.v(TAG, "Service running: " + serviceRunning);
        }
    };


    private void setLastValues(boolean serviceRunning, String lastTime, double lastLat, double lastLong, double lastTemp, double lastHumidity, double lastPressure) {
        // TODO: also set service running??
        //startTime.setText(lastTime);
        temp.setText("Latest temperature: " + Double.toString(lastTemp) + " °C");
        humidity.setText("Latest humidity: " + Double.toString(lastHumidity) + " %");
        location.setText(Double.toString(lastLat) + ", " + Double.toString(lastLon));
        //humidity.setText(Double.toString(lastHumidity));
        //pressure.setText(Double.toString(lastPressure));
    }


    // start backgroud service and send it json
    // MODE: 0 = start service, 1 = send update to the activity
    public void startMonitoring(Intent i, int mode, String data, int timeDelta) {
        i.putExtra("mode", mode);
        i.putExtra("data", data);
        i.putExtra("timeDelta", timeDelta);
        startService(i);
    }


    private void stopMonitoring(Intent i) {
        Log.v(TAG, "Stopping service from activity!!");
        final Intent in = new Intent(this, MonitorService.class);
        in.putExtra("status", false);
        startService(in);
    }


    public void showNotification() {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_assessment_black_24px)
                        .setContentTitle(appName)
                        .setContentText("Monitoring service is running!")
                        .setOngoing(true);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, Main2Activity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(Main2Activity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationId allows you to update the notification later on.
        mNotificationManager.notify(notificationId, mBuilder.build());
    }


    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        LatLng lj = new LatLng(46.0569, 14.5058);
        map.moveCamera(CameraUpdateFactory.newLatLng(lj));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);
    }


    public String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("HH:mm, dd.MM.yyyy");
        return format.format(date);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
