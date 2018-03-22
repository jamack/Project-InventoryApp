package com.example.android.project_inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.android.project_inventoryapp.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Database helper for Inventory app. Manages database creation and version management.
 */
public class ProductDbHelper extends SQLiteOpenHelper {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    /** Name of the database file */
    public static final String DATABASE_NAME = "store.db";

    /** Database version. If you change the database schema, you must increment the database version */
    public static final int DATABASE_VERSION = 1;

    /** String Constants - database data types */
    public static final String DATATYPE_INTEGER = " INTEGER";
    public static final String DATATYPE_TEXT = " TEXT";
    public static final String DATATYPE_BLOB = " BLOB";

    /** String Constants - table construction keywords */
    public static final String KEYWORD_PRIMARY_KEY = " PRIMARY KEY";
    public static final String KEYWORD_AUTOINCREMENT = " AUTOINCREMENT";
    public static final String KEYWORD_NOT_NULL = " NOT NULL";
    public static final String KEYWORD_DEFAULT = " DEFAULT ";

    /**
     * Constructs a new instance of {@link ProductDbHelper}.
     *
     * @param context of the app
     */
    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                ProductEntry._ID + DATATYPE_INTEGER + KEYWORD_PRIMARY_KEY + KEYWORD_AUTOINCREMENT + "," +
                ProductEntry.COLUMN_PRODUCT_NAME + DATATYPE_TEXT + KEYWORD_NOT_NULL + "," +
                ProductEntry.COLUMN_PRODUCT_PRICE + DATATYPE_INTEGER + KEYWORD_NOT_NULL + "," +
                ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED + DATATYPE_INTEGER + KEYWORD_DEFAULT + "0, " +
                ProductEntry.COLUMN_PRODUCT_IMAGE + DATATYPE_BLOB + ");";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

    /**
     * Static helper method to translate price (in cents) from database integer
     * to a string with dollars and cents (2 decimal places). "$" symbol not included.
     *
     * @param dbPrice integer value from database
     * @return string formatted for dollars and cents - e.g. 19.99
     */
    public static String priceDbToString(int dbPrice) {

        // Database integer represents price in cents.
        // Divide by 100 to get a decimal representing dollars and cents.
        double priceDollarsCents = (double) dbPrice / 100;

        // Round to (2) decimal points
        BigDecimal bd = BigDecimal.valueOf(priceDollarsCents);
        BigDecimal rounded = bd.setScale(2, RoundingMode.HALF_UP);

        // Convert to a string and return string
        return rounded.toString();
    }

    /**
     * Constant that signifies failure of priceStringtoDB helper method to properly parse user's input price.
     */
    public static final int PRICE_PARSE_FAILURE = -1;

    /**
     * Static helper method to translate price from string of dollars and cents
     * to an integer value that can be used by the database's price column.
     *
     * @param stringPrice string with dollars and cents, 2 decimal points, no '$' symbol - e.g. 19.99
     * @return integer price, reflecting just cents - e.g. 1999
     */
    public static int priceStringToDb(String stringPrice) {

        // Try parsing string to a double, representing price in dollars and cents.
        double priceDollarsCents;
        try {
            priceDollarsCents = Double.parseDouble(stringPrice);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG,"Cannot parse product price into a double");
            // Return an integer outside valid range to notify calling method of parsing failure
            return PRICE_PARSE_FAILURE;
        }

        // Multiply by 100 to remove the (2) decimal points and store as an integer
        int dbPrice = (int) (priceDollarsCents * 100);

        // Return the database-friendly integer value
        return dbPrice;
    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

}
