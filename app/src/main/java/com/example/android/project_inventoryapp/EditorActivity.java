package com.example.android.project_inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.project_inventoryapp.data.ProductContract.ProductEntry;
import com.example.android.project_inventoryapp.data.ProductDbHelper;

// TODO: ADD ALERT DIALOG TO ORDER BUTTON
public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * URI for specific pet entry, IF editing existing pet
     */
    private Uri mPassedUri;

    /**
     * Global variable for product image ImageView
     */
    private ImageView mImageView;

    /**
     * Global variable for product name EditText
     */
    private EditText mNameEditText;

    /**
     * Global variable for product price TextView
     */
    private EditText mPriceEditText;

    /**
     * Global variable for stocked quantity TextView
     */
    private EditText mQuantityStockedEditText;

    /**
     * Global variable for increase quantity Button
     */
    private Button mIncreaseQuantityButton;

    /**
     * Global variable for decrease quantity Button
     */
    private Button mDecreaseQuantityButton;

    /**
     * Global variable for order quantity EditText
     */
    private EditText mOrderQuantityEditText;

    /**
     * Global variable for order Button
     */
    private Button mOrderButton;

    /**
     * Global variable for delete product record Button
     */
    private Button mDeleteProductButton;

    /**
     * Constant for Edit Product CursorLoader
     */
    private static final int EDIT_PRODUCT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Store references to layout views
        mImageView = findViewById(R.id.editor_image);
        mNameEditText = findViewById(R.id.editor_name);
        mPriceEditText = findViewById(R.id.editor_price);
        mQuantityStockedEditText = findViewById(R.id.editor_quantity_stocked);
        mIncreaseQuantityButton = findViewById(R.id.editor_quantity_increase);
        mDecreaseQuantityButton = findViewById(R.id.editor_quantity_decrease);
        mOrderQuantityEditText = findViewById(R.id.editor_quantity_to_order);
        mOrderButton = findViewById(R.id.editor_button_order);
        mDeleteProductButton = findViewById(R.id.editor_button_delete_product_record);


        // Get URI passed with calling Intent. (URI could be null).
        mPassedUri = getIntent().getData();

        // If URI is not null, then EditorActivity has been initiated by selection of a product in the StockroomActivity
        if (mPassedUri != null) {
            // Change Activity title to indicate that user is editing details of an existing product
            // rather than adding a new product.
            setTitle("Edit Product");

            // Initialize/reuse CursorLoader to retrieve current data for existing product to be edited
            getLoaderManager().initLoader(EDIT_PRODUCT_LOADER, null, this);
        } else { // if URI is null, then EditorActivity has been initiated by StockroomActivity 'add product' fab
            setTitle("Add Product");

            // Add TextEdit hints for a new product
            mNameEditText.setHint("Enter product name");
            mPriceEditText.setHint("Enter product price (eg: $14.95)");
            mQuantityStockedEditText.setHint("Enter current quantity");

            // Adding a new product, so hide UI elements related to existing products.
            findViewById(R.id.editor_quantity_to_order_container).setVisibility(View.GONE);
            findViewById(R.id.editor_button_delete_product_record).setVisibility(View.GONE);

//            // Invalidate the options menu, so the "Delete" menu option can be hidden.
//            // (It doesn't make sense to delete a pet that hasn't been created yet.)
//            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "Entering onResume method");

        // Add onclicklistener to 'increase stock quantity' button
        mIncreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get entry from current stocked quantity TextEdit. (May be empty!)
                String quantityString = mQuantityStockedEditText.getText().toString();

                // Check that value from stocked quantity TextEdit is not empty
                if (!TextUtils.isEmpty(quantityString)) {
                    // Try to convert user entry to an integer
                    try {
                        Integer quantityInt = Integer.parseInt(quantityString);

                        // Increase the displayed quantity
                        quantityInt++;
                        mQuantityStockedEditText.setText(quantityInt.toString());
                    } catch (NumberFormatException e) {
                        // Notify user that their input is invalid
                        Log.e(LOG_TAG, "Invalid quantity input; cannot parse to integer");
                        Toast.makeText(getApplicationContext(), "Invalid quantity input - please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Add onclicklistener to 'decrease stock quantity' button
        mDecreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get entry from current stocked quantity TextEdit. (May be empty!)
                String quantityString = mQuantityStockedEditText.getText().toString();

                // Check that value from stocked quantity TextEdit is not empty
                if (!TextUtils.isEmpty(quantityString)) {
                    // Try to convert user entry to an integer
                    try {
                        Integer quantityInt = Integer.parseInt(quantityString);

                        // Do not allow negative values
                        if (quantityInt > 0) {
                            // Decrease the displayed quantity
                            quantityInt--;
                            mQuantityStockedEditText.setText(quantityInt.toString());
                        }
                    } catch (NumberFormatException e) {
                        // Notify user that their input is invalid
                        Log.e(LOG_TAG, "Invalid quantity input; cannot parse to integer");
                        Toast.makeText(getApplicationContext(), "Invalid quantity input - please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // If URI saved in onCreate() is not null, then EditorActivity has been initiated by selection of a product
        // in the StockroomActivity. Quantity and order button are only displayed in 'Edit Product' mode.
        if (mPassedUri != null) {
            // Add onclicklistener for 'order' button
            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Get quantity that user has input for the order
                    String quantityString = mOrderQuantityEditText.getText().toString();

                    // Check that value from stocked quantity TextEdit is not empty
                    if (!TextUtils.isEmpty(quantityString)) {
                        // Try to convert user entry to an integer
                        try {
                            Integer quantityInt = Integer.parseInt(quantityString);

                            // Validate data and display toast message accordingly
                            if (quantityInt < 0) {
                                Toast.makeText(getApplicationContext(), "Cannot order a negative quantity",
                                        Toast.LENGTH_SHORT).show();
                            } else if (quantityInt == 0) {
                                Toast.makeText(getApplicationContext(), "Please choose quantity to order",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Create a final variable that can be referenced in the inner class for the dialog listener
                                final String displayOrderQuantity = quantityInt.toString();

                                // Input is valid - setup an alert dialog for user to confirm the order.
                                // Create a click listener to handle the user confirming that changes should be discarded.
                                DialogInterface.OnClickListener confirmOrderButtonClickListener =
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                // User clicked alert dialog button confirming the order -
                                                // display toast message confirming the order + quantity.
                                                // Reset order quantity EditText to zero.
                                                Toast.makeText(getApplicationContext(), "Ordering " + displayOrderQuantity +
                                                        " more", Toast.LENGTH_SHORT).show();
                                                mOrderQuantityEditText.setText("0");
                                            }
                                        };

                                // Show dialog to confirm order
                                showOrderConfirmationDialog(confirmOrderButtonClickListener);
                            }

                        } catch (NumberFormatException e) {
                            // Notify user that their input is invalid
                            Log.e(LOG_TAG, "Invalid quantity input; cannot parse to integer");
                            Toast.makeText(getApplicationContext(), "Invalid quantity input - please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                mPassedUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.v(LOG_TAG, "Entering onLoadFinished method");
        // If cursor is empty, return early
        if (!(cursor.getCount() > 0)) {
            Log.e(LOG_TAG, "Failed to return cursor with data.");
            return;
        }

        // Move cursor to first row
        cursor.moveToFirst();

        // Retrieve values from the selected product's database entry
        String cursorName = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        Integer cursorPriceInteger = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
        String cursorPriceString = null;
        // Check that price is not null
        if (cursorPriceInteger != null) {
            // Call helper method in database helper class to format price as a string and overwrite the stored null value
            cursorPriceString = ProductDbHelper.priceDbToString(cursorPriceInteger);
        } else {
            Log.e(LOG_TAG, "Failed to retrieve valid price from cursor.");
        }

        Integer cursorQuantityStocked = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED));

        // Set the retrieved values on the UI fields as a starting point for editing the pet
        mNameEditText.setText(cursorName);
        mPriceEditText.setText(cursorPriceString);

        // Check that quantity stocked is not null
        if (cursorQuantityStocked != null) {
            mQuantityStockedEditText.setText(Integer.toString(cursorQuantityStocked));
        } else {
            Log.e(LOG_TAG, "Failed to retrieve valid stocked quantity from cursor.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Set all text back to being empty
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityStockedEditText.setText("");
        mOrderQuantityEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param confirmButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to order more of the product
     */
    private void showOrderConfirmationDialog(DialogInterface.OnClickListener confirmButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to place this order?");
        builder.setPositiveButton("Yes - place order", confirmButtonClickListener);
        builder.setNegativeButton("No - not right now", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "No" button, so dismiss the dialog
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

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "Entering onStop method");

        // Release resources
        mIncreaseQuantityButton.setOnClickListener(null);
        mDecreaseQuantityButton.setOnClickListener(null);
        mOrderButton.setOnClickListener(null);
    }
}
