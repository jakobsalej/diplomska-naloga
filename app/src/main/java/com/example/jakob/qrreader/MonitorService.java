package com.example.jakob.qrreader;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

import static android.R.attr.data;


/**
 * Created by jakob on 6/5/17.
 */


public class MonitorService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String ACTION = "com.example.jakob.qrreader.MonitorService";
    private static final String TAG = "MonitorService";
    private static final int REQUEST_CHECK_SETTINGS = 1000;
    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    public LocationRequest mLocationRequest;
    public String mLastUpdateTime;
    public int locationInterval;
    private RequestQueue reQueue;
    private String weatherAppID = "126cb0f7fc8884208c5178d70cac7bea";
    private JSONArray dataJSON = new JSONArray();
    public static boolean serviceRunning = false;
    public static String lastTime;
    public static String temp;
    public static String humidity;
    public static int lat;
    public static int lon;


    public MonitorService() {
        super("MonitorService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        serviceRunning = true;

        // Gets data from the incoming Intent
        Log.v(TAG, "Incoming intent!");


        // get MODE
        int mode = workIntent.getIntExtra("mode", 0);

        if (mode == 0) {
            // start service
            connectToGoogleApiClient();
        } else if (mode == 1) {
            // return available data immediately
            Log.v(TAG, "Will return data!");
            // sending result back to activity
            Intent i = new Intent(ACTION);
            i.putExtra("status", serviceRunning);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }


        String data = workIntent.getStringExtra("data");
        int timeDelta = workIntent.getIntExtra("timeDelta", 120);

        Log.v(TAG, "Starting monitor service with data " + data + timeDelta);
        String status = "Running";

        JSONObject obj;

        // parse JSON
        try {
            obj = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }




        Log.v(TAG, "Service will enter infinite loop now.");

        while(true) {
        }


    }


    // LOCATION

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "On create");
    }


    private void connectToGoogleApiClient() {

        serviceRunning = true;

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
        createLocationRequest();
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
            Log.v(TAG, "Latitude: " + String.valueOf(mLastLocation.getLatitude()));
            Log.v(TAG, "Longitude: " + String.valueOf(mLastLocation.getLongitude()));
        }

        // get continous location updates
        Log.v(TAG, "Will start with GPS updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
        mLocationRequest.setInterval(20000);    // TODO: set from 'location interval'
        mLocationRequest.setFastestInterval(10000);
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
                        // TODO: user doesn't have 'high accuracy' enabled, do something about it.. show dialog? its been already shown
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
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Log.v(TAG, "Getting new location!" + String.valueOf(location) + " " + mLastUpdateTime);

        // getting weather data
        getWeatherData(mLastLocation, mLastUpdateTime);
    }



    public void getWeatherData(final Location location, final String updateTime) {

        // get coordinates
        double lat = Double.parseDouble(String.valueOf(location.getLatitude()));
        double lng = Double.parseDouble(String.valueOf(location.getLongitude()));

        // Instantiate the RequestQueue.
        reQueue = Volley.newRequestQueue(this);

        // build url
        String url ="http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lng + "&APPID=" + weatherAppID;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, "Weather response is: "+ response);

                        // once we get weather data, add it to JSON
                        addDataToJSON(response, location, updateTime);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "That didn't work!");
                // TODO: if no weather data is available, save entry without it
            }
        });

        // Add the request to the RequestQueue.
        reQueue.add(stringRequest);
    }

    private void addDataToJSON(String weatherData, Location locationData, String updateTime) {
        // new JSON object
        JSONObject dataObj = new JSONObject();
        try {
            dataObj.put("time", mLastUpdateTime);
            dataObj.put("location", mLastLocation);
            dataObj.put("weather", weatherData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Adding JSON object to array: " + String.valueOf(dataObj));

        // add new data to array
        dataJSON.put(dataObj);
        //Log.v(TAG, String.valueOf(dataJSON));
        Log.v(TAG, "Array length: " +  dataJSON.length());
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Stopping service!");
        mGoogleApiClient.disconnect();
        serviceRunning = false;
    }

}

