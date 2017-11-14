package com.example.android.bmw_inventory_app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.bmw_inventory_app.data.BMWContract.BMWEntry;
/**
 * {@link BMWCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of the product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class BMWCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link BMWCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BMWCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // Inflate a list item view using the layout specified in list_item.xml

        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView brandTextView =  view.findViewById(R.id.brandView);
        TextView modelTextView =  view.findViewById(R.id.modelView);
        TextView priceTextView =  view.findViewById(R.id.priceView);
        TextView quantityTextView = view.findViewById(R.id.quantityView);


        // Find the columns of product attributes that we're interested in
        int brandColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_BRAND_NAME);
        int modelColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_MODEL);
        int priceColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BMWEntry.COLUMN_BMW_QUANTITY);


        // Read the product attributes from the Cursor for the current product.
        String productBrand = cursor.getString(brandColumnIndex);
        String productModel = cursor.getString(modelColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        String productQuantity = cursor.getString(quantityColumnIndex);

        Button salesButton = view.findViewById(R.id.purchase_button);
        // Read the product attributes from the Cursor for the current product

        final int myId =cursor.getInt(cursor.getColumnIndex(BMWEntry.BMW_ID));
        final int myQuantity = Integer.parseInt(productQuantity);
        salesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int allQuantity = myQuantity;
                if (allQuantity > 0) {
                    allQuantity = allQuantity - 1;
                }

                ContentValues values = new ContentValues();
                Uri updateUri = ContentUris.withAppendedId(BMWEntry.CONTENT_URI, myId);
                values.put(BMWEntry.COLUMN_BMW_QUANTITY, allQuantity);
                context.getContentResolver().update(updateUri, values,null, null);
            }
        });


        // If the product model is empty string or null, then use some default text
        // that says "Unknown model", so the TextView isn't blank.
        if (TextUtils.isEmpty(productModel)) {
            productModel = context.getString(R.string.unknown_model);
        }

        // Update the TextViews with the attributes for the current product
        brandTextView.setText(productBrand);
        modelTextView.setText(productModel);
        priceTextView.setText(productPrice);
        quantityTextView.setText(productQuantity);
    }
}