package com.example.android.project_inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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



        // ** TESTING DATABASE HELPER METHOD **
//        ProductDbHelper dbHelper = new ProductDbHelper(this);
//        SQLiteDatabase database = dbHelper.getReadableDatabase();
//
//        // ** TESTING CONTENTPROVIDER INSERT METHOD **
//        Uri insertUri = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME);
//        Log.v(LOG_TAG,"In onCreate method; generated URI is: " + insertUri.toString());
//        Log.v(LOG_TAG,"In onCreate method; UriMatcher URI for entire products table is: " +
//                ProductContract.CONTENT_AUTHORITY + ProductContract.ProductEntry.TABLE_NAME);
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, "Acme WhamHammer Stapler");
//        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED, 1);
//        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,1991);
//
//        getContentResolver().insert(insertUri,contentValues);
//
//        // insert additional pet(s)
//        getContentResolver().insert(insertUri,contentValues);
//        getContentResolver().insert(insertUri,contentValues);




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

        // TODO: ENSURE THAT THIS ISN'T TRIGGERED WHEN THE 'SALE' BUTTON IS PRESSED... (REFERENCE MIWOK APP...)
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
}

// ** TESTING DATABASE HELPER METHOD **
//        ProductDbHelper dbHelper = new ProductDbHelper(this);
//        SQLiteDatabase database = dbHelper.getReadableDatabase();
//
//        // ** TESTING CONTENTPROVIDER INSERT METHOD **
//        Uri insertUri = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME);
//        Log.v(LOG_TAG,"In onCreate method; generated URI is: " + insertUri.toString());
//        Log.v(LOG_TAG,"In onCreate method; UriMatcher URI for entire products table is: " +
//                ProductContract.CONTENT_AUTHORITY + ProductContract.ProductEntry.TABLE_NAME);
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, "Acme WhamHammer Stapler");
//        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED, 1);
//        contentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,1991);
//
//        getContentResolver().insert(insertUri,contentValues);
//
//        // insert additional pet(s)
//        getContentResolver().insert(insertUri,contentValues);
//        getContentResolver().insert(insertUri,contentValues);
//
//        // ** TESTING CONTENTPROVIDER UPDATE METHOD **
//        Log.v(LOG_TAG,"Testing the update() method");
//        Uri updateUri = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME + "/2");
//        ContentValues updateContentValues = new ContentValues();
//        updateContentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, "Acme WhamHammer Stapler");
//        updateContentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED, 100);
//        updateContentValues.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,1499);
//
//        getContentResolver().update(updateUri,updateContentValues,null,null);
//
//        // ** TESTING CONTENTPROVIDER DELETE METHOD - FOR SINGLE PRODUCT **
//        Log.v(LOG_TAG,"Testing the delete() method for a single product");
//        Uri deleteUriSingle = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME + "/1");
//        getContentResolver().delete(deleteUriSingle,null,null);
//
//        // ** TESTING CONTENTPROVIDER DELETE METHOD - FOR ALL PRODUCTS **
//        Log.v(LOG_TAG,"Testing the delete() method for all products in the database");
//        Uri deleteUriAll = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME);
//        getContentResolver().delete(deleteUriAll,null,null);
//
//                // ** TESTING CONTENTPROVIDER QUERY METHOD - FOR SINGLE PRODUCT**
//        Uri queryUriSingle = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME + "/8");
//        Uri queryUriSingle = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME);
//        queryUriSingle = ContentUris.withAppendedId(queryUriSingle,1L);
//
//        Cursor cursorSingle = getContentResolver().query(queryUriSingle,
//                null,null,null,null);
//        int rowCountSingle = cursorSingle.getCount();
//        Log.v(LOG_TAG,"Testing the query() method for a single product; " + rowCountSingle + " rows returned");
//
//        // ** TESTING CONTENTPROVIDER QUERY METHOD - FOR ALL PRODUCTS**
//        Log.v(LOG_TAG,"Testing the query() method for all products in the database");
//        Uri queryUriAll = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME);
//
//        Cursor cursorAll = getContentResolver().query(queryUriAll,
//                null,null,null,null);
//        int rowCountAll = cursorAll.getCount();
//        Log.v(LOG_TAG,"Testing the query() method for all products in database; " + rowCountAll + " rows returned");
