package com.example.jakob.qrreader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import java.util.Arrays;

import database.DatabaseHandler;
import database.Order;
import database.OrderDocument;
import database.OrderDocumentJSON;

import static android.R.attr.order;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.example.jakob.qrreader.MonitorService.getMeasurementsLength;
import static com.example.jakob.qrreader.ReadQRActivity.DB_DATA;
import static database.DatabaseHandler.getOrder;

public class DisplayDataActivity extends AppCompatActivity {

    ProgressDialog pd;
    TextView documentName, textViewMeasurements;
    String BASE_URL = "https://diploma-server-rest.herokuapp.com/api/documents/";
    String data;
    OrderDocumentJSON od;
    private int startIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        documentName = (TextView) findViewById(R.id.textView_item_raw);
        textViewMeasurements = (TextView) findViewById(R.id.textView_measurements);
        Button addButton = (Button) findViewById(R.id.button_document_add);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        data = intent.getStringExtra(DB_DATA);
        Boolean detailsView = intent.getBooleanExtra("item_details", false);
        startIndex = intent.getIntExtra("startIndex", 0);

        // if it's details view, hide 'Add' button and don't get new data from API
        if (detailsView) {
            String odJSON = intent.getStringExtra("item");
            od = (new Gson().fromJson(odJSON, OrderDocumentJSON.class));
            Log.v("DISPLAYDATA", "object " + od.toString());

            addButton.setVisibility(View.GONE);
            documentName.setText(data);
        } else {
            // check if we have Order document with this ID already
            OrderDocumentJSON od = DatabaseHandler.getOrder(Integer.parseInt(data));
            if (od != null) {
                // we do have that doc already!
                // get the same json as if we got it from server
                data = od.getData();
                addButton.setVisibility(View.GONE);
                documentName.setText(data);
            } else {
                // get data from API if it's not a detail view
                new GetDataFromDB().execute(BASE_URL + data);
            }
        }
    }


    public void getDataFromDB(String id) {

    }




    // TODO: optimize this (third party library? Volley, Retrofit)
    private class GetDataFromDB extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(DisplayDataActivity.this);
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
            documentName.setText(result);
            data = result;

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


    public void startMonitoring(View view) {
        Intent intent = new Intent(this, MonitoringActivity.class);
        intent.putExtra(DB_DATA, data);
        startActivity(intent);
    }


    public void stopMonitoring(View view) {
        // stop the monitoring and get measurements
        int endIndex = MonitorService.getMeasurementsLength();
        String measurements = "";

        // check if background service is even running
        if (MonitorService.serviceRunning) {
            JSONArray measurementsJSON = MonitorService.getMeasurements(startIndex, endIndex);
            measurements = measurementsJSON.toString();
            textViewMeasurements.setText(measurements);
        }

        // append measurements to JSON for server update
        // TODO: add moar fields to 'transport' (dispozicija...)
        String newData = null;
        String newTransport = null;
        try {
            // create new 'transport' object and add measurements to it
            JSONObject transport = new JSONObject();
            transport.put("measurementsList", measurements);
            newTransport = transport.toString();

            // add newly created object as a 'transport' field to existing OrderDocument object
            JSONObject obj = new JSONObject(data);
            obj.put("transport", transport);
            newData = obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // update local db (object)
        if (od != null) {
            od.setMeasurements(newTransport);
            od.setData(newData);
            Log.v("DISPLAYDATA", "Updating order! " + od.toString());
        }

        // update DB (local + server)
        DatabaseHandler.updateOrder(od);
    }


    public void addToQueue(View view) {
        saveToDB(data);     // TODO: this should not be on main thread?
        Intent intent = new Intent(this, OrdersActivity.class);

        // clear back button stack
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}





