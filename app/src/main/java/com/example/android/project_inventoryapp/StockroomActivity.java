package com.example.android.project_inventoryapp;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.android.project_inventoryapp.data.ProductContract;

public class StockroomActivity extends AppCompatActivity {

    /** Tag for log messages */
    private static final String LOG_TAG = StockroomActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stockroom);

        // ** TESTING DATABASE HELPER METHOD **
//        ProductDbHelper dbHelper = new ProductDbHelper(this);
//        SQLiteDatabase database = dbHelper.getReadableDatabase();

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

        // ** TESTING CONTENTPROVIDER DELETE METHOD - FOR SINGLE PRODUCT **
//        Log.v(LOG_TAG,"Testing the delete() method for a single product");
//        Uri deleteUriSingle = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME + "/1");
//        getContentResolver().delete(deleteUriSingle,null,null);

//        // ** TESTING CONTENTPROVIDER DELETE METHOD - FOR ALL PRODUCTS **
//        Log.v(LOG_TAG,"Testing the delete() method for all products in the database");
//        Uri deleteUriAll = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME);
//        getContentResolver().delete(deleteUriAll,null,null);

                // ** TESTING CONTENTPROVIDER QUERY METHOD - FOR SINGLE PRODUCT**
        Uri queryUriSingle = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME + "/8");
//        Uri queryUriSingle = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME);
//        queryUriSingle = ContentUris.withAppendedId(queryUriSingle,1L);

        Cursor cursorSingle = getContentResolver().query(queryUriSingle,
                null,null,null,null);
        int rowCountSingle = cursorSingle.getCount();
        Log.v(LOG_TAG,"Testing the query() method for a single product; " + rowCountSingle + " rows returned");

        // ** TESTING CONTENTPROVIDER QUERY METHOD - FOR ALL PRODUCTS**
        Log.v(LOG_TAG,"Testing the query() method for all products in the database");
        Uri queryUriAll = Uri.parse(ProductContract.BASE_CONTENT_URI + "/" + ProductContract.ProductEntry.TABLE_NAME);

        Cursor cursorAll = getContentResolver().query(queryUriAll,
                null,null,null,null);
        int rowCountAll = cursorAll.getCount();
        Log.v(LOG_TAG,"Testing the query() method for all products in database; " + rowCountAll + " rows returned");

    }

}
