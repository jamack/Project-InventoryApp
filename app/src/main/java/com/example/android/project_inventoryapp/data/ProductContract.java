package com.example.android.project_inventoryapp.data;

import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */
public class ProductContract {

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static class ProductEntry implements BaseColumns {

        /** Constant, table name */
        public final static String TABLE_NAME = "products";

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
