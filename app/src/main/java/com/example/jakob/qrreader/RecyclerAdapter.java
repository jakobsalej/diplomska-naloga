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

import java.util.ArrayList;

import database.OrderDocumentJSON;


/**
 * Created by jakob on 7/4/17.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemHolder> {

    private ArrayList<OrderDocumentJSON> mItems;

    public static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mItemTitle;
        private TextView mItemText;
        private OrderDocumentJSON mItem;
        private static final String ITEM_KEY = "ITEM";

        public ItemHolder(View v) {
            super(v);

            mItemTitle = (TextView) v.findViewById(R.id.rec_item_title);
            mItemText = (TextView) v.findViewById(R.id.rec_item_subtitle);
            v.setOnClickListener(this);
        }

        public void bindItem(OrderDocumentJSON od) {
            mItem = od;
            mItemTitle.setText(od.getTitle());
            mItemText.setText(od.getDate());
        }

        @Override
        public void onClick(View v) {

            // convert object to string to pass it to another activity
            Context context = itemView.getContext();
            Intent showItemIntent = new Intent(context, OrderItemActivity.class);
            showItemIntent.putExtra("isData", true);
            showItemIntent.putExtra("data", mItem.getData());
            showItemIntent.putExtra("measurements", mItem.getMeasurements());
            context.startActivity(showItemIntent);

        }
    }

    public RecyclerAdapter(ArrayList<OrderDocumentJSON> items) {
        mItems = items;
    }

    @Override
    public RecyclerAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item, parent, false);
        return new ItemHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.ItemHolder holder, int position) {
        OrderDocumentJSON odItem = mItems.get(position);
        holder.bindItem(odItem);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
