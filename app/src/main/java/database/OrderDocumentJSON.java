package database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    private final static long serialVersionUID = -2917451307888388000L;

    /**
     * No args constructor for use in serialization
     *
     */
    public OrderDocumentJSON() {
    }

    public OrderDocumentJSON(Integer id, String title, String data, Integer status, Integer delivered) {
        super();
        this.id = id;
        this.title = title;
        this.data = data;
        this.status = status;
        this.delivered = delivered;
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

    @Override
    public String toString() {
        return "OrderDocumentJSON [id = "+id+", title = "+title+", delivered = "+delivered+", status = "+status+", data = "+data+"]";
    }

}
