package com.example.android.bmw_inventory_app;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.design.widget.FloatingActionButton;


import com.example.android.bmw_inventory_app.data.BMWContract.BMWEntry;
/**
 * Displays list of products that were entered and stored in the app.
 */
public class BMWMainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int BMW_LOADER =0;

    BMWCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmwmain);

        // Setup FAB to open BMWEditorActivity
        FloatingActionButton fab = findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detailsIntent = new Intent(BMWMainActivity.this, BMWEditorActivity.class);
                startActivity(detailsIntent);
            }
        });


        // Find the ListView which will be populated with the product data
        ListView productListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        mCursorAdapter = new BMWCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        //Set up the OnItemClickListener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //Create a new Intent to go to {@ BMWEditorActivity}
                Intent intent = new Intent(BMWMainActivity.this, BMWDetailsActivity.class);

                Uri currentProductUri = ContentUris.withAppendedId(BMWEntry.CONTENT_URI, id);

                //Set the Uri on the data field of the Intent.
                intent.setData(currentProductUri);

                // Launch the {@Link BMWEditorActivity} to display the edit for the current product.
                startActivity(intent);

            }
        });
        //Let't kick off the loader
        getLoaderManager().initLoader(BMW_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded product data into the database. For debugging purposes only.
     */
    private void insertProducts() {
        // Create a ContentValues object where column names are the keys,
        // and the products attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BMWEntry.COLUMN_BMW_BRAND_NAME, "BMW");
        values.put(BMWEntry.COLUMN_BMW_QUANTITY, 8);
        values.put(BMWEntry.COLUMN_BMW_PRODUCT_PRICE, 180000 );
        values.put(BMWEntry.COLUMN_BMW_MODEL, "X7");
        values.put(BMWEntry.COLUMN_BMW_TRANSMISSION, BMWEntry.BMW_TRANSMISSION_AUTOMATIC);
        values.put(BMWEntry.COLUMN_BMW_FUEL, BMWEntry.FUEL_PETROL);
        values.put(BMWEntry.COLUMN_BMW_IMAGE, "NO_IMAGE");
        values.put(BMWEntry.COLUMN_BMW_CLIENT_NAME, "Ceejay");
        values.put(BMWEntry.COLUMN_BMW_CLIENT_PHONE, "015171437593");
        values.put(BMWEntry.COLUMN_BMW_CLIENT_EMAIL, "rowlandcj1983@gmail.com");

        // Insert a new row  into the provider using the ContentResolver.
        // Use the {@link BMWEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access product's data in the future.
        Uri newUri = getContentResolver().insert(BMWEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all products in the database.
     */
    private void deleteAllProduct() {
        int rowsDeleted = getContentResolver().delete(BMWEntry.CONTENT_URI, null, null);
        Log.v("BMWMainActivity", rowsDeleted + " rows deleted from bmw database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_bmw_main_activity.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_bmw_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.sample_data:
                insertProducts();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProduct();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        String[] projection = {
                BMWEntry._ID,
                BMWEntry.COLUMN_BMW_BRAND_NAME,
                BMWEntry.COLUMN_BMW_MODEL,
                BMWEntry.COLUMN_BMW_PRODUCT_PRICE,
                BMWEntry.COLUMN_BMW_QUANTITY
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, // Parent activity context
                BMWEntry.CONTENT_URI,
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the {@Link BMWCursorAdapter} with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}