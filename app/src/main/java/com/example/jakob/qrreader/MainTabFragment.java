package com.example.jakob.qrreader;

/**
 * Created by jakob on 7/16/17.
 */

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

import static android.R.attr.order;
import static android.os.Build.VERSION_CODES.M;
import static database.DatabaseHandler.getOrders;


public class MainTabFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public static SQLiteDatabase db;
    private ArrayList<OrderDocumentJSON> dbData;
    public static ArrayList<JSONObject> temperaturesData;
    private int position;
    private boolean updateDB = true;

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
        final View view = inflater.inflate(R.layout.fragment_main_tab, container, false);

        // prepare DB
        DatabaseHandler mDbHelper = new DatabaseHandler(getActivity());
        db = mDbHelper.getWritableDatabase();

        // get DB data
        final Bundle args = getArguments();
        position = args.getInt("position");
        dbData = getDataFromDB(position);

        // get temperature limits
        // if there are orders already in progress when we start the app and monitor service is not yet running,
        // add temp data (all orders added while monitor service is running will be added when created!)
        if (!MonitorService.serviceRunning && position == 0) {
            getTempLimits(dbData);
        }

        // dont get new data on onResume the first time (we already got data from onCreate)
        updateDB = false;

        // recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.ongoing_recycler_view);

        // monitoring status card show only on ongoing tab
        CardView mCardMonitoringStatus = (CardView) view.findViewById(R.id.monitoring_status_card);
        if (position > 0 || dbData.size() == 0) {
            mCardMonitoringStatus.setVisibility(View.GONE);
        }

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



            if (!MonitorService.serviceRunning && position == 1) {
                // if monitor service is not running yet, also update tempLimits
                getTempLimits(dbData);
            }

        }

        // enable getting new data from DB
        updateDB = true;
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

}
