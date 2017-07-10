package com.example.jakob.qrreader;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import database.OrderDocument;
import database.OrderDocumentJSON;

import static com.example.jakob.qrreader.ReadQRActivity.DB_DATA;

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
            mItemText = (TextView) v.findViewById(R.id.rec_item_id);
            v.setOnClickListener(this);
        }

        public void bindItem(OrderDocumentJSON od) {
            mItem = od;
            mItemTitle.setText(od.getTitle());
            mItemText.setText(od.getId().toString());
        }

        @Override
        public void onClick(View v) {
            Log.v("Rec view", "CLICK");

            // TODO: start details intent when user clicks on one of the items

            Context context = itemView.getContext();
            Intent showItemIntent = new Intent(context, DisplayDataActivity.class);
            showItemIntent.putExtra(DB_DATA, mItem.getData());
            showItemIntent.putExtra("item_details", true);
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
