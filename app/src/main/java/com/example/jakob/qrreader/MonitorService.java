package com.example.jakob.qrreader;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import database.DatabaseHandler;
import database.OrderDocument;
import database.OrderDocumentJSON;

import static android.R.attr.data;
import static android.R.attr.logo;
import static android.R.attr.order;
import static android.R.attr.start;
import static android.media.CamcorderProfile.get;
import static database.DatabaseHandler.getOrders;


/**
 * Created by jakob on 6/5/17.
 */


public class MonitorService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String ACTION = "com.example.jakob.qrreader.MonitorService";
    private static final String TAG = "MonitorService";
    private static final int REQUEST_CHECK_SETTINGS = 1000;
    private static final int mNotificationId = 27;
    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    public LocationRequest mLocationRequest;
    public Date mLastUpdateTime;
    public String mLastWeatherUpdate;
    public int locationInterval;
    private RequestQueue reQueue;
    private String weatherAppID = "126cb0f7fc8884208c5178d70cac7bea";
    private static JSONArray dataJSON = new JSONArray();
    private static JSONArray alerts = new JSONArray();
    public static boolean serviceRunning = false;
    public static Date lastTime;
    public static double lastTemp;
    public static double lastHumidity;
    public static double lastPressure;
    public static double lastLat;
    public static double lastLon;


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

        }

        String data = workIntent.getStringExtra("data");
        int timeDelta = workIntent.getIntExtra("timeDelta", 300);

        Log.v(TAG, "Starting monitor service with data " + data + timeDelta);
        String status = "Running";

        /* WTF IS THIS
        JSONObject obj;

        // parse JSON
        try {
            obj = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */



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
        mLocationRequest.setInterval(30000);    // TODO: set from 'location interval'
        mLocationRequest.setFastestInterval(30000);
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
        mLastUpdateTime = new Date();
        Log.v(TAG, "Getting new location!" + String.valueOf(location) + " " + mLastUpdateTime);

        // get coordinates
        double lat = Double.parseDouble(String.valueOf(location.getLatitude()));
        double lng = Double.parseDouble(String.valueOf(location.getLongitude()));

        // getting weather data
        getWeatherData(lat, lng, mLastUpdateTime);
    }



    public void getWeatherData(final double lat, final double lng, final Date updateTime) {

        // Instantiate the RequestQueue.
        reQueue = Volley.newRequestQueue(this);

        // build url
        String url ="http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lng + "&APPID=" + weatherAppID + "&units=metric";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, "Weather response is: "+ response);
                        mLastWeatherUpdate = response;

                        // get relevant data from weather report
                        JSONObject weatherObject;
                        try {
                            weatherObject = new JSONObject(response);
                            lastTemp = weatherObject.getJSONObject("main").getDouble("temp");
                            lastHumidity = weatherObject.getJSONObject("main").getDouble("humidity");
                            lastPressure = weatherObject.getJSONObject("main").getDouble("pressure");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // once we get weather data, add it to JSON
                        addDataToJSON(lastTemp, lastHumidity, lastPressure, lat, lng, updateTime);
                        updateLastValues(lastTemp, lastHumidity, lastPressure, lat, lng, updateTime);
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


    private void updateLastValues(double lastTemp, double lastHumidity, double lastPressure, double lat, double lng, Date updateTime) {
        // sending result back to activity
        Intent i = new Intent(ACTION);
        i.putExtra("status", serviceRunning);
        i.putExtra("lastTime", updateTime);
        i.putExtra("lastLat", lat);
        i.putExtra("lastLon", lng);
        i.putExtra("lastTemp", lastTemp);
        i.putExtra("lastHumidity", lastHumidity);
        i.putExtra("lastPressure", lastPressure);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }


    private void addDataToJSON(double lastTemp, double lastHumidity, double lastPressure, double lat, double lng, Date updateTime) {
        // new JSON object
        JSONObject dataObj = new JSONObject();
        long currentTime = updateTime.getTime();
        JSONObject locObj = new JSONObject();
        try {
            // new location object
            locObj.put("x", lat);
            locObj.put("y", lng);
            dataObj.put("location", locObj);

            // new weather object
            JSONObject weatherObj = new JSONObject();
            weatherObj.put("temperature", lastTemp);
            weatherObj.put("humidity", lastHumidity);
            weatherObj.put("pressure", lastPressure);
            dataObj.put("weather", weatherObj);

            // time
            dataObj.put("time", currentTime);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Adding JSON object to array: " + String.valueOf(dataObj));

        // add new data to array
        dataJSON.put(dataObj);
        Log.v(TAG, "Array length: " +  dataJSON.length());

        // check temperatures for alerts for alerts
        validateTemperatures(lastTemp, currentTime, locObj);
    }


    private void validateTemperatures(double value, long currentTime, JSONObject location) {
        for (int i = 0; i < alerts.length(); i++) {
            try {
                boolean addAlert = true;
                String alertMsg = "";
                JSONObject alert = (JSONObject) alerts.get(i);
                if (value < alert.getDouble("minTemp")) {
                    alertMsg = "Temperature is too low (recommended: " + alert.getDouble("minTemp") + ", measured: " + value + ")";
                } else if (value > alert.getDouble("maxTemp")) {
                    alertMsg = "Temperature is too high (recommended: " + alert.getDouble("maxTemp") + ", measured: " + value + ")";
                } else {
                    // everything is great!
                    addAlert = false;
                }

                if (addAlert) {
                    // create new Alert object and add it to order's alert list
                    JSONObject newAlertMsg = new JSONObject();
                    newAlertMsg.put("message", alertMsg);
                    newAlertMsg.put("time", currentTime);
                    newAlertMsg.put("location", location);
                    newAlertMsg.put("measurementValue", value);
                    alert.getJSONArray("alerts").put(newAlertMsg);

                    // show notification
                    int icon = R.drawable.ic_priority_high_white_24px;
                    showNotification("Warning!", alertMsg, icon);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public static int getMeasurementsLength() {
        // length of measurememnts array at the moment of requesting
        return dataJSON.length();
    }

    public static JSONArray getMeasurements(int startIndex, int endIndex) {
        // return subArray of measurements based on startIndex / endIndex
        JSONArray subArray = new JSONArray();
        for (int i = startIndex; i < endIndex; i ++) {
            try {
                subArray.put(dataJSON.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return subArray;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Stopping service!");

        // save measurements
        saveMeasurements();

        mGoogleApiClient.disconnect();
        serviceRunning = false;
        // TODO: send Broadcast to view, so we know background activity stopped
    }

    private void saveMeasurements() {

        long endTime = new Date().getTime();
        int endIndex = getMeasurementsLength();

        // get all still active (='in progress') orderDocuments from local DB and add measurements' subarray
        int status = 1;
        int doneStatus = 2;
        ArrayList<OrderDocumentJSON> orders = DatabaseHandler.getOrders(status);
        Log.v("ORDERS", String.valueOf(orders.size()));
        for(OrderDocumentJSON ord : orders) {
            int startIndex = ord.getStartIndex();
            ord.setEndIndex(endIndex);
            JSONArray measurements = getMeasurements(startIndex, endIndex);

            // get startTime from the first measurement
            long startTime = 0;
            if (measurements.length() > 0) {
                try {
                    startTime = measurements.getJSONObject(0).getLong("time");      // TODO: use last one?
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // update status + also update values in JSON string 'data'
            ord.setStatus(doneStatus);
            ord.setNewJSONValue("status", String.valueOf(doneStatus), 1);

            // get alerts for this order
            JSONArray alertMsgs = getAlertMessages(ord.getId());

            // TODO: set this somehow (was it successfully delivered?)
            int delivered = 1;

            // create new object transport
            JSONObject tr = addNewTransportObject(ord.getId(), measurements, alertMsgs.toString(), startTime, endTime, delivered);
            ord.setMeasurements(tr.toString());
            //ord.setNewJSONValue("transport", tr.toString(), 0);

            Log.v("Measurements", ord.getMeasurements());

            // TODO: create new transport entry, get it's id and save it to 'transport' field in json

            int res = DatabaseHandler.updateOrder(ord);       // UPDATE
        }
    }

    private JSONArray getAlertMessages(Integer id) {
        for (int i = 0; i < alerts.length(); i++) {
            try {
                if (alerts.getJSONObject(i).getInt("id") == id) {
                    return (JSONArray) alerts.getJSONObject(i).get("alerts");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private JSONObject addNewTransportObject(Integer id, JSONArray measurements, String alerts, long startTime, long endTime, int delivered) {

        // calculate duration in hours
        long duration = (endTime - startTime) / (60 * 60 * 1000) % 24;


        JSONObject tr = new JSONObject();
        try {
            tr.put("idOrder", id);
            tr.put("measurements", measurements);
            tr.put("alerts", alerts);
            tr.put("startDate", startTime);
            tr.put("endDate", endTime);
            tr.put("delivered", delivered);
            tr.put("duration", duration);

            // TODO: get from settings
            tr.put("vehicleType", 0);
            tr.put("vehicleReg", "LJ-B672");
            tr.put("driverID", 1);
            tr.put("text", "Customer was nice and friendly!");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tr;
    }


    public static void addAlertsArray(JSONObject obj) {
        alerts.put(obj);
        Log.v("ALERTS", obj.toString());
    }


    public void showNotification(String title, String text, int icon) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MonitoringActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MonitoringActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

}

