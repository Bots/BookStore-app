package com.botsone.android.bookstore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.botsone.android.bookstore.data.BookContract.BookEntry;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.text.NumberFormat;

/**
 * Allows user to create a new book or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;

    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri mCurrentBookUri;

    /**
     * Log tag for debug purposes
     */
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * EditText field to enter the book's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the book's store section
     */
    private EditText mSectionEditText;

    /**
     * EditText field to enter the book's author
     */
    private EditText mAuthorEditText;

    /**
     * EditText field to enter the book's publisher
     */
    private EditText mPublisherEditText;

    /**
     * EditText field to enter the book's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the book's quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the book's supplier
     */
    private EditText mSupplierEditText;

    /**
     * EditText field to enter the book's supplier phone number
     */
    private EditText mSupplierPhoneEditText;

    private Uri mUri;

    private ImageView mImageView;

    private Button mMinusButton;
    private Button mPlusButton;

    private Button mCallButton;

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int SEND_MAIL_REQUEST = 1;

    /**
     * Boolean flag to keep track of whether book has been edited
     */
    private boolean mBookHasChanged = false;

    /**
     * onTouchListener that listens for any user touches on a view, implying that the user is
     * modifying the view, if so we change mBookHasChanged to true
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity in order to figure out
        // if we are creating a new book or editing an existing one
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // If the intent doesn't contain a specific URI we know to create a new book
        if (mCurrentBookUri == null) {
            // This is a new book, change the app bar to say "Add a Book"
            setTitle(R.string.editor_activity_title_new_book);

            // Invalidate the options menu so "Delete" doesn't appear and confuse user
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing book, change bar to say "Edit Book"
            setTitle(R.string.editor_activity_title_edit_book);
        }

        // Find all the relevant views that we will need to read user input from
        mMinusButton = (Button) findViewById(R.id.minus_button);
        mPlusButton = (Button) findViewById(R.id.plus_button);
        mCallButton = (Button) findViewById(R.id.call_button);
        mImageView = (ImageButton) findViewById(R.id.edit_image_view);
        mNameEditText = (EditText) findViewById(R.id.edit_book_name);
        mSectionEditText = (EditText) findViewById(R.id.edit_book_section);
        mAuthorEditText = (EditText) findViewById(R.id.edit_book_author);
        mPublisherEditText = (EditText) findViewById(R.id.edit_book_publisher);
        mPriceEditText = (EditText) findViewById(R.id.edit_book_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_book_quantity);
        mSupplierEditText = (EditText) findViewById(R.id.edit_book_supplier);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_book_supplier_phone);

        // Initialize a loader to read the book data from the db and display
        // the current values in the editor
        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);



                // Set up onClickListener for imageView
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });

        // Set up click listener for minus button
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = mQuantityEditText.getText().toString();
                int quantity = Integer.parseInt(quantityString);

                if (quantity > 0) {
                    quantity --;
                    String newQuantity = String.valueOf(quantity);
                    mQuantityEditText.setText(newQuantity);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.sale_button_zero, Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Set up click listener for plus button
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = mQuantityEditText.getText().toString();
                int quantity = Integer.parseInt(quantityString);

                quantity ++;
                String newQuantity = String.valueOf(quantity);
                mQuantityEditText.setText(newQuantity);
            }
        });

        // Set up click listener for call button
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mSupplierPhoneEditText.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            }
        });

        // Setup onTouchListeners on all input fields to determine whether the user has
        // touched them so we can warn about unsaved changes
        mImageView.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mSectionEditText.setOnTouchListener(mTouchListener);
        mAuthorEditText.setOnTouchListener(mTouchListener);
        mPublisherEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());

                //mImageView.setImageBitmap(getBitmapFromUri(mUri));
                Picasso.get().load(mUri).resize(400,400).into(mImageView);

            }
        } else if (requestCode == SEND_MAIL_REQUEST && resultCode == Activity.RESULT_OK) {

        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    private void saveBook() {
        // Read input from fields
        // Use trim to trim leading or trailing whitespace

        String pictureString;
        if (mUri != null) {
            pictureString = mUri.toString();
        } else {
            Cursor cursor = getContentResolver().query(mCurrentBookUri, null, null, null, null);
            cursor.moveToFirst();
            pictureString = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PICTURE));

        }
        String nameString = mNameEditText.getText().toString().trim();
        String sectionString = mSectionEditText.getText().toString().trim();
        String authorString = mAuthorEditText.getText().toString().trim();
        String publisherString = mPublisherEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String supplierNumberString = mSupplierPhoneEditText.getText().toString().trim();

        // Convert price to double and quantity to int
        double price = Double.parseDouble(priceString);

        int quantity = Integer.parseInt(quantityString);

        // Create a ContentValues object where column names are the keys,
        // and book attributes from the editor are the values.
        ContentValues values = new ContentValues();

        values.put(BookEntry.COLUMN_BOOK_PICTURE, pictureString);
        values.put(BookEntry.COLUMN_BOOK_NAME, nameString);
        values.put(BookEntry.COLUMN_BOOK_SECTION, sectionString);
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, authorString);
        values.put(BookEntry.COLUMN_BOOK_PUBLISHER, publisherString);
        values.put(BookEntry.COLUMN_BOOK_PRICE, price);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER, supplierString);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE, supplierNumberString);

        // Determine if this is a new book or not by checking if mCurrentBookUri is null
        if (mCurrentBookUri == null)

    {
        // NEW BOOK - insert book into the provider, return the URI for the new book
        Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

        // Pop toast saying whether or not we had success
        if (newUri == null) {
            // ERROR
            Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Insertion successful
            Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                    Toast.LENGTH_SHORT).show();
        }

    } else

    {
        // EXISTING BOOK - update book with content URI mCurrentBookUri and pass in the
        // new ContentValues. Pass null for selection and selectionArgs because mCurrentBookUri
        // will already identify the correct row in the db that we want to modify
        int rowsAffected = getContentResolver().update(mCurrentBookUri, values,
                null, null);

        // Pop toast saying whether update was successful
        if (rowsAffected == 0) {
            // ERROR
            Toast.makeText(this, getString(R.string.editor_update_book_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // update successful
            Toast.makeText(this, getString(R.string.editor_update_book_successful),
                    Toast.LENGTH_SHORT).show();
        }


    }

}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    // This method is called after invalidateOptionsMenu so that the menu can be updated
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is a new book hide the delete menu item
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                // Check to make sure name price and quantity are entered
                if (mNameEditText.getText().toString().isEmpty() ||
                        mPriceEditText.getText().toString().isEmpty() ||
                        mQuantityEditText.getText().toString().isEmpty()) {
                    Toast.makeText(this, R.string.editor_enter_info,
                            Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    saveBook();
                    finish();
                    return true;
                }

                // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop confirmation dialog
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If no changes were made go up to parent activity
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // If there are unsaved changes, pop a dialog
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        // If book hasn't changed, continue with back press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise unsaved changes were made, pop a dialog.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Discard button close the current activity
                finish();
            }
        };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (mCurrentBookUri == null) {
            return null;
        }

        // This loader will execute the ContentProvider's query method on
        // a background thread
        return new CursorLoader(this, // Parent activity context
                mCurrentBookUri,             // Query the contentUri for the current book
                null,                  // Columns to include
                null,                // No selection clause
                null,             // No selection args
                null);               // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int pictureColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PICTURE);
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int sectionColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SECTION);
            int authorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);
            int publisherColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PUBLISHER);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE);

            // Extract out the value from the cursor for the given column index
            String picture = cursor.getString(pictureColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String section = cursor.getString(sectionColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            String publisher = cursor.getString(publisherColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            String stringPrice = Double.toString(price);
            int quantity = cursor.getInt(quantityColumnIndex);
            String stringQuantity = Integer.toString(quantity);
            String supplier = cursor.getString(supplierColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            //mImageView.setImageURI(Uri.parse(picture));
            Picasso.get().load(Uri.parse(picture)).resize(400, 400).into(mImageView);
            mNameEditText.setText(name);
            mSectionEditText.setText(section);
            mAuthorEditText.setText(author);
            mPublisherEditText.setText(publisher);
            mPriceEditText.setText(stringPrice);
            mQuantityEditText.setText(stringQuantity);
            mSupplierEditText.setText(supplier);
            mSupplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all data from the input fields
        mImageView.setImageURI(null);
        mNameEditText.setText("");
        mSectionEditText.setText("");
        mAuthorEditText.setText("");
        mPublisherEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
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
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
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
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform delete if this is an existing book
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }

        // Notify the content resolver and close the activity
        getContentResolver().notifyChange(mCurrentBookUri, null);
        finish();
    }
}
