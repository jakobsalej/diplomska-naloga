package database;

import android.provider.BaseColumns;

public final class OrderDocumentJSONHelper {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private OrderDocumentJSONHelper() {}

    /* Inner class that defines the table contents */
    public static class OrderDocumentJSONEntry implements BaseColumns {
        public static final String TABLE_NAME = "orderDocumentJSON";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DATA = "data";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_DELIVERED = "delivered";
        public static final String COLUMN_NAME_CUSTOMER = "customer";
        public static final String COLUMN_NAME_START_LOCATION = "startLocation";
        public static final String COLUMN_NAME_END_LOCATION = "endLocation";
        public static final String COLUMN_NAME_MIN_TEMP = "minTemp";
        public static final String COLUMN_NAME_MAX_TEMP = "maxTemp";
        public static final String COLUMN_NAME_MEASUREMENTS = "measurements";
    }
}
