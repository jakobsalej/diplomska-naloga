package com.example.jakob.qrreader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
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
import java.util.Date;
import java.util.TimeZone;

import database.DatabaseHandler;
import database.OrderDocumentJSON;

import static com.example.jakob.qrreader.ReadQRActivity.DB_DATA;

public class OrderItemActivity extends AppCompatActivity implements OnMapReadyCallback, CommonItemFragment.OnFragmentInteractionListener {

    String BASE_URL = "https://diploma-server-rest.herokuapp.com/api/documents/";

    private GoogleMap mMap;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ProgressDialog pd;
    private TextView senderTextView, senderDetailTextView, receiverTextView, receiverDetailTextView,
    statusTextView, dateTextView, vehicleTextView, textTextView;
    private String data;
    private int startIndex;
    private OrderDocumentJSON od;


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

        // get toolbar and set title
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("Some document");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        data = intent.getStringExtra(DB_DATA);
        Log.v("ITEM", data);
        Boolean detailsView = intent.getBooleanExtra("item_details", false);
        startIndex = intent.getIntExtra("startIndex", 0);

        // if it's details view, hide 'Add' button and don't get new data from API
        if (detailsView) {
            String odJSON = intent.getStringExtra("item");
            od = (new Gson().fromJson(odJSON, OrderDocumentJSON.class));
            Log.v("DISPLAYDATA", "object " + od.toString());

            setViewData(odJSON);

            //addButton.setVisibility(View.GONE);
            //documentName.setText(data);
        } else {
            // check if we have Order document with this ID already
            OrderDocumentJSON od = DatabaseHandler.getOrder(Integer.parseInt(data));
            if (od != null) {
                // we do have that doc already!
                // get the same json as if we got it from server
                data = od.getData();
                //addButton.setVisibility(View.GONE);
                //documentName.setText(data);
            } else {
                // get data from API if it's not a detail view
                new getDataFromDB().execute(BASE_URL + data);
            }
        }
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
            JSONObject receiver = obj.getJSONObject("customer");
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
            String date = obj.getString("dateDeadline");
            // TODO: parse date
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            parser.setTimeZone(TimeZone.getTimeZone("GMT"));
            String formattedDate = null;
            try {
                Date parsedDate = parser.parse(date);
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy (HH:mm)");
                formattedDate = formatter.format(parsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dateTextView.setText("Created on " + formattedDate);

            // text
            String text = obj.getString("text");
            textTextView.setText(text);

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
            if (pd.isShowing()){
                pd.dismiss();
            }
            //senderTextView.setText(result);
            data = result;
            setViewData(data);

            //saveToDB(data);

            // TODO: parse JSON and display data
        }
    }

    private void saveToDB(String data) {
        // add obtained data from server to local db

        // get start index (measurements length at the moment of saving to db)
        int startIndex = MonitorService.getMeasurementsLength();
        int endIndex = 0;
        Log.v("DISPLAYDATA", "Start index is " + startIndex);

        // parse JSON into object
        Log.v("DISPLAYDATA", data);
        JSONObject obj = null;
        try {
            obj = new JSONObject(data);
            int id = obj.getInt("documentID");
            String title = obj.getString("title");
            int status = obj.getInt("status");
            int delivered = obj.getInt("successfullyDelivered");
            String customer = obj.getJSONObject("customer").getString("name");
            String startLocation = obj.getJSONObject("startLocation").getString("x") + ","
                    + obj.getJSONObject("startLocation").getString("y");
            String endLocation = obj.getJSONObject("endLocation").getString("x") + ","
                    + obj.getJSONObject("endLocation").getString("y");
            double minTemp = 0;         // TODO: get it from DB
            double maxTemp = 10;        // TODO: get it from DB
            String measurements = String.valueOf(startIndex);  // save index.. TODO: separate field?
            OrderDocumentJSON  odj = new OrderDocumentJSON(
                    id,
                    title,
                    data,
                    status,
                    delivered,
                    customer,
                    startLocation,
                    endLocation,
                    minTemp,
                    maxTemp,
                    measurements,
                    startIndex,
                    endIndex
            );
            Log.v("DISPLAYDATA", odj.toString());

            // add to DB
            DatabaseHandler.addOrder(odj);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // set toolbar title
        // TODO: set correct title also onResume()

        //Gson gson = new Gson();
        //OrderDocumentJSON od = gson.fromJson(data, OrderDocumentJSON.class);
        // TODO: instead of creating object, try saving raw JSON directly as a blob to db? or as string
        // https://stackoverflow.com/questions/16603621/how-to-store-json-object-in-sqlite-database
    }
}
