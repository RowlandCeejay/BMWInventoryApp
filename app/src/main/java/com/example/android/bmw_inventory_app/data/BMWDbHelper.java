package com.example.android.bmw_inventory_app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.bmw_inventory_app.data.BMWContract.BMWEntry;

public class BMWDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = BMWDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "bmwProducts.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link BMWDbHelper}.
     *
     * @param context of the app
     */
    public BMWDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_BMW_TABLE = "CREATE TABLE " + BMWEntry.TABLE_NAME + " ("
                + BMWEntry.BMW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BMWEntry.COLUMN_BMW_BRAND_NAME + " TEXT NOT NULL, "
                + BMWEntry.COLUMN_BMW_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + BMWEntry.COLUMN_BMW_PRODUCT_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + BMWEntry.COLUMN_BMW_MODEL + " TEXT NOT NULL, "
                + BMWEntry.COLUMN_BMW_TRANSMISSION + " INTEGER NOT NULL, "
                + BMWEntry.COLUMN_BMW_FUEL + " INTEGER NOT NULL, "
                + BMWEntry.COLUMN_BMW_IMAGE + " TEXT, "
                + BMWEntry.COLUMN_BMW_CLIENT_NAME + " TEXT NOT NULL, "
                + BMWEntry.COLUMN_BMW_CLIENT_PHONE + " TEXT NOT NULL, "
                + BMWEntry.COLUMN_BMW_CLIENT_EMAIL + " TEXT)";
        Log.v(LOG_TAG, SQL_CREATE_BMW_TABLE);
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_BMW_TABLE);

    }

    /**
     * This is called when the database needs to be upgraded.
     */

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}