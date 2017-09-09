package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.jakob.qrreader.Main2Activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import database.OrderDocumentJSONHelper.OrderDocumentJSONEntry;

import static android.R.attr.id;

public class DatabaseHandler extends SQLiteOpenHelper {

    // app's database for storing data we want to retain during reopening

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 53;
    public static final String DATABASE_NAME = "db";
    private static final String TAG = "DatabaseHandler";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + OrderDocumentJSONEntry.TABLE_NAME + " (" +
                    OrderDocumentJSONEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    OrderDocumentJSONEntry.COLUMN_NAME_TITLE + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_DATA + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_STATUS + " INTEGER," +
                    OrderDocumentJSONEntry.COLUMN_NAME_DELIVERED + " INTEGER," +
                    OrderDocumentJSONEntry.COLUMN_NAME_DATE + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_START_LOCATION + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_END_LOCATION + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_MIN_TEMP + " DOUBLE," +
                    OrderDocumentJSONEntry.COLUMN_NAME_MAX_TEMP + " DOUBLE," +
                    OrderDocumentJSONEntry.COLUMN_NAME_MEASUREMENTS + " TEXT," +
                    OrderDocumentJSONEntry.COLUMN_NAME_START_INDEX + " INTEGER," +
                    OrderDocumentJSONEntry.COLUMN_NAME_END_INDEX + " INTEGER)";


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

        Log.v(TAG, "Adding order document to DB" + od.toString());
        ContentValues values = new ContentValues();
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_ID, od.getId());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_TITLE, od.getTitle());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_DATA, od.getData());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_STATUS, od.getStatus());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_DELIVERED, od.getDelivered());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_DATE, new Date().getTime());      // date added to local DB
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
        Log.v(TAG, "MMMM " + od.getMeasurements());
        ContentValues values = new ContentValues();
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_ID, od.getId());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_TITLE, od.getTitle());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_DATA, od.getData());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_STATUS, od.getStatus());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_DELIVERED, od.getDelivered());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_DATE, od.getDate());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_START_LOCATION, od.getStartLocation());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_END_LOCATION, od.getEndLocation());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_MIN_TEMP, od.getMinTemp());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_MAX_TEMP, od.getMaxTemp());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_MEASUREMENTS, od.getMeasurements());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_START_INDEX, od.getStartIndex());
        values.put(OrderDocumentJSONEntry.COLUMN_NAME_END_INDEX, od.getEndIndex());

        // Inserting Row
        SQLiteDatabase db = Main2Activity.db;
        db.beginTransaction();
        int updatedRows = db.update(OrderDocumentJSONEntry.TABLE_NAME, values,
                OrderDocumentJSONEntry.COLUMN_NAME_ID + " = ?",
                new String[] { String.valueOf(od.getId()) });

        db.setTransactionSuccessful();
        db.endTransaction();

        return updatedRows;
    }


    public static OrderDocumentJSON getOrder(int id) {
        SQLiteDatabase db = Main2Activity.db;
        String selectQuery =
                "SELECT  * FROM " + OrderDocumentJSONEntry.TABLE_NAME + " WHERE " +
                        OrderDocumentJSONEntry.COLUMN_NAME_ID + "=" + String.valueOf(id);
        Log.v("DB query", selectQuery);
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (!cursor.moveToFirst()) {
            Log.v("DB", "No entry with such ID to get!");
            return null;
        }

        // create new object
        OrderDocumentJSON od = new OrderDocumentJSON(
                        cursor.getInt(0),           // id
                        cursor.getString(1),        // title
                        cursor.getString(2),        // data
                        cursor.getInt(3),           // status
                        cursor.getInt(4),           // delivered
                        cursor.getString(5),        // date
                        cursor.getString(6),        // startLocation
                        cursor.getString(7),        // endLocation
                        cursor.getDouble(8),        // minTemp
                        cursor.getDouble(9),        // maxTemp
                        cursor.getString(10),       // measurements
                        cursor.getInt(11),          // startIndex
                        cursor.getInt(12)           // endIndex
        );
        Log.v("DB", "Getting one order!");
        od.printValues();
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
                    OrderDocumentJSONEntry.COLUMN_NAME_STATUS+ "=" + status + " ORDER BY " +
                    OrderDocumentJSONEntry.COLUMN_NAME_DATE + " DESC";
        }
        SQLiteDatabase db = Main2Activity.db;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                // create new object
                OrderDocumentJSON od = new OrderDocumentJSON(
                        cursor.getInt(0),           // id
                        cursor.getString(1),        // title
                        cursor.getString(2),        // data
                        cursor.getInt(3),           // status
                        cursor.getInt(4),           // delivered
                        cursor.getString(5),        // date
                        cursor.getString(6),        // startLocation
                        cursor.getString(7),        // endLocation
                        cursor.getDouble(8),        // minTemp
                        cursor.getDouble(9),        // maxTemp
                        cursor.getString(10),       // measurements
                        cursor.getInt(11),          // startIndex
                        cursor.getInt(12)           // endIndex
                );
                Log.v("DATABASE", od.toString());
                odList.add(od);
            } while (cursor.moveToNext());
        }



        return odList;
    }
}