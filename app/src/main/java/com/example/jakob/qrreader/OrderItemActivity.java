package com.example.jakob.qrreader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import database.DatabaseHandler;
import database.OrderDocumentJSON;

import static android.R.attr.order;


public class OrderItemActivity extends AppCompatActivity implements OnMapReadyCallback, CommonItemFragment.OnFragmentInteractionListener {

    String BASE_URL = "https://diploma-server-rest.herokuapp.com/api/orders/";

    private Button addBtn, transportDetailsBtn;
    private GoogleMap mMap;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ProgressDialog pd;
    private TextView senderTextView, senderDetailTextView, receiverTextView, receiverDetailTextView,
    statusTextView, dateTextView, vehicleTextView, textTextView, tempRangeTextView;
    private String data;
    private String measurementsData;
    private JSONObject loc1, loc2;
    private double minTemp, maxTemp;

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_item);

        // get textviews
        senderTextView = (TextView) findViewById(R.id.textView_sender);
        senderDetailTextView = (TextView) findViewById(R.id.textView_sender_details);
        receiverTextView = (TextView) findViewById(R.id.textView_receiver);
        receiverDetailTextView = (TextView) findViewById(R.id.textView_receiver_details);
        statusTextView = (TextView) findViewById(R.id.textView_status);
        dateTextView = (TextView) findViewById(R.id.textView_date);
        vehicleTextView = (TextView) findViewById(R.id.textView_vehicleType);
        textTextView = (TextView) findViewById(R.id.textView_text);
        tempRangeTextView = (TextView) findViewById(R.id.textView_temp_range);
        addBtn = (Button) findViewById(R.id.button_add_item);
        transportDetailsBtn = (Button) findViewById(R.id.button_transport_details);

        // get toolbar and set title
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("Document title");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        boolean isData = intent.getBooleanExtra("isData", false);

        // if we get data from intent (and not just ID)
        if (isData) {
            data = intent.getStringExtra("data");
            measurementsData = intent.getStringExtra("measurements");

            setViewData(data);
            setMeasuremntsViewData(measurementsData);

            // hide 'add' button - we already have it stored locally
            addBtn.setVisibility(View.GONE);

        } else {
            String id = intent.getStringExtra("id");

            // check if we already have order with that ID locally
            OrderDocumentJSON od = DatabaseHandler.getOrder(Integer.parseInt(id));
            if (od != null) {
                // we do have that doc already!
                Log.v("DB", "We have this ID already");

                // if service is running and we scanned an ID of order in progress, stop it
                if (MonitorService.serviceRunning && od.getStatus() == 1) {
                    MonitorService.saveMeasurements(od.getId());

                    // now that transport data is saved, get order again from DB (to get all that saved data)
                    od = DatabaseHandler.getOrder(Integer.parseInt(id));
                    Log.v("DB", "Saved transport data! " + od.getMeasurements());
                    measurementsData = od.getMeasurements();
                    setMeasuremntsViewData(null);
                }

                // if we do, set view
                setViewData(od.getData());
                setMeasuremntsViewData(od.getMeasurements());

                // hide 'add' button - we already have it stored locally
                addBtn.setVisibility(View.GONE);

            } else {
                // else get it from server
                new getDataFromDB().execute(BASE_URL + id);
            }
        }
    }

    private void setMeasuremntsViewData(String measurementsData) {
        minTemp = 5;
        maxTemp = 11;
    }


    private void setViewData(String odJSON) {
        try {
            JSONObject obj = new JSONObject(odJSON);
            String title = obj.getString("title");
            collapsingToolbarLayout.setTitle(title);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                collapsingToolbarLayout.setCollapsedTitleTextColor(getColor(android.R.color.white));
            }
            // sender
            JSONObject sender = obj.getJSONObject("sender");
            String senderName = sender.getString("name");
            String detailsSender = concatAddress(sender.getJSONObject("address"));
            senderTextView.setText(senderName);
            senderDetailTextView.setText(detailsSender);

            // receiver
            JSONObject receiver = obj.getJSONObject("receiver");
            String receiverName = receiver.getString("name");
            String detailsReceiver = concatAddress(receiver.getJSONObject("address"));
            receiverTextView.setText(receiverName);
            receiverDetailTextView.setText(detailsReceiver);

            // map points
            JSONObject startLoc = obj.getJSONObject("startLocation");
            JSONObject endLoc = obj.getJSONObject("endLocation");
            setMapMarkers(startLoc, endLoc);

            // status
            int status = obj.getInt("status");
            String statusMsg = "";
            switch (status) {
                case 0:
                    statusMsg = "Not started";
                    break;
                case 1:
                    statusMsg = "In progress";
                    break;
                case 2:
                    statusMsg = "Finished";
                    // show transportDetails button
                    transportDetailsBtn.setVisibility(View.VISIBLE);
                    break;
                default:
                    statusMsg = "Unknown";
            }
            statusTextView.setText(statusMsg);

            // vehicle type
            int vehicleType = obj.getInt("vehicleTypeRequired");
            String vehicleTypeStr = "Normal";
            if (vehicleType != 1) {
                vehicleTypeStr = "Cooler required";
            }
            vehicleTextView.setText(vehicleTypeStr);

            // date
            String date = obj.getString("date");
            // TODO: parse date
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            parser.setTimeZone(TimeZone.getTimeZone("GMT"));
            String formattedDate = null;
            try {
                Date parsedDate = parser.parse(date);
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, dd.MM.yyyy");
                formattedDate = formatter.format(parsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dateTextView.setText(formattedDate);

            // text
            String text = obj.getString("text");
            textTextView.setText(text);

            // temp range
            // TODO: get from DB
            double minT = 5;
            double maxT = 11;
            minTemp = minT;
            maxTemp = maxT;
            tempRangeTextView.setText(String.valueOf(minTemp) + " °C - " + String.valueOf(maxTemp) + " °C");

            // cargo
            //JSONObject cargoObj = obj.getJSONObject("cargo");
            //JSONArray cargoItems = cargoObj.getJSONArray("items");
            JSONArray cargoItems = obj.getJSONArray("cargo");

            // add cargo items fragment
            CommonItemFragment fragment = CommonItemFragment.newInstance("cargo", cargoItems.toString());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_holder, fragment).commit();


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void setMapMarkers(JSONObject startLoc, JSONObject endLoc) {
        if (mMap != null) {
            try {
                // add markers
                LatLng start = new LatLng(startLoc.getDouble("x"), startLoc.getDouble("y"));
                LatLng end = new LatLng(endLoc.getDouble("x"), endLoc.getDouble("y"));
                mMap.addMarker(new MarkerOptions().position(start).title("Start point"));
                mMap.addMarker(new MarkerOptions().position(end).title("End point"));

                // set zoom on markers
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(start);
                builder.include(end);
                LatLngBounds bounds = builder.build();

                // TODO: fix crashing when rotating
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.35); // offset from edges of the map 10% of screen

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

                mMap.animateCamera(cu);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Log.v("MAP", "not ready yet");
            loc1 = startLoc;
            loc2 = endLoc;
        }
    }


    private String concatAddress(JSONObject address) {
        // create an address string from all fields of objcet 'address'
        try {
            String street = address.getString("street");
            String city = address.getString("city");
            String cityCode = address.getString("cityCode");
            String country = address.getString("country");
            return street + ", " + cityCode + " " + city + ", " + country;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // set custom style
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.maps_style));

            if (!success) {
                Log.e("MAPS", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MAPS", "Can't find style. Error: ", e);
        }

        // Add a marker in Ljubljana as a starting point and move the camera
        LatLng ljubljana = new LatLng(46.0569, 14.5058);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ljubljana));

        // if loc1 / loc2 are not null, value was assigned to them as map wasn't ready yet
        if (loc1 != null && loc2 != null) {
            setMapMarkers(loc1, loc2);
        }
    }


    // TODO: optimize this (third party library? Volley, Retrofit)
    private class getDataFromDB extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(OrderItemActivity.this);
            pd.setMessage("Loading data...");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // show gathered data
            data = result;
            setViewData(data);
            if (pd.isShowing()){
                pd.dismiss();
            }
        }
    }

    public void saveToDB(String data) {
        // add obtained data from server to local db

        // get start index (measurements length at the moment of saving to db)
        int startIndex = MonitorService.getMeasurementsLength();
        int endIndex = 0;
        //Log.v("DISPLAYDATA", "Start index is " + startIndex);

        // parse JSON into object
        //Log.v("DISPLAYDATA", data);
        JSONObject obj = null;
        try {
            obj = new JSONObject(data);
            Log.v("OBJECT", obj.toString());
            int id = obj.getInt("orderID");
            String title = obj.getString("title");
            int status = obj.getInt("status");

            // change status to 'in progress' && update value in JSON
            int newStatus = 1;      // In progress
            if (status == 0) {
                status = newStatus;
                obj.put("status", newStatus);
                // TODO: send updated JSON to server
                data = obj.toString();
            }

            int vehicleTypeRequired = obj.getInt("vehicleTypeRequired");        // TODO: show warning if vehicle type is different than required
            String date = obj.getString("date");
            String startLocation = obj.getJSONObject("startLocation").getString("x") + ","
                    + obj.getJSONObject("startLocation").getString("y");
            String endLocation = obj.getJSONObject("endLocation").getString("x") + ","
                    + obj.getJSONObject("endLocation").getString("y");
            double minTemp = 5;         // TODO: get it from DB
            double maxTemp = 11;        // TODO: get it from DB
            int delivered = 0;          // when we add it, its not yet delivered
            String measurements = "measurements placeholder";
            OrderDocumentJSON  odj = new OrderDocumentJSON(
                    id,
                    title,
                    data,
                    status,
                    delivered,
                    date,
                    startLocation,
                    endLocation,
                    minTemp,
                    maxTemp,
                    measurements,
                    startIndex,
                    endIndex
            );
            odj.printValues();

            // add to DB
            DatabaseHandler.addOrder(odj);

            // add entry to alertsArray (where we save alerts specific for this order)
            JSONObject alertObj = new JSONObject();
            try {
                JSONArray alertsArray = new JSONArray();
                alertObj.put("id", odj.getId());
                alertObj.put("title", odj.getTitle());
                alertObj.put("minTemp", odj.getMinTemp());
                alertObj.put("maxTemp", odj.getMaxTemp());
                alertObj.put("lastValueOK", true);
                alertObj.put("alerts", alertsArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            MonitorService.addAlertsArray(alertObj);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void onAddClick(View v) {
        //Snackbar.make(v, "Saving Order Document!", Snackbar.LENGTH_LONG).show();
        saveToDB(data);     // TODO: this should not be on main thread?
        Intent intent = new Intent(this, Main2Activity.class);

        // clear back button stack
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    public void transportDetailsActivity(View v) {
        Intent intent = new Intent(this, TransportDetailsActivity.class);
        intent.putExtra("transport", measurementsData);
        intent.putExtra("minTemp", minTemp);
        intent.putExtra("maxTemp", maxTemp);
        startActivity(intent);
    }
}
