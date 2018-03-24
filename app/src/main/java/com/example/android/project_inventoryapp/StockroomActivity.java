package com.example.android.project_inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.project_inventoryapp.data.ProductContract;
import com.example.android.project_inventoryapp.data.ProductContract.ProductEntry;
import com.example.android.project_inventoryapp.data.ProductDbHelper;

public class StockroomActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Tag for log messages */
    private static final String LOG_TAG = StockroomActivity.class.getSimpleName();

    // Identifier for products cursorloader
    private static final int PRODUCTS_LOADER = 0;

    // Global reference to list view
    ListView mProductsListView;

    // Global reference to instance of PetCursorAdapter
    ProductsCursorAdapter mCursorAdapter;

    // Reference to instance of database operations helper class
    private ProductDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stockroom);

        // Get reference to ListView
        mProductsListView = findViewById(R.id.stockroom_listview);

        // Get reference to the empty view
        View emptyView = findViewById(R.id.stockroom_empty_view);
        // Set empty view on the ListView
        mProductsListView.setEmptyView(emptyView);

        // Get reference to a new ProductsCursorAdapter. (Pass it a null cursor.
        // Cursor will be swapped once CursorLoader returns cursor from database).
        mCursorAdapter = new ProductsCursorAdapter(this, null);

        // Set adapter on the ListView
        mProductsListView.setAdapter(mCursorAdapter);

        // Create a listener for when an item in the list view is selected
        mProductsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.v(LOG_TAG,"In onCreate method; defining onItemClick method for new OnItemClickListener");
                //Create an intent
                Intent intent = new Intent(StockroomActivity.this,EditorActivity.class);

                // Construct a URI for a single database row, using the clicked item's ID
                Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI,id);

                // Attach the URI to the intent
                intent.setData(uri);

                // Start the new activity intent
                startActivity(intent);
            }
        });

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.stockroom_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StockroomActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PRODUCTS_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Projection with table columns to return
        String[] projection = new String[] {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED,
                ProductEntry.COLUMN_PRODUCT_PRICE
        };

        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
        Log.v(LOG_TAG,"In onLoadFinished method; cursor returned " + cursor.getCount() + " rows");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nullify the old cursor to prevent memory leaks
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the designated resources/menus xml file
        getMenuInflater().inflate(R.menu.menu_stockroom, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Match user's menu item choice with one of the below cases
        switch (item.getItemId()) {
            // User has selected the "insert sample data" menu option
            case R.id.action_insert_sample_data:
                insertSampleData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_products:
                // Check whether any there are currently any products in the database
                if (mCursorAdapter.getCount() != 0) {
                    // Display alert dialog for user to proceed with deletion or cancel the deletion
                    showDeleteConfirmationDialog();
                } else {
                    Toast.makeText(this,"No products to delete",Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you wish to delete all product entries?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all product entries.
                getContentResolver().delete(ProductEntry.CONTENT_URI,null,null);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
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
     * Test method to insert sample data to database.
     * Includes entries both with and without associated images.
     */
    private void insertSampleData() {

        // Format URI for the database table path
        Uri insertUri = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductEntry.TABLE_NAME);

        // Create new ContentValues object
        ContentValues contentValues = new ContentValues();

        // Create bitmap variables for formatting images for database
        Bitmap bitmap = null;
        byte[] bitmapByteArray = null;

        // Add values for first product to ContentValues object
        contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, "Acme WhamHammer Stapler");
        contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED, 3);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_PRICE,1495);
        bitmap = ProductDbHelper.sizeImageForDb(
                BitmapFactory.decodeResource(this.getResources(), R.drawable.red_stapler));
        bitmapByteArray = ProductDbHelper.getBytes(bitmap);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_IMAGE,bitmapByteArray);

        // Insert product into database
        getContentResolver().insert(insertUri,contentValues);

        // Clear ContentValues object and add values for an additional product
        contentValues.clear();
        contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, "Bob's Best Wackadoodle");
        contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED, 20);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_PRICE,99);

        // Insert product into database
        getContentResolver().insert(insertUri,contentValues);

        // Clear ContentValues object and add values for an additional product
        contentValues.clear();
        contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, "Smiles-n-Stuff YummyCone");
        contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED, 1000);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_PRICE,300);
        bitmap = ProductDbHelper.sizeImageForDb(
                BitmapFactory.decodeResource(this.getResources(), R.drawable.yummycone));
        bitmapByteArray = ProductDbHelper.getBytes(bitmap);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_IMAGE,bitmapByteArray);

        // Insert product into database
        getContentResolver().insert(insertUri,contentValues);
    }
}