package com.example.jakob.qrreader;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jakob on 8/7/17.
 */

public class DateAxisValueFormatter implements IAxisValueFormatter
{
    private String[] mValues;
    private long baseTime;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    public DateAxisValueFormatter(String[] values, long baseTime) {
        this.mValues = values;
        this.baseTime = baseTime;
    }

    //@override
    public String getFormattedValue(float value, AxisBase axis) {
        return sdf.format(new Date((long)(value + baseTime))); }
}
