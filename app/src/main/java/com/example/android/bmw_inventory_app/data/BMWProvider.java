package com.example.android.bmw_inventory_app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.bmw_inventory_app.data.BMWContract.BMWEntry;


public class BMWProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = BMWProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the bmw table */
    private static final int BMW = 100;

    /** URI matcher code for the content URI for a single product in the bmw table */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(BMWContract.CONTENT_AUTHORITY, BMWContract.PATH_BMW,BMW);
        sUriMatcher.addURI(BMWContract.CONTENT_AUTHORITY, BMWContract.PATH_BMW +"/#",PRODUCT_ID);
    }


    /** Database helper that will provide us access to the database */
    private BMWDbHelper mDbHelper;


    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new BMWDbHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BMW:
                // For the BMW code, query the bmw table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the bmw table.
                cursor =database.query(BMWEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // For an example, URI such as "content://com.example.android.bmw_inventory/bmw/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = BMWEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the bmw table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(BMWEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BMW:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {

        // Check that the brand name is not null
        String brand_name = values.getAsString(BMWEntry.COLUMN_BMW_BRAND_NAME);
        if (brand_name == null ) {
            throw new IllegalArgumentException("product requires brand_name");
        }

        // Check that the quantity is not null
        Integer quantity = values.getAsInteger(BMWEntry.COLUMN_BMW_QUANTITY);
        if (quantity != null && quantity <0) {
            throw new IllegalArgumentException("product requires valid quantity");
        }

        // Check that the price is not null
        Integer price = values.getAsInteger(BMWEntry.COLUMN_BMW_PRODUCT_PRICE);
        if (price != null && price <0) {
            throw new IllegalArgumentException("product requires valid price");
        }


        // Check that the product model is not null
        String model = values.getAsString(BMWEntry.COLUMN_BMW_MODEL);
        if (model == null) {
            throw new IllegalArgumentException("product model is required");
        }

        // Check that the transmission is valid
        Integer transmission = values.getAsInteger(BMWEntry.COLUMN_BMW_TRANSMISSION);
        if (transmission == null || !BMWEntry.isValidTransmission(transmission)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        // Check that the fuel type is valid
        Integer fuel = values.getAsInteger(BMWEntry.COLUMN_BMW_FUEL);
        if (fuel == null || !BMWEntry.isValidFuel(fuel)) {
            throw new IllegalArgumentException("product requires valid fuel type");
        }

        // No need to check the , any value is valid (including null).

        // Check that the product image is not null
        String image = values.getAsString(BMWEntry.COLUMN_BMW_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("product image is required");
        }

        // Check that the client name is not null
        String name = values.getAsString(BMWEntry.COLUMN_BMW_CLIENT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Seller's name is required");
        }

        // Check that the client phone number is not null
        String phone = values.getAsString(BMWEntry.COLUMN_BMW_CLIENT_PHONE);
        if (phone == null) {
            throw new IllegalArgumentException("Client phone number is required");
        }


        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(BMWEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BMW:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = BMWEntry.BMW_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link BMWEntry#COLUMN_BMW_BRAND_NAME} key is present,
        // check that the brand_name value is not null
        if (values.containsKey(BMWEntry.COLUMN_BMW_BRAND_NAME)) {
            String brand_name = values.getAsString(BMWEntry.COLUMN_BMW_BRAND_NAME);
            if (brand_name == null) {
                throw new IllegalArgumentException("Product requires valid brand_name");
            }
        }

        // If the {@link BMWEntry#COLUMN_BMW_QUANTITY} key is present,
        // check that the quantity value is not null.
        if (values.containsKey(BMWEntry.COLUMN_BMW_QUANTITY)) {
            Integer quantity = values.getAsInteger(BMWEntry.COLUMN_BMW_QUANTITY);
            if (quantity != null && quantity <0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }

        // If the {@link BMWEntry#COLUMN_BMW_PRODUCT_PRICE} key is present,
        // check that the name value is not null.
        if (values.containsKey(BMWEntry.COLUMN_BMW_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(BMWEntry.COLUMN_BMW_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product price is required");
            }
        }


        // If the {@link BMWEntry#COLUMN_BMW_MODEL} key is present,
        // check that the model value is not null.
        if (values.containsKey(BMWEntry.COLUMN_BMW_MODEL)) {
            String model = values.getAsString(BMWEntry.COLUMN_BMW_MODEL);
            if (model == null) {
                throw new IllegalArgumentException("Product model is required");
            }
        }

        // If the {@link BMWEntry#COLUMN_BMW_TRANSMISSION} key is present,
        // check that the transmission value is valid.
        if (values.containsKey(BMWEntry.COLUMN_BMW_TRANSMISSION)) {
            Integer transmission = values.getAsInteger(BMWEntry.COLUMN_BMW_TRANSMISSION);
            if (transmission == null || !BMWEntry.isValidTransmission(transmission)) {
                throw new IllegalArgumentException("Product requires valid cylinder");
            }
        }

        // If the {@link BMWEntry#COLUMN_BMW_FUEL} key is present,
        // check that the fuel value is valid.
        if (values.containsKey(BMWEntry.COLUMN_BMW_FUEL)) {
            Integer fuel = values.getAsInteger(BMWEntry.COLUMN_BMW_FUEL);
            if (fuel == null || !BMWEntry.isValidFuel(fuel)) {
                throw new IllegalArgumentException("Product requires valid fuel");
            }
        }

        // If the {@link BMWEntry#COLUMN_BMW_IMAGE} key is present,
        // check that the image value is not null.
        if (values.containsKey(BMWEntry.COLUMN_BMW_IMAGE)) {
            String image = values.getAsString(BMWEntry.COLUMN_BMW_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Product image is required");
            }
        }

        // If the {@link BMWEntry#COLUMN_BMW_CLIENT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(BMWEntry.COLUMN_BMW_CLIENT_NAME)) {
            String name = values.getAsString(BMWEntry.COLUMN_BMW_CLIENT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Seller's name is required");
            }
        }


        // If the {@link BMWEntry#COLUMN_BMW_CLIENT_PHONE} key is present,
        // check that the phone value is not null.
        if (values.containsKey(BMWEntry.COLUMN_BMW_CLIENT_PHONE)) {
            String phone = values.getAsString(BMWEntry.COLUMN_BMW_CLIENT_PHONE);
            if (phone == null) {
                throw new IllegalArgumentException("Seller's phone number is required");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BMWEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed

        if (rowsUpdated != 0) {

            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BMW:
                // Delete all rows that match the selection and selection args
                // For  case BMW:
                rowsDeleted = database.delete(BMWEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = BMWEntry.BMW_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                // For case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                rowsDeleted = database.delete(BMWEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BMW:
                return BMWEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return BMWEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}