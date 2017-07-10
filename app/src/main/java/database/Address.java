package database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by jakob on 7/10/17.
 */

public class Address implements Serializable
{

    @SerializedName("street")
    @Expose
    private String street;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("cityCode")
    @Expose
    private Integer cityCode;
    @SerializedName("country")
    @Expose
    private String country;
    private final static long serialVersionUID = 579305809781395390L;

    /**
     * No args constructor for use in serialization
     *
     */
    public Address() {
    }

    /**
     *
     * @param street
     * @param cityCode
     * @param country
     * @param city
     */
    public Address(String street, String city, Integer cityCode, String country) {
        super();
        this.street = street;
        this.city = city;
        this.cityCode = cityCode;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getCityCode() {
        return cityCode;
    }

    public void setCityCode(Integer cityCode) {
        this.cityCode = cityCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "ClassPojo [street = "+street+", cityCode = "+cityCode+", country = "+country+", city = "+city+"]";
    }

}
