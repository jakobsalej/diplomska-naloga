package com.example.jakob.qrreader;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import database.OrderDocument;

/**
 * Created by jakob on 7/4/17.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemHolder> {

    private ArrayList<OrderDocument> mItems;

    public static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mItemTitle;
        private TextView mItemText;
        private OrderDocument mItem;

        public ItemHolder(View v) {
            super(v);

            mItemTitle = (TextView) v.findViewById(R.id.rec_item_title);
            mItemText = (TextView) v.findViewById(R.id.rec_item_id);
            v.setOnClickListener(this);
        }

        public void bindItem(OrderDocument od) {
            mItem = od;
            mItemTitle.setText(od.getTitle());
            mItemText.setText(od.getText());
        }

        @Override
        public void onClick(View v) {
            Log.v("Rec view", "CLICK");
        }
    }

    public RecyclerAdapter(ArrayList<OrderDocument> items) {
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
        OrderDocument odItem = mItems.get(position);
        holder.bindItem(odItem);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
