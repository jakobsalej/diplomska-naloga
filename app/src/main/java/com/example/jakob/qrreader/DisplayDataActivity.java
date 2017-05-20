package com.example.jakob.qrreader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DisplayDataActivity extends AppCompatActivity {

    ProgressDialog pd;
    TextView documentName;
    String BASE_URL = "https://diploma-server-rest.herokuapp.com/api/documents/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String data = intent.getStringExtra(ReadQRActivity.DB_DATA);

        // Capture the layout's TextView and set the string as its text
        documentName = (TextView) findViewById(R.id.textview_document_name);
        //documentName.setText("Getting data...");

        new GetDataFromDB().execute(BASE_URL + data);
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
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

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

            // TODO: parse JSON and display data
        }
    }
}





