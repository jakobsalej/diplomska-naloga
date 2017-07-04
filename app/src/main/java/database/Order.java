package database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Order {

    int id;
    String title;
    String date;

    // Empty constructor
    public Order(){

    }

    // constructor
    public Order(int id, String name, String date){
        this.id = id;
        this.title = title;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }
}


