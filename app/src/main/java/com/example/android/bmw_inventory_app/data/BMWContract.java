package com.example.android.bmw_inventory_app.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BMWContract {

    private static final String TAG = "bmw";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private BMWContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.bmw_inventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.bmw_inventory/bmw/ is a valid path for
     * looking at bmw data. content://com.example.android.bmw/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_BMW = "bmw_product_brands";

    /**
     * Inner class that defines constant values for the bmw database table.
     * Each entry in the table represents a single product.
     */
    public static final class BMWEntry implements BaseColumns {

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of bmw products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BMW;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BMW;

        /**
         * The content URI to access the bmw data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BMW);

        /**
         * Name of database table for bmw
         */
        public final static String TABLE_NAME = "bmw";

        /**
         * Unique ID number for the product (only for use in the database table).
         * Type: INTEGER
         */

        public final static String BMW_ID = BaseColumns._ID;

        /**
         * brand of the product.
         * Type: TEXT
         */

        public final static String COLUMN_BMW_BRAND_NAME = "brand_name";

        /**
         * Cylinder speed of the product.
         * <p>
         * Type: INTEGER
         */

        public final static String COLUMN_BMW_QUANTITY = "quantity";

        /**
         * Price of the product.
         * <p>
         * Type: REAL
         */
        public final static String COLUMN_BMW_PRODUCT_PRICE = "price";

        /**
         * Model of the product.
         * <p>
         * <p>
         * Type: TEXT
         */

        public final static String COLUMN_BMW_MODEL = "model";

        /**
         * Image of the product.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_BMW_IMAGE = "image";

        /**
         * fuel type of the product.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_BMW_FUEL = "fuel";

        /**
         * Transmission type of the product.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_BMW_TRANSMISSION = "transmission";

        /**
         * Name of the client (seller).
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_BMW_CLIENT_NAME = "name";

        /**
         * Contact phone of the client(seller).
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_BMW_CLIENT_PHONE = "phone";

        /**
         * Contact Email of the client (seller).
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_BMW_CLIENT_EMAIL = "email";


        /**
         * Possible values for the fuel type of the product.
         */

        public static final int FUEL_PETROL = 0;

        public static final int FUEL_DIESEL = 1;

        public static final int FUEL_HYBRID = 2;


        /**
         * Possible values for the transmission of the product.
         */

        public static final int BMW_TRANSMISSION_AUTOMATIC = 0;

        public static final int BMW_TRANSMISSION_MANUAL = 1;

        public static final int BMW_TRANSMISSION_SEMI_AUTOMATIC = 2;


        /**
         * Returns whether or not the given transmission type is {@link #BMW_TRANSMISSION_AUTOMATIC},
         * {@link #BMW_TRANSMISSION_MANUAL} or {@link #BMW_TRANSMISSION_SEMI_AUTOMATIC}.
         */
        public static boolean isValidTransmission(int transmission) {
            if (transmission == BMW_TRANSMISSION_AUTOMATIC || transmission == BMW_TRANSMISSION_MANUAL
                    || transmission == BMW_TRANSMISSION_SEMI_AUTOMATIC) {
                return true;
            }
            return false;
        }

        /**
         * Returns whether or not the given fuel type is {@link #FUEL_PETROL}
         * {@link #FUEL_DIESEL} or {@link #FUEL_HYBRID}.
         */
        public static boolean isValidFuel(int fuel) {
            if (fuel == FUEL_PETROL || fuel == FUEL_DIESEL || fuel == FUEL_HYBRID) {
                return true;
            }
            return false;
        }

    }

}
