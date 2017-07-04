package database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jakob on 7/4/17.
 */

public class OrderDocument {

    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("documentID")
    @Expose
    private Integer documentID;
    @SerializedName("__v")
    @Expose
    private Integer v;

    /**
     * No args constructor for use in serialization
     *
     */
    public OrderDocument() {
    }

    /**
     *
     * @param id
     * @param v
     * @param documentID
     * @param title
     * @param text
     */
    public OrderDocument(String id, String text, String title, Integer documentID, Integer v) {
        super();
        this.id = id;
        this.text = text;
        this.title = title;
        this.documentID = documentID;
        this.v = v;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDocumentID() {
        return documentID;
    }

    public void setDocumentID(Integer documentID) {
        this.documentID = documentID;
    }

    public Integer getV() {
        return v;
    }

    public void setV(Integer v) {
        this.v = v;
    }

}
