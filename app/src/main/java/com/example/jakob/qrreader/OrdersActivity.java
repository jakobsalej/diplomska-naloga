package com.example.jakob.qrreader;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import database.DatabaseHandler;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public static SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        DatabaseHandler mDbHelper = new DatabaseHandler(this);
        db = mDbHelper.getWritableDatabase();

        mRecyclerView = (RecyclerView) findViewById(R.id.ongoing_recycler_view);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new RecyclerAdapter(DatabaseHandler.getOrders());
        mRecyclerView.setAdapter(mAdapter);

    }

    public void scanCode(View view) {
        Intent intent = new Intent(this, ReadQRActivity.class);
        startActivity(intent);
    }
}
