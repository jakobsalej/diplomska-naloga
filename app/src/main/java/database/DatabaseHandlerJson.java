package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.jakob.qrreader.OrdersActivity;

import java.util.ArrayList;

import database.OrderDocumentHelper.DocumentEntry;

public class DatabaseHandlerJson extends SQLiteOpenHelper {

    // app's database for storing data we want to retain during reopening

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "db";
    private static final String TAG = "DatabaseHandler";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DocumentEntry.TABLE_NAME + " (" +
                    DocumentEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    DocumentEntry.COLUMN_NAME_TITLE + " TEXT," +
                    DocumentEntry.COLUMN_NAME_TEXT + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DocumentEntry.TABLE_NAME;


    public DatabaseHandlerJson(Context context) {
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
    public static void addOrder(OrderDocument od) {

        Log.v(TAG, "Adding order document " + od.getTitle() + ' ' + od.getText());
        ContentValues values = new ContentValues();
        values.put(DocumentEntry.COLUMN_NAME_TITLE, od.getTitle()); // Contact Name
        values.put(DocumentEntry.COLUMN_NAME_TEXT, od.getText()); // Contact Phone

        // Inserting Row
        SQLiteDatabase db = OrdersActivity.db;
        db.insert(DocumentEntry.TABLE_NAME, null, values);

        // TODO: close connection when exiting OrdersActivity
        //db.close(); // Closing database connection
    }

    public static ArrayList<OrderDocument> getOrders() {
        ArrayList<OrderDocument> odList = new ArrayList<OrderDocument>();

        String selectQuery = "SELECT * FROM " + DocumentEntry.TABLE_NAME;
        SQLiteDatabase db = OrdersActivity.db;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Log.v("DATABASE", String.valueOf(cursor));
                OrderDocument od = new OrderDocument();
                od.setId(cursor.getString(0));
                od.setText(cursor.getString(2));
                od.setTitle(cursor.getString(1));
                Log.v("DATABASEALL", cursor.getString(0) + ' ' + cursor.getString(1) + ' ' + cursor.getString(2));


                odList.add(od);
            } while (cursor.moveToNext());
        }

        return odList;
    }

}