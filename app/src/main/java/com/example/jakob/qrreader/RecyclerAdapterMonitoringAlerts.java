package com.example.jakob.qrreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.media.CamcorderProfile.get;
import static com.example.jakob.qrreader.R.color.colorRED;
import static com.example.jakob.qrreader.R.id.textView;


/**
 * Created by jakob on 23.7.2017.
 */

public class RecyclerAdapterMonitoringAlerts extends RecyclerView.Adapter<RecyclerAdapterMonitoringAlerts.ItemHolder> {

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
                mItemTitle.setText(od.getString("title"));

                // set status text
                if (od.getBoolean("lastValueOK")) {
                    mItemText.setText("EVERYTHING IS OK!");
                    mItemText.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_check_black_24px, 0);
                } else {
                    JSONArray alerts = od.getJSONArray("alerts");
                    JSONObject alertObj = (JSONObject) alerts.get(alerts.length()-1);
                    mItemText.setText(alertObj.getString("message").toUpperCase());
                    mItemText.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_error_outline_black_24px, 0);
                    mItemText.setTextColor(Color.parseColor("#d32f2f"));
                    for (Drawable drawable : mItemText.getCompoundDrawables()) {
                        if (drawable != null) {
                            drawable.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#d32f2f"), PorterDuff.Mode.SRC_IN));
                        }
                    }
                }

                // TODO: show more stuff
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            Log.v("Rec view", "CLICK");
            Context context = itemView.getContext();
            Intent showItemIntent = new Intent(context, OrderItemActivity.class);
            try {
                showItemIntent.putExtra("id", mItem.getString("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            context.startActivity(showItemIntent);


        }
    }

    public RecyclerAdapterMonitoringAlerts(JSONArray items) {
        mItems = items;
    }

    @Override
    public RecyclerAdapterMonitoringAlerts.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_alert_item, parent, false);
        return new ItemHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapterMonitoringAlerts.ItemHolder holder, int position) {
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


    public static String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("HH:mm, dd.MM.yyyy");
        return format.format(date);
    }
}
