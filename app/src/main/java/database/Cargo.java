package database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by jakob on 7/10/17.
 */

public class Cargo implements Serializable
{

    @SerializedName("itemID")
    @Expose
    private Integer itemID;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("company")
    @Expose
    private String company;
    @SerializedName("minTemp")
    @Expose
    private Integer minTemp;
    @SerializedName("maxTemp")
    @Expose
    private Integer maxTemp;
    private final static long serialVersionUID = -2917451307888388000L;

    /**
     * No args constructor for use in serialization
     *
     */
    public Cargo() {
    }

    /**
     *
     * @param maxTemp
     * @param itemID
     * @param company
     * @param name
     * @param minTemp
     */
    public Cargo(Integer itemID, String name, String company, Integer minTemp, Integer maxTemp) {
        super();
        this.itemID = itemID;
        this.name = name;
        this.company = company;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public Integer getItemID() {
        return itemID;
    }

    public void setItemID(Integer itemID) {
        this.itemID = itemID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Integer getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(Integer minTemp) {
        this.minTemp = minTemp;
    }

    public Integer getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(Integer maxTemp) {
        this.maxTemp = maxTemp;
    }

    @Override
    public String toString() {
        return "ClassPojo [maxTemp = "+maxTemp+", itemID = "+itemID+", company = "+company+", name = "+name+", minTemp = "+minTemp+"]";
    }

}