package com.example.android.project_inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.project_inventoryapp.data.ProductContract;
import com.example.android.project_inventoryapp.data.ProductDbHelper;

/**
 * {@link ProductsCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductsCursorAdapter extends CursorAdapter {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = ProductsCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link ProductsCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductsCursorAdapter(Context context, Cursor c) {
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

        // Inflate the list item view
        View listItem = LayoutInflater.from(context).inflate(R.layout.item_stockroom_listview, parent, false);

        // Create a new ViewHolder object to cache children views
        ViewHolder holder = new ViewHolder();
        holder.name = listItem.findViewById(R.id.listview_item_name);
        holder.quantity = listItem.findViewById(R.id.listview_item_quantity);
        holder.price = listItem.findViewById(R.id.listview_item_price);
        holder.saleButton = listItem.findViewById(R.id.listview_item_sale_button);

        // Attach the ViewHolder object with cached views
        listItem.setTag(holder);

        // Return the list item view
        return listItem;
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Retrieve tagged ViewHolder object and its cached views
        ViewHolder holder = (ViewHolder) view.getTag();
        TextView tvName = holder.name;
        TextView tvQuantity = holder.quantity;
        TextView tvPrice = holder.price;
        Button btnSale = holder.saleButton;

        // Get data from cursor
        final String cName = cursor.getString(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME));
        final Integer cQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED));
        final Integer cPrice = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE));

        // Get _id from the cursor. (Will be used if user clicks this item's 'Sale' button).
        final String cId = cursor.getString(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry._ID));

        // Set data on the textviews
        tvName.setText(cName);
        tvQuantity.setText(Integer.toString(cQuantity));
        tvPrice.setText("$" + ProductDbHelper.priceDbToString(cPrice));

        // Set up the 'sale' button
        btnSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(LOG_TAG, "ListView item button clicked; entering onClick method");

                // Check that quantity will not drop below zero before executing sale logic
                if (cQuantity > 0) {

                    // Create ContentValues object
                    ContentValues values = new ContentValues();

                    // Reduce stocked quantity by one and add updated quantity to ContentValues object.
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED, cQuantity - 1);

                    // Add other current product data back to ContentValues object.
                    // (Required by ProductProvider's data validation logic).
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, cName);
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, cPrice);
                    Log.v(LOG_TAG, "In bindView method; value of readied ContentValues: " + values.toString());

                    // Use the ID to create URI for item's database entry
                    Uri uri = Uri.withAppendedPath(ProductContract.ProductEntry.CONTENT_URI, cId);
                    Log.v(LOG_TAG, "In bindView method; updating URI of: " + uri);

                    // Perform database update operation, passing in ContentValues object
                    int rowsUpdated = context.getContentResolver().update(uri, values, null, null);

                    // Check whether database operation was successful. Warn user if operation failed.
                    if (rowsUpdated == 0) {
                        Toast.makeText(context, "Failed to update product", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.v(LOG_TAG, "In bindView method's listener setup; successfully updated " + Integer.toString(rowsUpdated) + " rows.");
                        Log.v(LOG_TAG, "In bindView method; after database update, value of cQuantityInt is: " + Integer.toString(cQuantity - 1));
                    }
                } else { // Quantity is already zero
                    // TODO: CREATE ALERTDIALOG TO ALERT USER THAT PRODUCT IS OUT OF STOCK, ASK TO ORDER MORE, & ORDER MORE?
                    Toast.makeText(context, "Product is already sold out", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * /** Cache of the children views for a list item.
     * Store inside the item views w/ Tag.
     */
    static class ViewHolder {
        TextView name;
        TextView quantity;
        TextView price;
        Button saleButton;
    }
}
