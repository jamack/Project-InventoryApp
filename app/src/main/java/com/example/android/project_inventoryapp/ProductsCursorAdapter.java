package com.example.android.project_inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.project_inventoryapp.data.ProductContract;
import com.example.android.project_inventoryapp.data.ProductDbHelper;

/**
 * {@link ProductsCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductsCursorAdapter extends CursorAdapter {

    /** Tag for log messages */
    private static final String LOG_TAG = ProductsCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link ProductsCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductsCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // Inflate the list item view
        View listItem = LayoutInflater.from(context).inflate(R.layout.item_stockroom_listview,parent,false);

        // Create a new ViewHolder object to cache children views
        ViewHolder holder = new ViewHolder();
        holder.name = listItem.findViewById(R.id.listview_item_name);
        holder.quantity = listItem.findViewById(R.id.listview_item_quantity);
        holder.price = listItem.findViewById(R.id.listview_item_price);
        holder.saleButton = listItem.findViewById(R.id.listview_item_sale_button);

        // Attach the ViewHolder object with cached views
        listItem.setTag(holder);

        // Return the list item view
        return listItem;
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Retrieve tagged ViewHolder object and its cached views
        ViewHolder holder = (ViewHolder) view.getTag();
        TextView tvName = holder.name;
        TextView tvQuantity = holder.quantity;
        TextView tvPrice = holder.price;
        Button btnSale = holder.saleButton;

        // Get data from cursor
        String cName = cursor.getString(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME));
        String cQuantity = cursor.getString(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY_STOCKED));
        Integer cPrice = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE));

        // Set data on the textviews
        tvName.setText(cName);
        tvQuantity.setText(cQuantity);
        tvPrice.setText("$" + ProductDbHelper.priceDbToString(cPrice));
    }

    /**
     * /** Cache of the children views for a list item.
     * Store inside the item views w/ Tag.
     */
    static class ViewHolder {
        TextView name;
        TextView quantity;
        TextView price;
        Button saleButton;
    }
}
