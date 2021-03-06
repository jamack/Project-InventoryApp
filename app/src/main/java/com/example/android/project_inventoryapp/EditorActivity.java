package com.example.android.project_inventoryapp;

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
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.project_inventoryapp.data.ProductContract.ProductEntry;
import com.example.android.project_inventoryapp.data.ProductDbHelper;

import java.io.IOException;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * Constant for add image button - choose existing image intent response code
     */
    private static final int INTENT_CHOOSE_IMAGE = 0;

    /**
     * Constant for Edit Product CursorLoader
     */
    private static final int EDIT_PRODUCT_LOADER = 0;

    /**
     * URI for specific pet entry, IF editing existing pet
     */
    private Uri mPassedUri;

    /**
     * Global variable for button to add product image.
     */
    private Button mAddImageButton;

    /**
     * Global variable for product image bitmap. Initially null.
     */
    private Bitmap mImageBitmap = null;

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
     * Initial setup - inflate layout, store references to views,
     * tailor visible items to 'Add Product' or 'Edit Product' mode,
     * and - if editing existing product - initiate loader for database cursor.
     *
     * @param savedInstanceState prior saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editor);

        // Store references to layout views
        mAddImageButton = findViewById(R.id.editor_button_add_image);
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
        currencySymbol.setText(R.string.symbol_dollar_sign);

        // Get URI passed with calling Intent. (URI could be null).
        mPassedUri = getIntent().getData();

        // If URI is not null, then EditorActivity has been initiated by selection of a product in the StockroomActivity
        if (mPassedUri != null) {
            // Change Activity title to indicate that user is editing details of an existing product
            // rather than adding a new product.
            setTitle(getString(R.string.activity_title_edit_product));

            // Initialize/reuse CursorLoader to retrieve current data for existing product to be edited
            getLoaderManager().initLoader(EDIT_PRODUCT_LOADER, null, this);
        } else { // if URI is null, then EditorActivity has been initiated by StockroomActivity 'add product' fab
            setTitle(getString(R.string.activity_title_add_product));

            // Make 'add product image' button visible
            mAddImageButton.setVisibility(View.VISIBLE);

            // Add TextEdit hints for a new product
            mNameEditText.setHint(R.string.hint_enter_name);
            mPriceEditText.setHint(R.string.hint_enter_price);
            mQuantityStockedEditText.setHint(R.string.hint_enter_quantity_in_stock);

            // Adding a new product, so hide UI elements related to existing products.
            mDecreaseQuantityButton.setVisibility(View.GONE);
            mIncreaseQuantityButton.setVisibility(View.GONE);
            mOrderQuantityEditText.setVisibility(View.GONE);
            findViewById(R.id.editor_order_colon).setVisibility(View.GONE);
            mOrderButton.setVisibility(View.GONE);
            findViewById(R.id.editor_button_delete_product_record).setVisibility(View.GONE);
        }
    }

    /**
     * Add {@link android.view.View.OnTouchListener} & {@link android.view.View.OnClickListener} listeners
     * as required for either 'Add Product' or 'Edit Product' mode.
     * <p>
     * Assigned either first time Activity is started or upon returning to the activity after they've been released.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Add ontouchlisteners to detect when user enters/revises any product data and set mProductHasChanged to 'true'.
        // Buttons will also update mProductHasChanged, via the onclicklisteners added below.
        // Set a listener on each view, to detect any user changes
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityStockedEditText.setOnTouchListener(mTouchListener);
        // Check whether activity opened in 'Add Product' or 'Edit Product' mode.
        // If in 'Edit Product' mode, order quantity field is visible; add listener.
        mOrderQuantityEditText.setOnTouchListener(mTouchListener);

        // If URI saved in onCreate() is null, then EditorActivity has been initiated by Floating Action Button
        // in the StockroomActivity. Add product image button is only displayed in 'Add Product' mode.
        if (mPassedUri == null) {

            // Add onclicklistener to 'add product image' button
            mAddImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent pickImage = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    // Check that Intent can be resolved and start Intent
                    PackageManager packageManager = getApplicationContext().getPackageManager();
                    if (pickImage.resolveActivity(packageManager) != null) {
                        startActivityForResult(pickImage, INTENT_CHOOSE_IMAGE);
                    }
                }
            });
        }

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
                        Toast.makeText(getApplicationContext(), R.string.message_error_invalid_entry_quantity, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), R.string.message_error_invalid_entry_quantity, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getApplicationContext(), R.string.message_error_negative_quantity,
                                        Toast.LENGTH_SHORT).show();
                            } else if (quantityInt == 0) {
                                Toast.makeText(getApplicationContext(), R.string.message_notice_choose_quantity,
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
                                                // User clicked alert dialog button confirming the order - Open email-capable app,
                                                // pre-populated with relevant info to order more of the displayed product.

                                                // Create new email Intent
                                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                intent.setData(Uri.parse("mailto:"));

                                                // Retrieve product name
                                                String name = mNameEditText.getText().toString();
                                                // Check that product name has not been cleared before adding to Intent's subject line.
                                                if (!TextUtils.isEmpty(name)) {
                                                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_order_subject_line) + name);
                                                }

                                                // Add body text to email Intent
                                                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_order_body_1of3) +
                                                        displayOrderQuantity + getString(R.string.email_order_body_2of3) + name +
                                                        getString(R.string.email_order_body_3of3));

                                                //Check that Intent can be resolved and start Intent
                                                PackageManager packageManager = getApplicationContext().getPackageManager();
                                                if (intent.resolveActivity(packageManager) != null) {
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(getApplicationContext(), R.string.message_error_no_app_for_email,
                                                            Toast.LENGTH_SHORT).show();
                                                }

                                                // Reset order quantity to zero
                                                mOrderQuantityEditText.setText(R.string.text_zero);
                                            }
                                        };

                                // Show dialog to confirm order
                                showOrderConfirmationDialog(confirmOrderButtonClickListener);
                            }

                        } catch (NumberFormatException e) {
                            // Notify user that their input is invalid
                            Log.e(LOG_TAG, "Invalid quantity input; cannot parse to integer");
                            Toast.makeText(getApplicationContext(), R.string.message_error_invalid_entry_quantity, Toast.LENGTH_SHORT).show();
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
                                    int rowsDeleted = getContentResolver().delete(mPassedUri, null, null);
                                    if (rowsDeleted == 0) {
                                        Toast.makeText(getApplicationContext(), R.string.message_error_failed_to_delete_product,
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.message_product_deleted,
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

                // Check that we have received a non-null ContentValues object from helper method
                if (values == null) { // collectInput helper method unable to process data and returned a null ContentValues
                    // Return without performing database operations on the null ContentValues object
                    return;
                }

                // If URI passed to EditorActivity with starting Intent is null, it's in 'Add Product' mode
                if (mPassedUri == null) {

                    // Insert ContentValues object with product data into the database
                    Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
                    // Check returned URI to see whether database insertion was successful
                    if (uri == null) {
                        // Warn user that database insertion failed
                        Toast.makeText(getApplicationContext(), R.string.message_error_failed_to_add_product, Toast.LENGTH_SHORT).show();
                    } else {
                        // Notify user that database insertion was successful
                        Toast.makeText(getApplicationContext(), R.string.message_product_added, Toast.LENGTH_SHORT).show();
                        // Return to StockroomActivity
                        finish();
                    }
                } else { // If URI passed to EditorActivity with starting Intent is not null, it's in 'Edit Product' mode
                    // Update existing database entry - via passed URI - with revised product data in ContentValues object
                    int rowsUpdated = getContentResolver().update(mPassedUri, values, null, null);
                    // Check returned integer to see whether database entry update was successful
                    if (rowsUpdated == 0) {
                        // Warn user that database update failed
                        Toast.makeText(getApplicationContext(), R.string.message_error_failed_to_update_product, Toast.LENGTH_SHORT).show();
                    } else {
                        // Notify user that database update was successful
                        Toast.makeText(getApplicationContext(), R.string.message_product_updated, Toast.LENGTH_SHORT).show();
                        // Return to StockroomActivity
                        finish();
                    }
                }
            }
        });
    }

    /**
     * Helper method to validate and collect the user's input
     *
     * @return {@link ContentValues} object with input paired to database's column id's.
     * Returns null if any data has failed validation.
     */
    @Nullable
    private ContentValues collectInput() {

        // Check whether user has added/edited product info before proceeding.
        // If not, notify user and return early.
        if (mProductHasChanged == false) {
            if (mPassedUri == null) {
                Toast.makeText(this, R.string.message_notice_no_product_data_to_save, Toast.LENGTH_SHORT).show();
                return null;
            } else {
                Toast.makeText(this, R.string.message_notice_no_product_edits_to_save, Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        // Create new ContentValues object to hold user input
        ContentValues values = new ContentValues();

        // Check whether an image has been set for the product.
        // Product image is required.
        if (mImageBitmap == null) {
            Toast.makeText(getApplicationContext(), R.string.message_notice_image_required,Toast.LENGTH_SHORT).show();
            return null;
        } else {
            // Convert image bitmap into byte array that can be saved into database's blob data type,
            // using helper method in database helper class.
            byte[] imageByteArray = ProductDbHelper.getBytes(mImageBitmap);

            // Add converted image to ContentValues
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageByteArray);
        }

        // Retrieve and validate product name.
        // Name is required - cannot be null or empty.
        String name = mNameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.message_notice_name_required, Toast.LENGTH_SHORT).show();
            return null;
        } else {
            // If valid name, add to ContentValues
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
        }

        // Retrieve product price. Validate/format product price, using helper method from database helper class.
        // Price is required - cannot be null or empty, and must parse into a usable value
        String priceString = mPriceEditText.getText().toString();
        int priceInt;
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.message_notice_price_required, Toast.LENGTH_SHORT).show();
            return null;
        } else {
            priceInt = ProductDbHelper.priceStringToDb(priceString);
            // Check whether helper method has properly parsed the user's input
            if (priceInt == ProductDbHelper.PRICE_PARSE_FAILURE) {
                Toast.makeText(this, R.string.message_notice_valid_price_required, Toast.LENGTH_SHORT).show();
                return null;
            } else {
                values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceInt);
            }
        }

        // Retrieve and validate product stocked quantity.
        // Quantity must parse into a usable value and must be zero or greater.
        String quantityString = mQuantityStockedEditText.getText().toString();
        int quantityInt = 0;
        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.message_notice_stocked_quantity_required, Toast.LENGTH_SHORT).show();
            return null;
        } else {
            // Confirm whether user input can be parsed into an integer
            try {
                quantityInt = Integer.parseInt(quantityString);
            } catch (NumberFormatException e) {
                // Notify user that their input is invalid
                Toast.makeText(getApplicationContext(), R.string.message_notice_valid_quantity_required, Toast.LENGTH_SHORT).show();
                return null;
            }

            // If valid quantity, add to ContentValues
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED, quantityInt);
        }

        return values;
    }

    /**
     * Creates a loader to perform database operations on a background thread.
     *
     * @param i      The ID whose loader is to be created.
     * @param bundle Any arguments supplied by the caller
     * @return Return a new Loader instance that is ready to start loading
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Return loader with URI of desired table (and all columns, by default)
        return new CursorLoader(this,
                mPassedUri,
                null,
                null,
                null,
                null);
    }

    /**
     * Called when loader has finished its load; these
     * operations are performed on the UI thread.
     *
     * @param loader that has finished
     * @param cursor the data generated by the loader
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // If cursor is empty, return early
        if (!(cursor.getCount() > 0)) {
            Log.e(LOG_TAG, "Failed to return cursor with data.");
            return;
        }

        // Move cursor to first row
        cursor.moveToFirst();

        // Retrieve values from the selected product's database entry
        byte[] cursorImage = cursor.getBlob(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_IMAGE));
        String cursorName = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        Integer cursorPriceInteger = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
        String cursorPriceString = null;

        // Check whether product has an associated image in the database.
        // (Value in database entry's image column is neither null nor empty).
        if (cursorImage != null && cursorImage.length != 0) {

            // Make product ImageView visible
            mImageView.setVisibility(View.VISIBLE);

            // Decode the cursor's byte array / blob back into an image
            mImageBitmap = ProductDbHelper.getImage(cursorImage);

            // Set the retrieved image on the product image view
            mImageView.setImageBitmap(mImageBitmap);
        }

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

    /**
     * Called when the previously created loader is reset and its data is no longer available.
     * Remove any reference to the previous loader's data.
     *
     * @param loader that is being reset
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Set all text back to being empty
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityStockedEditText.setText("");
        mOrderQuantityEditText.setText("");
    }

    /**
     * This hook is called whenever an item in the options menu is selected
     *
     * @param item that was selected
     * @return boolean - 'false' to allow normal menu processing to proceed, 'true' to consume it here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity (StockroomActivity).
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
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
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
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

        // If no changes have been made to the product, handle back button as normal
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

    /**
     * Called when an activity launched from this activity exits
     *
     * @param requestCode    integer originally supplied to startActivityForResult(), allowing identification of activity result came from
     * @param resultCode     integer returned by the child activity through its setResult() method
     * @param returnedIntent any result data from child activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        switch (requestCode) {
            case INTENT_CHOOSE_IMAGE:
                if (resultCode == RESULT_OK) {
                    // Retrieve URI for selected image
                    Uri selectedImage = returnedIntent.getData();

                    // Ensure the product image view is visible
                    mImageView.setVisibility(View.VISIBLE);

                    // Get image in bitmap format from the URI
                    try {
                        Bitmap rawBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        mImageBitmap = ProductDbHelper.sizeImageForDb(rawBitmap);
                    } catch (IOException e) {
                        // Notify user that image selection has failed
                        Toast.makeText(getApplicationContext(), R.string.message_error_failed_to_add_image, Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG, "Error processing camera photo into local bitmap", e);
                    }

                    // Set selected image on the image view
                    mImageView.setImageBitmap(mImageBitmap);

                    // Since image has been selected, turn the 'add image' button off
                    mAddImageButton.setVisibility(View.GONE);

                    // Update mProductHasChanged boolean
                    mProductHasChanged = true;
                }

                break;

            default:
                Toast.makeText(getApplicationContext(), R.string.message_error_failed_to_pick_image, Toast.LENGTH_SHORT).show();
                break;

        }
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
        builder.setMessage(R.string.dialog_prompt_confirm_order);
        builder.setPositiveButton(R.string.dialog_option_place_order, confirmButtonClickListener);
        builder.setNegativeButton(R.string.dialog_option_not_right_now, new DialogInterface.OnClickListener() {
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
        builder.setMessage(R.string.dialog_prompt_delete_product_record);
        builder.setPositiveButton(R.string.dialog_option_delete_record, deletionButtonClickListener);
        builder.setNegativeButton(R.string.dialog_option_retain_record, new DialogInterface.OnClickListener() {
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
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener click listener to notify user and handle response
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_prompt_discard_unsaved_changes);
        builder.setPositiveButton(R.string.dialog_option_discard_changes, discardButtonClickListener);
        builder.setNegativeButton(R.string.dialog_option_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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
     * Release system resources when activity is not visible to the user
     */
    @Override
    protected void onStop() {
        super.onStop();

        // Release resources
        mAddImageButton.setOnClickListener(null);
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