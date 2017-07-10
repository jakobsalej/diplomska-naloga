package database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by jakob on 7/10/17.
 */

public class Customer implements Serializable
{

    @SerializedName("customerID")
    @Expose
    private Integer customerID;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("address")
    @Expose
    private Address address;
    @SerializedName("_id")
    @Expose
    private String id;
    private final static long serialVersionUID = -1932588972199328047L;

    /**
     * No args constructor for use in serialization
     *
     */
    public Customer() {
    }

    /**
     *
     * @param id
     * @param address
     * @param name
     * @param customerID
     */
    public Customer(Integer customerID, String name, Address address, String id) {
        super();
        this.customerID = customerID;
        this.name = name;
        this.address = address;
        this.id = id;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ClassPojo [_id = "+id+", address = "+address+", name = "+name+", customerID = "+customerID+"]";
    }

}
