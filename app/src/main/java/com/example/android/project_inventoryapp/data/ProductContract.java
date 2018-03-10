package com.example.android.project_inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */
public class ProductContract {

    // Empty constructor to prevent accidental instantiation of the contract class
    private ProductContract() {}

    /**
     * Constant for content authority portion of content provider URI
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.project_inventoryapp";

    /**
     * Uri object initialized with scheme + content authority portion of content provider URI
     */
    public static final Uri BASE_CONTENT_URI= Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static class ProductEntry implements BaseColumns {


        /** Constant, table name */
        public final static String TABLE_NAME = "products";

        /** The content URI to access the product data in the provider */
        public final static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        // Constant, title for _id column
        public static final String _ID = BaseColumns._ID;

        // Constant, title for product name column
        public static final String COLUMN_PRODUCT_NAME = "name";

        // Constant, title for quantity-in-stock column
        public static final String COLUMN_PRODUCT_QUANTITY_STOCKED = "quantity";

        // Constant, title for product price column
        public static final String COLUMN_PRODUCT_PRICE = "price";

        // Constant, title for product image column
        public static final String COLUMN_PRODUCT_IMAGE = "image";
    }

}
