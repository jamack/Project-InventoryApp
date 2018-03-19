package com.example.android.project_inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.project_inventoryapp.data.ProductContract.ProductEntry;
import com.example.android.project_inventoryapp.data.ProductDbHelper;

// TODO: ADD INTENT TO SEND PRODUCT ORDER INFO TO EMAIL APP
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
     * Global variable for the Floating Action Button
     */
    private FloatingActionButton mFloatingActionButton;

    /**
     * Constant for Edit Product CursorLoader
     */
    private static final int EDIT_PRODUCT_LOADER = 0;

    /**
     * Global boolean for whether user has entered/edited any product info
     */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener to detect any changes that user makes to product info.
     * Sets mProductHasChanged to 'true' if any changes are detected.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    /**
     * Constants for ContentValues keys
     */
    private static final String KEY_IMAGE = "IMAGE";
    private static final String KEY_NAME = "NAME";
    private static final String KEY_PRICE = "PRICE";
    private static final String KEY_QUANTITY = "QUANTITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG,"Entering onCreate method");
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
        mFloatingActionButton = findViewById(R.id.editor_fab);

        // Set text for currency symbol, to allow for localization.
        TextView currencySymbol = findViewById(R.id.editor_price_currency_symbol);
        currencySymbol.setText("$");

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
            mPriceEditText.setHint("Enter product price (eg: 14.95)");
            mQuantityStockedEditText.setHint("Enter current quantity (eg: 20");

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

        // Add ontouchlisteners to detect when user enters/revises any product data and set mProductHasChanged to 'true'.
        // Buttons will also update mProductHasChanged, via the onclicklisteners added below.
        // Set a listener on each view, to detect any user changes
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityStockedEditText.setOnTouchListener(mTouchListener);
        // Check whether activity opened in 'Add Product' or 'Edit Product' mode.
        // If in 'Edit Product' mode, order quantity field is visible; add listener.
        mOrderQuantityEditText.setOnTouchListener(mTouchListener);

        // Add onclicklistener to 'increase stock quantity' button
        mIncreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update mProductHasChanged boolean
                mProductHasChanged = true;

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
                // Update mProductHasChanged boolean
                mProductHasChanged = true;

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

        // If URI saved in onCreate() is not null, then EditorActivity has been initiated by selection of a product
        // in the StockroomActivity. Delete product button is only displayed in 'Edit Product' mode.
        if (mPassedUri != null) {
            // Add onclicklistener for 'delete product record' button
            mDeleteProductButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Create a click listener to handle the user confirming that changes should be discarded.
                    // This click listener will be passed to an alert dialog for user to confirm deletion of product record.
                    DialogInterface.OnClickListener confirmDeletionButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked alert dialog button confirming the deletion - delete product record,
                                    // return to Stockroom Activity, and display toast message confirming deletion.
                                    int rowsDeleted = getContentResolver().delete(mPassedUri,null,null);
                                    if (rowsDeleted == 0) {
                                        Toast.makeText(getApplicationContext(),"Failed to delete product record",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(),"Product record deleted",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            };

                    // Show dialog to confirm order
                    showDeletionConfirmationDialog(confirmDeletionButtonClickListener);
                }
            });
        }

        // Add onclicklistener for floating action button (fab)
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call helper method to collect user input and store in a ContentValues object
                ContentValues values = collectInput();

                // If URI passed to EditorActivity with starting Intent is null, it's in 'Add Product' mode
                if (mPassedUri == null) {
                    // Insert ContentValues object with product data into the database
                    Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI,values);
                    // Check returned URI to see whether database insertion was successful
                    if (uri == null) {
                        // Warn user that database insertion failed
                        Toast.makeText(getApplicationContext(),"Failed to save new product",Toast.LENGTH_SHORT).show();
                    } else {
                        // Notify user that database insertion was successful
                        Toast.makeText(getApplicationContext(),"New product saved",Toast.LENGTH_SHORT).show();
                        // Return to StockroomActivity
                        finish();
                    }
                } else { // If URI passed to EditorActivity with starting Intent is not null, it's in 'Edit Product' mode
                    // Update existing database entry - via passed URI - with revised product data in ContentValues object
                    int rowsUpdated = getContentResolver().update(mPassedUri,values,null,null);
                    // Check returned integer to see whether database entry update was successful
                    if (rowsUpdated == 0) {
                        // Warn user that database update failed
                        Toast.makeText(getApplicationContext(),"Failed to update product",Toast.LENGTH_SHORT).show();
                    } else {
                        // Notify user that database update was successful
                        Toast.makeText(getApplicationContext(),"Product entry updated",Toast.LENGTH_SHORT).show();
                        // Return to StockroomActivity
                        finish();
                    }
                }
            }
        });
    }

    @Nullable
    private ContentValues collectInput() {
        Log.v(LOG_TAG,"Entering collectInput method");
        // Check whether user has added/edited product info before proceeding.
        // If not, notify user and return early.
        if (mProductHasChanged == false) {
            if (mPassedUri == null) {
                Toast.makeText(this,"No product info to save",Toast.LENGTH_SHORT).show();
                return null;
            } else {
                Toast.makeText(this,"Make desired edit(s) before saving",Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        // Create new ContentValues object to hold user input
        ContentValues values = new ContentValues();

        // Retrieve and validate product name.
        // Name is required - cannot be null or empty.
        String name = mNameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this,"Product name required",Toast.LENGTH_SHORT).show();
            return null;
        } else {
            // If valid name, add to ContentValues
            values.put(ProductEntry.COLUMN_PRODUCT_NAME,name);
        }

        // Retrieve product price. Validate/format product price, using helper method from database helper class.
        // Price is required - cannot be null or empty, and must parse into a usable value
        String priceString = mPriceEditText.getText().toString();
        int priceInt;
        if(TextUtils.isEmpty(priceString)) {
            Toast.makeText(this,"Product price required",Toast.LENGTH_SHORT).show();
            return null;
        } else {
            priceInt = ProductDbHelper.priceStringToDb(priceString);
            // Check whether helper method has properly parsed the user's input
            if (priceInt == ProductDbHelper.PRICE_PARSE_FAILURE) {
                Toast.makeText(this,"Valid price required. (e.g. 14.95)",Toast.LENGTH_SHORT).show();
                return null;
            } else {
                values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceInt);
            }
        }

        // Retrieve and validate product stocked quantity.
        // Quantity must parse into a usable value and must be zero or greater.
        String quantityString = mQuantityStockedEditText.getText().toString();
        int quantityInt = 0;
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this,"Stocked quantity required",Toast.LENGTH_SHORT).show();
            return null;
        } else {
            // TODO: COULD POSSIBLY EXTRACT BELOW VALIDATION INTO A SINGLE METHOD TO BE CALLED FROM HERE & IN QUANTITY BUTTON LISTENERS
            // Confirm whether user input can be parsed into an integer
            try {
                quantityInt = Integer.parseInt(quantityString);
            } catch (NumberFormatException e) {
                // Notify user that their input is invalid
                Log.e(LOG_TAG, "Invalid quantity input; cannot parse to integer");
                Toast.makeText(getApplicationContext(), "Invalid quantity input - please try again", Toast.LENGTH_SHORT).show();
            }

            // If valid quantity, add to ContentValues
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED,quantityInt);
        }

        return values;
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
     * Show a dialog that asks the user to confirm whether they want to order more of the product.
     *
     * @param confirmButtonClickListener is the click listener for logic to execute if the user confirms
     *                                   they want to order more of the product
     */
    private void showOrderConfirmationDialog(DialogInterface.OnClickListener confirmButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to place this order?");
        builder.setPositiveButton("Place order", confirmButtonClickListener);
        builder.setNegativeButton("Not right now", new DialogInterface.OnClickListener() {
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

    /**
     * Show a dialog that asks the user to confirm whether they truly want to delete the product record.
     *
     * @param deletionButtonClickListener is the click listener for logic to execute if the user confirms
     *                                    they want to delete the product record
     */
    private void showDeletionConfirmationDialog(DialogInterface.OnClickListener deletionButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this product record?");
        builder.setPositiveButton("Delete record", deletionButtonClickListener);
        builder.setNegativeButton("Retain record", new DialogInterface.OnClickListener() {
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
        mDeleteProductButton.setOnClickListener(null);
        mFloatingActionButton.setOnClickListener(null);
        mNameEditText.setOnTouchListener(null);
        mPriceEditText.setOnTouchListener(null);
        mQuantityStockedEditText.setOnTouchListener(null);
        mOrderQuantityEditText.setOnTouchListener(null);
    }
}
