package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

    // app's database for storing data we want to retain during reopening

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "OrderDocument.db";
    private static final String TAG = "DatabaseHandler";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + OrderDocument.DocumentEntry.TABLE_NAME + " (" +
                    OrderDocument.DocumentEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    OrderDocument.DocumentEntry.COLUMN_NAME_TITLE + " TEXT," +
                    OrderDocument.DocumentEntry.COLUMN_NAME_SUBTITLE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + OrderDocument.DocumentEntry.TABLE_NAME;


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
    void addContact(Order order, SQLiteDatabase db) {

        ContentValues values = new ContentValues();
        values.put(OrderDocument.DocumentEntry.COLUMN_NAME_TITLE, order.getTitle()); // Contact Name
        values.put(OrderDocument.DocumentEntry.COLUMN_NAME_SUBTITLE, order.getDate()); // Contact Phone

        // Inserting Row
        db.insert(OrderDocument.DocumentEntry.TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

}