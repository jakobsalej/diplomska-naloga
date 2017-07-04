package database;

import android.provider.BaseColumns;

public final class OrderDocumentHelper {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private OrderDocumentHelper() {}

    /* Inner class that defines the table contents */
    public static class DocumentEntry implements BaseColumns {
        public static final String TABLE_NAME = "orderDocument";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TEXT = "text";
    }
}
