package com.botsone.android.bookstore;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.botsone.android.bookstore.data.BookContract.BookEntry;
import com.squareup.picasso.Picasso;

import java.util.zip.Inflater;

import static com.botsone.android.bookstore.data.BookContract.BookEntry.CONTENT_URI;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */
public class BookCursorAdapter extends CursorAdapter {

    private int bookQuantity;
    private Context context;
    private LayoutInflater inflater;

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        this.context = context;

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
    public void bindView(View view, final Context context, final Cursor cursor) {



        // Find the views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        Button button = (Button) view.findViewById(R.id.sale_button);
        ImageView imageView = (ImageView) view.findViewById(R.id.list_item_image_view);

        nameTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_NAME)));
        priceTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_PRICE)));
        quantityTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_QUANTITY)));

        /* Get values from db */
        final int currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_QUANTITY));
        String currentId = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry._ID));
        final Uri currentUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, Long.parseLong(currentId));

        int pictureColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PICTURE);
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
        String bookPicture = cursor.getString(pictureColumnIndex);
        String bookName = cursor.getString(nameColumnIndex);
        Double bookPrice = cursor.getDouble(priceColumnIndex);
        bookQuantity = cursor.getInt(quantityColumnIndex);
        Uri bookPictureUri = Uri.parse(bookPicture);

        if (bookPictureUri == null || bookPicture.equals("")) {
            Picasso.get().load(R.drawable.ic_empty_shelter).resize(200, 200).into(imageView);
        } else {
            Picasso.get().load(bookPictureUri).resize(200, 200).into(imageView);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();

                if (currentQuantity == 0) {
                    Toast.makeText(context, R.string.sale_button_zero, Toast.LENGTH_SHORT).show();
                } else {
                    values.put(BookEntry.COLUMN_BOOK_QUANTITY, currentQuantity - 1);
                    context.getContentResolver().update(currentUri, values, null, null);
                }
            }
        });

        nameTextView.setText(bookName);
        priceTextView.setText("$" + bookPrice.toString());
        quantityTextView.setText(Integer.toString(bookQuantity));
//
//        // Find the columns of book attributes that we want
//        int pictureColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PICTURE);
//        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
//        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
//        final int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
//
//        // Read the book attributes from the cursor for the current book
//        String bookPicture = cursor.getString(pictureColumnIndex);
//        String bookName = cursor.getString(nameColumnIndex);
//        Double bookPrice = cursor.getDouble(priceColumnIndex);
//        bookQuantity = cursor.getInt(quantityColumnIndex);
//
//        Uri bookPictureUri = Uri.parse(bookPicture);
//        final Uri bookQuantityUri = Uri.parse((Integer.toString(bookQuantity)));
//
//        final Uri bookUri = BookEntry.CONTENT_URI;
//
//        final ContentValues values = new ContentValues();

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bookQuantity = bookQuantity - 1;
//                quantityTextView.setText(Integer.toString(bookQuantity));
//                values.put(BookEntry.COLUMN_BOOK_QUANTITY, bookQuantity);
//                context.getContentResolver().update(bookUri, values, null, null);
//            }
//        });



//        //Update the imageView with pic from current book
//        if (bookPictureUri == null || bookPicture.equals("")) {
//            Picasso.get().load(R.drawable.ic_empty_shelter).resize(200, 200).into(imageView);
//        } else {
//            Picasso.get().load(bookPictureUri).resize(200, 200).into(imageView);
//        }
//
//        // Update the textviews with the attributes for the current book
//        nameTextView.setText(bookName);
//        priceTextView.setText("$" + bookPrice.toString());
//        quantityTextView.setText(Integer.toString(bookQuantity));
    }


}