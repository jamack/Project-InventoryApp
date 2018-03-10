package com.example.android.project_inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.project_inventoryapp.R;
import com.example.android.project_inventoryapp.data.ProductContract.ProductEntry;

/**
 * {@link ContentProvider} for Inventory app.
 */
public class ProductProvider extends ContentProvider{

    /** Tag for log messages */
    private static final String LOG_TAG = ProductProvider.class.getSimpleName();

    /**
     * Database helper class reference
     */
    private ProductDbHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the products table
     */
    private static final int PRODUCTS = 100;

    /**
     * URI matcher code for the content URI for a single product in the products table
     */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. Run the first time anything is called from this class.
    static {
        // The content URI of the form "content://com.example.android.project_inventoryapp/products" will map to the
        // integer code {@link #PRODUCTS}. Access to MULTIPLE rows of the products table.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductEntry.TABLE_NAME, PRODUCTS);

        // The content URI of the form "content://com.example.android.PRODUCTS/PRODUCTS/#" will map to the
        // integer code {@link #PRODUCT_ID}. Access to ONE single row of the PRODUCTS table.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductEntry.TABLE_NAME + "/#", PRODUCT_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        // Create and initialize a ProductDbHelper object to gain access to the products database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, 
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.v(LOG_TAG,"Entering query() method; passed URI is: " + uri.toString());
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the PRODUCTS code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI
                // and query that row of the products table.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the cursor.
        // If the data at this URI changes, cursor needs to be updated
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
        // TODO: MAKE SURE I'VE NULLIFIED THE CURSOR AT ITS ENDPOINT(S), TO RELEASE RESOURCES
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        // Check that passed URI is valid, i.e. it matches that for the entire Products table.
        // If so, call the helper method for insert operaton.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // URI is valid; call helper method to perform insertion
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Helper method to insert a product into the database with the given content values.
     * Return the new content URI for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {

        // Validate ContentValues data via helper method.
        // If invalid data, return early.
        if (validateData(values) == false) {
            return null;
        }

        // Get a writable instance of the products database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Call the insert method on the database.
        // Method returns a long containing the row number for the new entry.
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            // TODO: SHOULD TOAST BE CALLED HERE OR AT THE POINT OF ORIGIN?
            Toast.makeText(getContext(), R.string.message_error_failed_to_add_product, Toast.LENGTH_SHORT).show();
            return null;
        }

        // Notify all listeners that data has changed for this URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Helper method to update products in the database with the given content values. Apply the changes
     * to the rows specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(ContentValues values, String selection, String[] selectionArgs) {

        // Validate ContentValues data via helper method.
        // If invalid data, return early.
        // TODO: CURRENTLY, validateData THROWS AN EXCEPTION INSTEAD OF RETURNING 'FALSE'...
        if (validateData(values) == false) {
            return 0;
        }

        // Get writable database instance
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int numRowsUpdates = database.update(ProductEntry.TABLE_NAME,values,selection,selectionArgs);

        // If any rows have been updated,
        // notify all listeners that data has changed for this URI
        if (numRowsUpdates != 0) {
            getContext().getContentResolver().notifyChange(ProductEntry.CONTENT_URI, null);
        }

        // Return the number of rows that were affected
        return numRowsUpdates;
    }

    /**
     * Validate data to be inserted / updated in {@link ProductEntry}.
     * Return true if valid; throw exception if invalid data is present.
     */
    private boolean validateData(ContentValues values){

        // Check that passed ContentValues is not null or empty. Throw exception if it is.
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Insert/Update cannot be performed with a null or empty ContentValues object");
        }

        // Check that the name is not null
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            // TODO: MAKE THESE TOAST MESSAGES INSTEAD, RETURN 'FALSE', AND ADDRESS IT AS NEEDED IN CALLING METHOD?
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check that quantity is not null or negative
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED);
        if (quantity == null) {
            throw new IllegalArgumentException("Product requires a stocked quantity");
        } else if (quantity < 0) {
            throw new IllegalArgumentException("Stocked product quantity cannot be less than zero");
        }

        // Check that price is not null or negative
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Product requires a price");
        } else if (price < 0) {
            throw new IllegalArgumentException("Product price cannot be less than zero");
        }

        return true;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Number of row(s) deleted.
        int deletedRows;

        // Check passed URI against UriMatcher & act accordingly
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                deletedRows = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                deletedRows = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If rows have been deleted,
        // notify all listeners that data has changed for this URI
        if (deletedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedRows;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
