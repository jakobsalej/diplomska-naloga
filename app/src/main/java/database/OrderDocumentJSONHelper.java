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
    }
}
