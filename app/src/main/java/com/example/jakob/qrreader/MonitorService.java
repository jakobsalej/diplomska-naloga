package com.example.jakob.qrreader;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by jakob on 6/5/17.
 */


public class MonitorService extends IntentService {

    public MonitorService() {
        super("MonitorService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

    }
}
