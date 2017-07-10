package database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jakob on 7/4/17.
 */

public class OrderDocument implements Serializable
{

    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("documentID")
    @Expose
    private Integer documentID;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("customer")
    @Expose
    private Customer customer;
    @SerializedName("startLocation")
    @Expose
    private Location startLocation;
    @SerializedName("endLocation")
    @Expose
    private Location endLocation;
    @SerializedName("dateDeadline")
    @Expose
    private String dateDeadline;
    @SerializedName("items")
    @Expose
    private String items;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("dateUpdated")
    @Expose
    private String dateUpdated;
    @SerializedName("dateCreated")
    @Expose
    private String dateCreated;
    @SerializedName("successfullyDelivered")
    @Expose
    private Boolean successfullyDelivered;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("cargo")
    @Expose
    private List<Cargo> cargo = null;
    @SerializedName("vehicleTypeRequired")
    @Expose
    private Integer vehicleTypeRequired;
    private final static long serialVersionUID = -7307509615082929416L;

    /**
     * No args constructor for use in serialization
     *
     */
    public OrderDocument() {
    }

    /**
     *
     * @param text
     * @param status
     * @param startLocation
     * @param customer
     * @param cargo
     * @param dateUpdated
     * @param id
     * @param dateDeadline
     * @param documentID
     * @param title
     * @param items
     * @param endLocation
     * @param dateCreated
     * @param successfullyDelivered
     * @param vehicleTypeRequired
     */
    public OrderDocument(String id, Integer documentID, String title, Customer customer, Location startLocation, Location endLocation, String dateDeadline, String items, String text, String dateUpdated, String dateCreated, Boolean successfullyDelivered, Integer status, List<Cargo> cargo, Integer vehicleTypeRequired) {
        super();
        this.id = id;
        this.documentID = documentID;
        this.title = title;
        this.customer = customer;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.dateDeadline = dateDeadline;
        this.items = items;
        this.text = text;
        this.dateUpdated = dateUpdated;
        this.dateCreated = dateCreated;
        this.successfullyDelivered = successfullyDelivered;
        this.status = status;
        this.cargo = cargo;
        this.vehicleTypeRequired = vehicleTypeRequired;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getDocumentID() {
        return documentID;
    }

    public void setDocumentID(Integer documentID) {
        this.documentID = documentID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public String getDateDeadline() {
        return dateDeadline;
    }

    public void setDateDeadline(String dateDeadline) {
        this.dateDeadline = dateDeadline;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Boolean getSuccessfullyDelivered() {
        return successfullyDelivered;
    }

    public void setSuccessfullyDelivered(Boolean successfullyDelivered) {
        this.successfullyDelivered = successfullyDelivered;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Cargo> getCargo() {
        return cargo;
    }

    public void setCargo(List<Cargo> cargo) {
        this.cargo = cargo;
    }

    public Integer getVehicleTypeRequired() {
        return vehicleTypeRequired;
    }

    public void setVehicleTypeRequired(Integer vehicleTypeRequired) {
        this.vehicleTypeRequired = vehicleTypeRequired;
    }

    @Override
    public String toString() {
        return "ClassPojo [text = "+text+", status = "+status+", startLocation = "+startLocation+", customer = "+customer+", cargo = "+cargo+", dateUpdated = "+dateUpdated+", dateDeadline = "+dateDeadline+", documentID = "+documentID+", title = "+title+", _id = "+id+", items = "+items+", endLocation = "+endLocation+", dateCreated = "+dateCreated+", successfullyDelivered = "+successfullyDelivered+", vehicleTypeRequired = "+vehicleTypeRequired+"]";
    }

}


