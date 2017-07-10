package database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by jakob on 7/10/17.
 */

public class Location implements Serializable
{

    @SerializedName("x")
    @Expose
    private Double x;
    @SerializedName("y")
    @Expose
    private Double y;
    @SerializedName("_id")
    @Expose
    private String id;
    private final static long serialVersionUID = -7769293627669383237L;

    /**
     * No args constructor for use in serialization
     *
     */
    public Location() {
    }

    /**
     *
     * @param id
     * @param y
     * @param x
     */
    public Location(Double x, Double y, String id) {
        super();
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ClassPojo [_id = "+id+", y = "+y+", x = "+x+"]";
    }

}