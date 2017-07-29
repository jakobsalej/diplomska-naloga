package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.jakob.qrreader.Main2Activity;

import java.util.ArrayList;
import java.util.List;

import database.OrderDocumentJSONHelper.OrderDocumentJSONEntry;

import static android.R.attr.id;

public class DatabaseHandler extends SQLiteOpenHelper {

    // app's database for storing data we want to retain during reopening

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 13;
    public static final String DATABASE_NAME = "db";
    private static final String TAG = "DatabaseHandler";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + OrderDocumentJSONEntry.TABLE_NAME + " (" +
                    OrderDocumentJSONEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    OrderDocumentJSONEntry.COLUMN_NAME_TITLE + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_DATA + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_STATUS + " INTEGER," +
                    OrderDocumentJSONEntry.COLUMN_NAME_CUSTOMER + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_START_LOCATION + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_END_LOCATION + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_MIN_TEMP + " DOUBLE," +
                    OrderDocumentJSONEntry.COLUMN_NAME_MAX_TEMP + " DOUBLE," +
                    OrderDocumentJSONEntry.COLUMN_NAME_MEASUREMENTS + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_START_INDEX + " INTEGER," +
                    OrderDocumentJSONEntry.COLUMN_NAME_END_INDEX + " INTEGER," +
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
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_CUSTOMER, od.getCustomer());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_START_LOCATION, od.getStartLocation());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_END_LOCATION, od.getEndLocation());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_MIN_TEMP, od.getMinTemp());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_MAX_TEMP, od.getMaxTemp());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_MEASUREMENTS, od.getMeasurements());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_START_INDEX, od.getStartIndex());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_END_INDEX, od.getEndIndex());

        // Inserting Row
        SQLiteDatabase db = Main2Activity.db;
        db.insert(OrderDocumentJSONEntry.TABLE_NAME, null, values);

        // TODO: close connection when exiting Main2Activity
        //db.close(); // Closing database connection
    }

    // UPDATE
    public static int updateOrder(OrderDocumentJSON od) {

        Log.v(TAG, "Updating order document " + od.toString());
        ContentValues values = new ContentValues();
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_MEASUREMENTS, od.getMeasurements());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_END_INDEX, od.getEndIndex());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_STATUS, od.getStatus());

        // Inserting Row
        SQLiteDatabase db = Main2Activity.db;
        return db.update(OrderDocumentJSONEntry.TABLE_NAME, values,
                OrderDocumentJSONEntry.COLUMN_NAME_ID + " = ?",
                new String[] { String.valueOf(od.getId()) });
    }


    public static OrderDocumentJSON getOrder(int id) {
        SQLiteDatabase db = Main2Activity.db;
        String selectQuery =
                "SELECT  * FROM " + OrderDocumentJSONEntry.TABLE_NAME + " WHERE " +
                        OrderDocumentJSONEntry.COLUMN_NAME_ID + "=" + String.valueOf(id);
        Log.v("DB query", selectQuery);
        Cursor cursor = db.rawQuery(selectQuery, null);

        //Cursor cursor =  db.query(OrderDocumentJSONEntry.TABLE_NAME, new String[] { " * " }, OrderDocumentJSONEntry.COLUMN_NAME_ID + "=?",
        //        new String[] { String.valueOf(id) }, null, null, null, null);

        if (!cursor.moveToFirst()) {
            Log.v("DB", "No entry with such ID to get!");
            return null;
        }

        // create new object
        OrderDocumentJSON od = new OrderDocumentJSON(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getDouble(8),
                        cursor.getDouble(9),
                        cursor.getString(10),
                        cursor.getInt(11),
                        cursor.getInt(12)
        );
        return od;

    }


    public static ArrayList<OrderDocumentJSON> getOrders(int status) {
        ArrayList<OrderDocumentJSON> odList = new ArrayList<OrderDocumentJSON>();

        String selectQuery = "";
        if (status == -1) {
            selectQuery = "SELECT * FROM " + OrderDocumentJSONEntry.TABLE_NAME;
        } else {
            // get back documents based on status
            selectQuery = "SELECT  * FROM " + OrderDocumentJSONEntry.TABLE_NAME + " WHERE " +
                    OrderDocumentJSONEntry.COLUMN_NAME_STATUS+ "=" + status;
        }
        SQLiteDatabase db = Main2Activity.db;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Log.v("DATABASE", String.valueOf(cursor));

                // create new object
                OrderDocumentJSON od = new OrderDocumentJSON(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getDouble(8),
                        cursor.getDouble(9),
                        cursor.getString(10),
                        cursor.getInt(11),
                        cursor.getInt(12)
                );
                odList.add(od);
            } while (cursor.moveToNext());
        }

        return odList;
    }
}