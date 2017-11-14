package com.example.android.bmw_inventory_app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.bmw_inventory_app.data.BMWContract.BMWEntry;

import static java.lang.Integer.parseInt;

/**
 * Allows user to create a new product.
 */
public class BMWEditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the product data loader */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /** Content URI for the existing product (null if it's a new product) */
    private Uri mCurrentProductUri;

    /** EditText field to enter the product's brand name */
    private EditText brandNameEdiText;

    /** EditText field to enter the product's price */
    private EditText mPriceEditText;

    /** EditText field to enter the product's model */
    private EditText mModelEditText;

    /** Spinner field to choose between the product's fuel types */
    private Spinner mFuelSpinner;

    /**
     * Fuel type of the product. The possible valid values are in the BMWContract.java file:
     * {@link BMWEntry#FUEL_PETROL}, {@link BMWEntry#FUEL_DIESEL}
     *or {@link BMWEntry#FUEL_HYBRID}.
     */
    private int mFuel = BMWEntry.FUEL_PETROL;

    /** Spinner field to choose between the product's transmission types*/
    private Spinner mTransmissionSpinner;

    /**
     *Transmission type of the product. The possible valid values are in the BMWContract.java file:
     * {@link BMWEntry#BMW_TRANSMISSION_AUTOMATIC}, {@link BMWEntry#BMW_TRANSMISSION_MANUAL}
     *or {@link BMWEntry#BMW_TRANSMISSION_SEMI_AUTOMATIC}.
     */
    private int mTransmission = BMWEntry.BMW_TRANSMISSION_AUTOMATIC;

    /** EditText field to enter the product's quantity */
    private EditText mQuantityEdiText;

    /** EditText field to enter the client seller's name */
    private EditText mNameEditText;

    /** EditText field to enter the client seller's phone */
    private EditText mPhoneEditText;

    /** EditText field to enter the client seller's email */
    private EditText mEmailEditText;

    /** ImageView field to enter the product's image */
    ImageView mImage;

    Integer REQUEST_CAMERA =1, SELECT_FILE =0;

    private Uri selectedImageUri;

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmweditor);

        // Setup FAB to open the camera.
        FloatingActionButton fab =  findViewById(R.id.fab_editor);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SelectImage();
            }
        });

        //Examine the Intent that was used to launch this Activity,
        //in order to figure out if we are creating we are creating a new pet or editing an existing one
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //If the Intent doesn't contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null){
            //This is a new product, so change the app bar top say "Add a product"
            setTitle(getString(R.string.editor_activity_title_add_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        }else {
            // Otherwise this is an existing product, so change app bar to say "edit product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mImage =  findViewById(R.id.product_image);
        brandNameEdiText =  findViewById(R.id.edit_brand_name);
        mQuantityEdiText =  findViewById(R.id.edit_product_quantity);

        mPriceEditText =  findViewById(R.id.edit_product_price);
        mModelEditText =  findViewById(R.id.edit_product_model);
        mFuelSpinner =  findViewById(R.id.spinner_fuel);
        mTransmissionSpinner = findViewById(R.id.spinner_transmission);

        mNameEditText =  findViewById(R.id.edit_client_name);
        mPhoneEditText =  findViewById(R.id.edit_phone_no);
        mEmailEditText =  findViewById(R.id.edit_email_address);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mPriceEditText.setOnTouchListener(mTouchListener);
        mModelEditText.setOnTouchListener(mTouchListener);
        mImage.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        brandNameEdiText.setOnTouchListener(mTouchListener);
        mQuantityEdiText.setOnTouchListener(mTouchListener);
        mFuelSpinner.setOnTouchListener(mTouchListener);
        mTransmissionSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    private void SelectImage(){

        final CharSequence[] items={"Camera", "Gallery", "Cancel"};
        AlertDialog.Builder imageBuilder = new AlertDialog.Builder(BMWEditorActivity.this);
        imageBuilder.setTitle("Add Image");

        imageBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")){
                    Intent cameraIntent;
                    if (ContextCompat.checkSelfPermission(BMWEditorActivity.this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(BMWEditorActivity.this, new String[]{
                                Manifest.permission.CAMERA}, 8);

                    } else {
                        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, REQUEST_CAMERA);
                    }

                } else if (items[i].equals("Gallery")){
                    Intent galleryIntent;
                    if (Build.VERSION.SDK_INT >= 20){
                        galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                } else {
                        galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    }

                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent.createChooser(galleryIntent, "Select File"), SELECT_FILE);

                }else if (items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        imageBuilder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK){

            if (requestCode == REQUEST_CAMERA){

                Bundle bundle = data.getExtras();
                assert bundle != null;
                final Bitmap myBmp = (Bitmap) bundle.get("data");
                mImage.setImageBitmap(myBmp);

            } else if (requestCode ==SELECT_FILE){

                selectedImageUri = data.getData();
                mImage.setImageURI(selectedImageUri);
            }
        }

    }

    /**
     * Setup the dropdown spinner that allows the user to select the appropriate product attribute
     * required.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter fuelSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_fuel_options, android.R.layout.simple_spinner_item);
        // Specify dropdown layout style - simple list view with 1 item per line
        fuelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Apply the adapter to the spinner
        mFuelSpinner.setAdapter(fuelSpinnerAdapter);

        ArrayAdapter transmissionSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_transmission_options, android.R.layout.simple_spinner_item);
        // Specify dropdown layout style - simple list view with 1 item per line
        transmissionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Apply the adapter to the spinner
        mTransmissionSpinner.setAdapter(transmissionSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mFuelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.fuel_diesel))) {
                        mFuel = BMWEntry.FUEL_DIESEL;
                    } else if (selection.equals(getString(R.string.fuel_hybrid))) {
                        mFuel = BMWEntry.FUEL_HYBRID;
                    } else {
                        mFuel = BMWEntry.FUEL_PETROL;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mFuel = BMWEntry.FUEL_PETROL;
            }
        });

        // Set the integer mSelected to the constant values
        mTransmissionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.transmission_manual))) {
                        mTransmission = BMWEntry.BMW_TRANSMISSION_MANUAL;
                    } else if (selection.equals(getString(R.string.transmission_semi_auto))) {
                        mTransmission = BMWEntry.BMW_TRANSMISSION_SEMI_AUTOMATIC;
                    }  else {
                        mTransmission = BMWEntry.BMW_TRANSMISSION_AUTOMATIC;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mTransmission = BMWEntry.BMW_TRANSMISSION_AUTOMATIC;
            }
        });
    }

    /**
     * Get user input from editor and save product into database.
     */
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String brandNameString = brandNameEdiText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String modelString = mModelEditText.getText().toString().trim();
        String nameString = mNameEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String quantityString = mQuantityEdiText.getText().toString().trim();
        Integer fuelInt = mFuel;
        Integer transmissionInt = mTransmission;

        String imageString = null;
        if (mCurrentProductUri == null) {
            if (selectedImageUri != null) imageString = selectedImageUri.toString();
        } else imageString = selectedImageUri.toString();

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(BMWEntry.COLUMN_BMW_BRAND_NAME, brandNameString);
        values.put(BMWEntry.COLUMN_BMW_PRODUCT_PRICE, priceString);
        values.put(BMWEntry.COLUMN_BMW_QUANTITY, quantityString);
        values.put(BMWEntry.COLUMN_BMW_MODEL, modelString);
        values.put(BMWEntry.COLUMN_BMW_TRANSMISSION, transmissionInt);
        values.put(BMWEntry.COLUMN_BMW_FUEL, fuelInt);
        values.put(BMWEntry.COLUMN_BMW_IMAGE, imageString);
        values.put(BMWEntry.COLUMN_BMW_CLIENT_NAME, nameString);
        values.put(BMWEntry.COLUMN_BMW_CLIENT_PHONE, phoneString);
        values.put(BMWEntry.COLUMN_BMW_CLIENT_EMAIL, emailString);


        if (imageString == null) {
            Toast.makeText(this, R.string.image_error, Toast.LENGTH_SHORT).show();
            return;
        }


        if (TextUtils.isEmpty(modelString)) {
            Toast.makeText(this, R.string.model_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, R.string.name_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(brandNameString)) {
            values.put(BMWEntry.COLUMN_BMW_BRAND_NAME, brandNameString);
        } else {
            Toast.makeText(this, R.string.brand_name_error, Toast.LENGTH_SHORT).show();
            return;
        }

        int iPrice;
        if (!TextUtils.isEmpty(priceString)) {
            iPrice = Integer.parseInt(priceString);
            values.put(BMWEntry.COLUMN_BMW_PRODUCT_PRICE, iPrice);
        } else {
            Toast.makeText(this, R.string.price_error, Toast.LENGTH_SHORT).show();
            return;
        }


        int iQuantity;
        if (!TextUtils.isEmpty(quantityString)) {
            iQuantity = parseInt(quantityString);
            values.put(BMWEntry.COLUMN_BMW_QUANTITY, iQuantity);
        } else {
            Toast.makeText(this, R.string.quantity_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phoneString)) {
            Toast.makeText(this, R.string.phone_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(emailString)) {
            Toast.makeText(this, R.string.email_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(BMWEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri,
                    values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_bmw_editor_activity.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_bmw_editor_activity, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //save product to the database.
                saveProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link BMWMainActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(BMWEditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(BMWEditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the pet table
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
                BMWEntry.COLUMN_BMW_CLIENT_EMAIL};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
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


            // Extract out the value from the Cursor for the given column index
            String brand = cursor.getString(brandNameColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            Integer price = cursor.getInt(priceColumnIndex);
            String model = cursor.getString(modelColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            Integer quantity = cursor.getInt(quantityColumnIndex);
            Integer fuel = cursor.getInt(fuelColumnIndex);
            Integer transmission = cursor.getInt(transmissionColumnIndex);

            // Update the views on the screen with the values from the database
            selectedImageUri = Uri.parse(image);
            mImage.setImageURI(selectedImageUri);
            mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            brandNameEdiText.setText(brand);
            mPriceEditText.setText(String.valueOf(price));
            mModelEditText.setText(model);
            mNameEditText.setText(name);
            mPhoneEditText.setText(phone);
            mEmailEditText.setText(email);
            mQuantityEdiText.setText(String.valueOf(quantity));


            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.

            switch (fuel) {
                case BMWEntry.FUEL_DIESEL:
                    mFuelSpinner.setSelection(1);
                    break;
                case BMWEntry.FUEL_HYBRID:
                    mFuelSpinner.setSelection(2);
                    break;
                default:
                    mFuelSpinner.setSelection(0);
                    break;
            }


            switch (transmission) {
                case BMWEntry.BMW_TRANSMISSION_MANUAL:
                    mTransmissionSpinner.setSelection(1);
                    break;
                case BMWEntry.BMW_TRANSMISSION_SEMI_AUTOMATIC:
                    mTransmissionSpinner.setSelection(2);
                    break;
                default:
                    mTransmissionSpinner.setSelection(0);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // If the loader is invalidated, clear out all the data from the input fields.
        mImage.setImageURI(null);
        mPriceEditText.setText("");
        mModelEditText.setText("");
        mNameEditText.setText("");
        mPhoneEditText.setText("");
        mEmailEditText.setText("");
        mQuantityEdiText.setText("");
        brandNameEdiText.setText("");
        mFuelSpinner.setSelection(BMWEntry.FUEL_PETROL);
        mTransmissionSpinner.setSelection(BMWEntry.BMW_TRANSMISSION_AUTOMATIC);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
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