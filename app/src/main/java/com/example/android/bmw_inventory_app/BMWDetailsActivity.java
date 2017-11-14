package com.example.android.bmw_inventory_app;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bmw_inventory_app.data.BMWContract.BMWEntry;

import static java.lang.Integer.parseInt;

/**
 * Allows user to view details of products listed in the {@link BMWMainActivity}
 */
public class BMWDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the product data loader */
    private static final int BMW_LOADER = 1;

    private ImageView mImage;
    private TextView brandNameTextView;
    private TextView quantityTextView;
    private TextView modelTextView;
    private TextView mPriceTextView;
    private TextView transmissionTextView;
    private TextView fuelTextView;
    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView emailTextView;

    //URI of the product for which details is requested
    private Uri mCurrentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmwdetails);

        // Setup FAB to open the editor
        FloatingActionButton fab =  findViewById(R.id.fab_editor);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent editorIntent = new Intent(BMWDetailsActivity.this, BMWEditorActivity.class);
                editorIntent.setData(mCurrentProductUri);
                startActivity(editorIntent);
            }
        });

        //Get the URI of the product clicked from previous activity
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //Reference views and set OnClickListener
        mImage = findViewById(R.id.product_image);
        brandNameTextView = findViewById(R.id.brand_name_display);
        modelTextView =  findViewById(R.id.model_display);
        mPriceTextView =  findViewById(R.id.price_display);
        quantityTextView =  findViewById(R.id.quantity_display);
        fuelTextView =  findViewById(R.id.fuel_display);
        transmissionTextView = findViewById(R.id.transmission_display);
        nameTextView = findViewById(R.id.name_display);
        phoneTextView = findViewById(R.id.phone_display);
        emailTextView = findViewById(R.id.email_display);
        Button decrementButton = findViewById(R.id.decrement_button);
        Button incrementButton = findViewById(R.id.increment_button);
        Button phoneButton = findViewById(R.id.phone_button);
        Button emailButton = findViewById(R.id.email_button);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String writeEmail = emailTextView.getText().toString();
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this.
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, writeEmail);
                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                }
            }
        });

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String callCustomer = phoneTextView.getText().toString();
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + callCustomer));
                if (callIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(callIntent);
                }
            }
        });

        // Set a clickListener on decrement button
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(quantityTextView.getText().toString())) {
                    int iQuantity = parseInt(quantityTextView.getText().toString().trim());
                    if (iQuantity == 0) {
                        iQuantity = 0;
                        Toast.makeText(view.getContext(), getString(R.string.quantity_error),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        iQuantity--;
                    }
                    quantityTextView.setText(String.valueOf(iQuantity));
                    saveQuantity(iQuantity);
                }
            }
        });

        // Set a clickListener on increment button
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mQuantity;
                if (!TextUtils.isEmpty(quantityTextView.getText().toString())) {
                    mQuantity = parseInt(quantityTextView.getText().toString().trim());
                    mQuantity++;
                } else {
                    mQuantity = 1;
                }
                quantityTextView.setText(String.valueOf(mQuantity));
                saveQuantity(mQuantity);

            }
        });

        //let's kick off the loader
        getLoaderManager().initLoader(BMW_LOADER, null, this);
    }

    private void saveQuantity(int save){
        ContentValues quantityValues = new ContentValues();
        quantityValues.put(BMWEntry.COLUMN_BMW_QUANTITY, save);
        getContentResolver().update(mCurrentProductUri, quantityValues, null, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.bmw_menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.home_button:
                takeMeHome();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //We need all columns this time
        String[] projection = {
                BMWEntry.BMW_ID,
                BMWEntry.COLUMN_BMW_BRAND_NAME,
                BMWEntry.COLUMN_BMW_QUANTITY,
                BMWEntry.COLUMN_BMW_PRODUCT_PRICE,
                BMWEntry.COLUMN_BMW_MODEL,
                BMWEntry.COLUMN_BMW_TRANSMISSION,
                BMWEntry.COLUMN_BMW_FUEL,
                BMWEntry.COLUMN_BMW_IMAGE,
                BMWEntry.COLUMN_BMW_CLIENT_NAME,
                BMWEntry.COLUMN_BMW_CLIENT_PHONE,
                BMWEntry.COLUMN_BMW_CLIENT_EMAIL
        };
        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        //If cursor has data move to first and read
        if (cursor.moveToFirst()) {
            //Get index of column in cursor
            int brandNameColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_BRAND_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_PRODUCT_PRICE);
            int modelColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_MODEL);
            int transmissionColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_TRANSMISSION);
            int fuelColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_FUEL);
            int imageColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_CLIENT_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_CLIENT_PHONE);
            int emailColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_CLIENT_EMAIL);

            //Get data from column using column index and store them in variables
            String brand = cursor.getString(brandNameColumnIndex);
            Integer quantity = cursor.getInt(quantityColumnIndex);
            Integer price = cursor.getInt(priceColumnIndex);
            String model = cursor.getString(modelColumnIndex);
            Integer transmission = cursor.getInt(transmissionColumnIndex);
            Integer fuel = cursor.getInt(fuelColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            String email = cursor.getString(emailColumnIndex);

            //Present the data to user
            Uri selectedImageUri = Uri.parse(image);
            mImage.setImageURI(selectedImageUri);
            brandNameTextView.setText(brand);
            quantityTextView.setText(String.valueOf(quantity));
            modelTextView.setText(model);
            mPriceTextView.setText(String.valueOf(price));
            nameTextView.setText(name);
            phoneTextView.setText(phone);
            emailTextView.setText(email);

            String iTransmission;
            if (transmission == BMWEntry.BMW_TRANSMISSION_MANUAL) {
                iTransmission = getString(R.string.transmission_manual);
            }else if (transmission == BMWEntry.BMW_TRANSMISSION_SEMI_AUTOMATIC) {
                iTransmission = getString(R.string.transmission_semi_auto);
            }else {
                iTransmission = getString(R.string.transmission_automatic);
            }
            transmissionTextView.setText(iTransmission);

            String iFuel;
            if (fuel == BMWEntry.FUEL_DIESEL)
                iFuel = getString(R.string.fuel_diesel);
            else if (fuel == BMWEntry.FUEL_HYBRID)
                iFuel = getString(R.string.fuel_hybrid);
            else iFuel = getString(R.string.fuel_petrol);
            fuelTextView.setText(iFuel);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImage.setImageURI(null);
        brandNameTextView.setText("");
        quantityTextView.setText("");
        modelTextView.setText("");
        mPriceTextView.setText("");
        nameTextView.setText("");
        phoneTextView.setText("");
        emailTextView.setText("");
        transmissionTextView.setText("");
        fuelTextView.setText("");
    }

    /**
     * takes the user back to the home page
     */
    private void takeMeHome(){
        Intent homeIntent = new Intent(BMWDetailsActivity.this, BMWMainActivity.class);
        homeIntent.setData(mCurrentProductUri);
        startActivity(homeIntent);

    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}