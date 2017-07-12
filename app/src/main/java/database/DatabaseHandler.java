package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.jakob.qrreader.OrdersActivity;

import java.util.ArrayList;
import java.util.List;

import database.OrderDocumentJSONHelper.OrderDocumentJSONEntry;

import static android.R.attr.logo;
import static android.R.attr.order;
import static database.OrderDocumentHelper.DocumentEntry.COLUMN_NAME_TEXT;

public class DatabaseHandler extends SQLiteOpenHelper {

    // app's database for storing data we want to retain during reopening

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "db";
    private static final String TAG = "DatabaseHandler";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + OrderDocumentJSONEntry.TABLE_NAME + " (" +
                    OrderDocumentJSONEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    OrderDocumentJSONEntry.COLUMN_NAME_TITLE + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_DATA + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_STATUS + " INTEGER," +
                    OrderDocumentJSONEntry.COLUMN_NAME_DELIVERED + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + OrderDocumentJSONEntry.TABLE_NAME;


    public DatabaseHandler (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.v(TAG, SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    // CRUD operations

    // CREATE
    public static void addOrder(OrderDocumentJSON od) {

        Log.v(TAG, "Adding order document " + od.toString());
        ContentValues values = new ContentValues();
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_ID, od.getId());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_TITLE, od.getTitle());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_DATA, od.getData());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_STATUS, od.getStatus());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_DELIVERED, od.getDelivered());

        // Inserting Row
        SQLiteDatabase db = OrdersActivity.db;
        db.insert(OrderDocumentJSONEntry.TABLE_NAME, null, values);

        // TODO: close connection when exiting OrdersActivity
        //db.close(); // Closing database connection
    }


    public static OrderDocumentJSON getOrder(int id) {
        SQLiteDatabase db = OrdersActivity.db;
        String selectQuery = "SELECT  * FROM " + OrderDocumentJSONEntry.TABLE_NAME + " WHERE " + OrderDocumentJSONEntry.COLUMN_NAME_ID + "=" + String.valueOf(id);
        Log.v("DB query", selectQuery);
        Cursor cursor = db.rawQuery(selectQuery, null);

        //Cursor cursor =  db.query(OrderDocumentJSONEntry.TABLE_NAME, new String[] { " * " }, OrderDocumentJSONEntry.COLUMN_NAME_ID + "=?",
        //        new String[] { String.valueOf(id) }, null, null, null, null);

        if (!cursor.moveToFirst()) {
            Log.v("DB", "No entry with such ID to get!");
            return null;
        }

        OrderDocumentJSON od = new OrderDocumentJSON(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4));
        return od;

    }


    public static ArrayList<OrderDocumentJSON> getOrders() {
        ArrayList<OrderDocumentJSON> odList = new ArrayList<OrderDocumentJSON>();

        String selectQuery = "SELECT * FROM " + OrderDocumentJSONEntry.TABLE_NAME;
        SQLiteDatabase db = OrdersActivity.db;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Log.v("DATABASE", String.valueOf(cursor));
                OrderDocumentJSON od = new OrderDocumentJSON();
                od.setId(cursor.getInt(0));
                od.setTitle(cursor.getString(1));
                od.setData(cursor.getString(2));
                od.setStatus(cursor.getInt(3));
                od.setDelivered(cursor.getInt(4));
                //Log.v("DATABASEALL", cursor.getString(0) + ' ' + cursor.getString(1) + ' ' + cursor.getString(2));

                odList.add(od);
            } while (cursor.moveToNext());
        }

        return odList;
    }

}