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
import android.os.Bundle;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;

import static com.example.jakob.qrreader.ReadQRActivity.DB_DATA;

public class MonitoringActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "MonitoringActivity";
    private static final int REQUEST_CHECK_SETTINGS = 1000;
    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    public LocationRequest mLocationRequest;
    public DateFormat mLastUpdateTime;
    public int locationInterval;
    public int status = 0;
    private int notificationId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        final String data = intent.getStringExtra(DB_DATA);

        // set location interval
        // TODO: get this from settings?
        locationInterval = 120;
        final int timeDelta = locationInterval;

        // parse JSON
        if(data != null && data.length() > 0) {
            try {
                JSONObject obj = new JSONObject(data);

                // set toolbar title
                String title = obj.getString("title");
                getSupportActionBar().setTitle(title);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        // new service intent
        final Intent i = new Intent(this, MonitorService.class);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status == 0) {
                    startMonitoring(i, data, timeDelta);
                    status = 1;
                    showNotification();
                    Snackbar.make(view, "Monitor activity started!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    stopService(i);
                    cancelNotification(getApplicationContext(), notificationId);
                    status = 0;
                    Snackbar.make(view, "Monitor activity stopped!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // last known location == current location
        // Create an instance of GoogleAPIClient.

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }


    protected void onStart() {
        mGoogleApiClient.connect();
        createLocationRequest();
        super.onStart();
    }


    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Register for the particular broadcast based on ACTION string
        IntentFilter ifilter = new IntentFilter(MonitorService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(monitorReceiver, ifilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(this).unregisterReceiver(monitorReceiver);
    }


    // Define the callback for what to do when data is received
    private BroadcastReceiver monitorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra("status");
            Toast.makeText(MonitoringActivity.this, status, Toast.LENGTH_SHORT).show();

        }
    };


    // start backgroud service and send it json
    public void startMonitoring(Intent i, String data, int timeDelta) {
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


    @Override
    public void onConnected(Bundle connectionHint) {
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.v(TAG, "Getting first location!");
            Log.v(TAG, "Latitude: " + String.valueOf(mLastLocation.getLatitude()));
            Log.v(TAG, "Longitude: " + String.valueOf(mLastLocation.getLongitude()));
        }

        // get continous location updates
        // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "Connection failed");
    }


    // continous location updates
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);    // TODO: set from 'location interval'
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // add location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        // check if user's location settings are enabled
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        // prompt the user to change location settings
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.

                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            // TODO: handle a case where user clicks 'cancel':
                            // https://stackoverflow.com/questions/29801368/how-to-show-enable-location-dialog-like-google-maps/29872703#29872703
                            status.startResolutionForResult(
                                    MonitoringActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        break;
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        Log.v(TAG, "Getting new location!" + String.valueOf(location));
        //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }



    public void showNotification() {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.cast_ic_notification_small_icon)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!")
                        .setOngoing(true);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MonitoringActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MonitoringActivity.class);
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
}


