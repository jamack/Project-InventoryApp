package com.example.android.project_inventoryapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.project_inventoryapp.data.ProductContract.ProductEntry;
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
        final String cName = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME));
        final Integer cQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED));
        final Integer cPrice = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE));

        // Get _id from the cursor. (Will be used if user clicks this item's 'Sale' button).
        final String cId = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry._ID));

        // Set data on the textviews
        tvName.setText(cName);
        tvQuantity.setText(Integer.toString(cQuantity));
        tvPrice.setText(R.string.currency_symbol_dollar_sign );
        tvPrice.append(ProductDbHelper.priceDbToString(cPrice));

        // Set up the 'sale' button
        btnSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Check that quantity will not drop below zero before executing sale logic
                if (cQuantity > 0) {

                    // Create local variable to hold new value
                    int updatedQuantity = cQuantity - 1;

                    // Create ContentValues object
                    ContentValues values = new ContentValues();

                    // Reduce stocked quantity by one and add updated quantity to ContentValues object.
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED, updatedQuantity);

                    // Add other current product data back to ContentValues object.
                    // (Required by ProductProvider's data validation logic).
                    values.put(ProductEntry.COLUMN_PRODUCT_NAME, cName);
                    values.put(ProductEntry.COLUMN_PRODUCT_PRICE, cPrice);

                    // Use the ID to create URI for item's database entry
                    Uri uri = Uri.withAppendedPath(ProductEntry.CONTENT_URI, cId);

                    // Perform database update operation, passing in ContentValues object
                    int rowsUpdated = context.getContentResolver().update(uri, values, null, null);

                    // Check whether database operation was successful. Warn user if operation failed.
                    if (rowsUpdated == 0) {
                        Toast.makeText(context, R.string.message_error_failed_to_update_product, Toast.LENGTH_SHORT).show();
                        // Return early
                        return;
                    }

                    // TODO: CREATE ALERTDIALOG TO ALERT USER THAT PRODUCT IS OUT OF STOCK, ASK TO ORDER MORE, & ORDER MORE?
                    // If quantity reduced to zero, alert user and provide option to order more
                    if (updatedQuantity == 0) {
                        Toast.makeText(context, R.string.message_notice_product_sold_out, Toast.LENGTH_SHORT).show();
                    }
                } else { // Quantity is already zero
                    // TODO: CREATE ALERTDIALOG TO ALERT USER THAT PRODUCT IS OUT OF STOCK, ASK TO ORDER MORE, & ORDER MORE?
                    Toast.makeText(context, R.string.message_notice_product_sold_out, Toast.LENGTH_SHORT).show();
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

    // TODO: FINISH THIS ONCLICKLISTENER AND THE SHOWORDERCONFIRMATIONDIALOG METHOD BELOW
    // Create a click listener to handle the user choosing to order more product.
    DialogInterface.OnClickListener confirmOrderButtonClickListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // User clicked alert dialog button choosing to order more of the product -
                    // set up an Intent to open product in EditorActivity, with its order functionality.

//                    //Create an intent
//                    Intent intent = new Intent(mContext,EditorActivity.class);
//
//                    // Construct a URI for a single database row, using the clicked item's ID
//                    Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI,cId);
//
//                    // Attach the URI to the intent
//                    intent.setData(uri);
//
//                    // Start the new activity intent
//                    startActivity(intent);

                }
            };

    /**
     * Show a dialog that asks the user to confirm whether they'd like to order more of the product.
     *
     * @param confirmButtonClickListener is the click listener for logic to execute if the user confirms
     *                                   they want to order more of the product
     */
    private void showOrderConfirmationDialog(DialogInterface.OnClickListener confirmButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.message_notice_product_sold_out);
        builder.setPositiveButton(R.string.dialog_option_order_more, confirmButtonClickListener);
        builder.setNegativeButton(R.string.dialog_option_not_right_now, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "No" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
