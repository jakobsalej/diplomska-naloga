package com.example.jakob.qrreader;

/**
 * Created by jakob on 7/16/17.
 */

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import database.DatabaseHandler;
import database.OrderDocument;
import database.OrderDocumentJSON;

import static android.R.attr.logo;
import static android.R.attr.order;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.M;
import static database.DatabaseHandler.getOrders;


public class MainTabFragment extends Fragment implements View.OnClickListener{

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public static SQLiteDatabase db;
    private ArrayList<OrderDocumentJSON> dbData;
    public static ArrayList<JSONObject> temperaturesData;
    private int position;
    private boolean updateDB = true;
    private View view;
    private CardView mCardMonitoringStatus;
    private RelativeLayout help;
    private TextView cardText;
    private Button btnMonitoring;

    public MainTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main_tab, container, false);
        mCardMonitoringStatus = (CardView) view.findViewById(R.id.monitoring_status_card);
        help = (RelativeLayout) view.findViewById(R.id.help_layout);
        cardText = (TextView) view.findViewById(R.id.textView_status_running);
        btnMonitoring = (Button) view.findViewById(R.id.button_start_monitor);

        // scan vehicle button
        Button b = (Button) view.findViewById(R.id.button_scan_vehicle);
        b.setOnClickListener(this);

        // prepare DB
        DatabaseHandler mDbHelper = new DatabaseHandler(getActivity());
        db = mDbHelper.getWritableDatabase();

        // get DB data
        final Bundle args = getArguments();
        position = args.getInt("position");
        dbData = getDataFromDB(position);

        // if monitor service doesn't have alerts for every active order
        // (for example, that happens if we add orders and then kill app)
        // delete all alerts and add them again from active orders
        if (position == 0 && dbData.size() != MonitorService.alerts.length()){
            getTempLimits(dbData);
        }

        // dont get new data on onResume the first time (we already got data from onCreate)
        updateDB = false;

        // recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.ongoing_recycler_view);



        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new RecyclerAdapter(dbData);
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }


    private void getTempLimits(ArrayList<OrderDocumentJSON> dbData) {

        // empty the alerts first
        // TODO: bad performance?
        MonitorService.alerts = new JSONArray();

        Log.v("DB", dbData.toString());
        for (OrderDocumentJSON order : dbData) {
            JSONObject obj = new JSONObject();
            try {
                JSONArray alertsArray = new JSONArray();
                obj.put("id", order.getId());
                obj.put("title", order.getTitle());
                obj.put("minTemp", order.getMinTemp());
                obj.put("maxTemp", order.getMaxTemp());
                obj.put("lastValueOK", true);
                obj.put("alerts", alertsArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // add to alerts
            MonitorService.alerts.put(obj);
            Log.v("ALERTS", "alert added!: " + MonitorService.alerts.length());
        }
    }


    @Override
    public void onResume() {
        super.onResume();


        // get new data from db
        if (updateDB) {
            dbData.clear();
            dbData.addAll(getDataFromDB(position));
            mAdapter.notifyDataSetChanged();
        }

        // enable getting new data from DB
        updateDB = true;


        // monitoring status card show only on ongoing tab
        if (position > 0 || dbData.size() == 0) {
            mCardMonitoringStatus.setVisibility(View.GONE);
        } else if (dbData.size() > 0 && MonitorService.serviceRunning) {
            TextView cardText = (TextView) view.findViewById(R.id.textView_status_running);
            cardText.setText("MONITORING IN PROGRESS");
            Button btnMonitoring = (Button) view.findViewById(R.id.button_start_monitor);
            btnMonitoring.setText("Open");
        } else if (dbData.size() > 0 && !MonitorService.serviceRunning) {
            cardText.setText("MONITORING IS NOT RUNNING");
            btnMonitoring.setText("Start");
        }

        // show help only on first tab if there are no orders
        if (position > 0 || dbData.size() > 0) {
            help.setVisibility(View.GONE);
        }
    }


    private static ArrayList<OrderDocumentJSON> getDataFromDB(int sectionNumber) {
        // get data from DB, based on status: 0 = not started, 1 = running, 2 = done, 3 = cleared
        if (sectionNumber == 0) {
            ArrayList<OrderDocumentJSON> odArray0 = getOrders(0);
            ArrayList<OrderDocumentJSON> odArray1 = getOrders(1);
            odArray0.addAll(odArray1);
            return odArray0;
        } else if(sectionNumber == 1) {
            return getOrders(2);
        } else {
            return getOrders(3);
        }
    }


    private void scanVehicleQR() {
        Intent intent = new Intent(this.getActivity(), ReadQRActivity.class);
        intent.putExtra("title", "Add vehicle info");
        intent.putExtra("text", "To add vehicle info, please scan its QR code or manually enter vehicle's registration number.");
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_scan_vehicle:
                scanVehicleQR();
                break;
        }
    }

}
