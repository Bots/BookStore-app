package com.botsone.android.bookstore;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.botsone.android.bookstore.data.BookContract.BookEntry;
import com.squareup.picasso.Picasso;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */
public class BookCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor c) {
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
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find the views that we want to modify in the list item layout
        ImageView imageView = (ImageView) view.findViewById(R.id.list_item_image_view);
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        // Find the columns of book attributes that we want
        int pictureColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PICTURE);
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);

        // Read the book attributes from the cursor for the current book
        String bookPicture = cursor.getString(pictureColumnIndex);
        String bookName = cursor.getString(nameColumnIndex);
        Double bookPrice = cursor.getDouble(priceColumnIndex);
        String bookQuantity = cursor.getString(quantityColumnIndex);

        Uri bookPictureUri = Uri.parse(bookPicture);

        //Update the imageView with pic from current book
        if (bookPictureUri == null || bookPicture.equals("")) {
            Picasso.get().load(R.drawable.ic_empty_shelter).resize(200, 200).into(imageView);
            //imageView.setImageURI();
        } else {
            Picasso.get().load(bookPictureUri).resize(200, 200).into(imageView);
            //imageView.setImageURI(bookPictureUri);
        }


        // Update the textviews with the attributes for the current book
        nameTextView.setText(bookName);
        priceTextView.setText("$" + bookPrice.toString());
        quantityTextView.setText(bookQuantity);
    }
}