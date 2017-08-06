package database;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static android.R.attr.name;

/**
 * Created by jakob on 7/10/17.
 */

public class OrderDocumentJSON implements Serializable
{

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("data")
    @Expose
    private String data;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("delivered")
    @Expose
    private Integer delivered;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("startLocation")
    @Expose
    private String startLocation;
    @SerializedName("endLocation")
    @Expose
    private String endLocation;
    @SerializedName("minTemp")
    @Expose
    private Double minTemp;
    @SerializedName("maxTemp")
    @Expose
    private Double maxTemp;
    @SerializedName("measurements")
    @Expose
    private String measurements;
    @SerializedName("startIndex")
    @Expose
    private Integer startIndex;
    @SerializedName("endIndex")
    @Expose
    private Integer endIndex;
    private final static long serialVersionUID = -2917451307888388000L;

    /**
     * No args constructor for use in serialization
     *
     */
    public OrderDocumentJSON() {
    }

    public OrderDocumentJSON(Integer id, String title, String data, Integer status, Integer delivered, String date, String startLocation, String endLocation, Double minTemp, Double maxTemp, String measurements, Integer startIndex, Integer endIndex) {
        super();
        this.id = id;
        this.title = title;
        this.data = data;
        this.status = status;
        this.delivered = delivered;
        this.date = date;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.measurements = measurements;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDelivered() {
        return delivered;
    }

    public void setDelivered(Integer delivered) {
        this.delivered = delivered;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public Double getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(Double minTemp) {
        this.minTemp = minTemp;
    }

    public Double getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(Double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getMeasurements() {
        return measurements;
    }

    public void setMeasurements(String measurements) {
        this.measurements = measurements;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public Integer getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
    }

    @Override
    public String toString() {
        return "OrderDocumentJSON [id = "+id+", title = "+title+", delivered = "+delivered+", status = "+status+", data = "+data+", maxTemp = "+maxTemp+", measurements = "+measurements+", endLocation = "+endLocation+", startLocation = "+startLocation+", minTemp = "+minTemp+", date = "+date+", startIndex = "+startIndex+", endIndex = "+endIndex+"]";
    }

    public void printValues() {
        Log.v("ID: ", this.getId().toString());
        Log.v("TITLE: ", this.getTitle());
        Log.v("DATA: ", this.getData());
        Log.v("STATUS: ", this.getStatus().toString());
        Log.v("DATE: ", this.getDate());
        Log.v("START LOC: ", this.getStartLocation());
        Log.v("END LOC: ", this.getEndLocation());
        Log.v("MIN TEMP: ", this.getMinTemp().toString());
        Log.v("MAX TEMP: ", this.getMaxTemp().toString());
        Log.v("MEASUREMENTS: ", this.getMeasurements());
        Log.v("START INDX: ", this.getStartIndex().toString());
        Log.v("END INDX: ", this.getEndIndex().toString());
        Log.v("DELIVERED: ", this.getDelivered().toString());
    }

    public void setNewJSONValue(String field, String value, int type) {

        // update field in JSON
        try {
            JSONObject obj = new JSONObject(this.data);

            // type: 0 = String, 1 = int
            if (type == 0) {
                obj.put(field, value);
            } else if (type == 1) {
                int val = Integer.parseInt(value);
                obj.put(field, val);
            }

            this.data = obj.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
