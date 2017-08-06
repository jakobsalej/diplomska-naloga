package com.example.jakob.qrreader;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import database.OrderDocumentJSON;


/**
 * Created by jakob on 23.7.2017.
 */

public class RecyclerAdapterCommon extends RecyclerView.Adapter<RecyclerAdapterCommon.ItemHolder> {

    private JSONArray mItems;

    public static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mItemTitle;
        private TextView mItemText;
        private JSONObject mItem;

        public ItemHolder(View v) {
            super(v);

            mItemTitle = (TextView) v.findViewById(R.id.rec_item_title);
            mItemText = (TextView) v.findViewById(R.id.rec_item_subtitle);
            v.setOnClickListener(this);
        }

        public void bindItem(JSONObject od) {
            mItem = od;
            try {
                mItemTitle.setText(od.getString("name"));
                mItemText.setText(od.getString("company"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            Log.v("Rec view", "CLICK");

            // TODO: start details intent when user clicks on one of the items
        }
    }

    public RecyclerAdapterCommon(JSONArray items) {
        mItems = items;
    }

    @Override
    public RecyclerAdapterCommon.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_common_item, parent, false);
        return new ItemHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapterCommon.ItemHolder holder, int position) {
        JSONObject odItem = null;
        try {
            odItem = (JSONObject) mItems.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.bindItem(odItem);
    }

    @Override
    public int getItemCount() {
        return mItems.length();
    }
}
