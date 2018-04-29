package com.botsone.android.bookstore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.carrier.CarrierMessagingService;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.botsone.android.bookstore.data.BookContract.BookEntry;

import java.util.List;

/**
 * Displays list of books that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int BOOK_LOADER = 0;

    BookCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the listView which will be populated with book data
        ListView bookListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on listview when there are 0 books
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        // Set up and adapter to create a list item for each row of book data in the cursor
        // There is no book data yet (until the loader finishes) so pass null for the cursor
        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);

        // Set up the item click listener for each cell in the listview
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create new intent to go to editorActivity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content uri that represents the specific book that was clicked on
                // by appending the "id" (passed as an input to this method) onto the contentUri
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentBookUri);

                startActivity(intent);

            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void insertBook() {

        // Create a contentValues object where column names are the keys,
        // and Star Wars attributes are the values
        ContentValues values = new ContentValues();

        values.put(BookEntry.COLUMN_BOOK_NAME, "Star Wars");
        values.put(BookEntry.COLUMN_BOOK_SECTION, "Sci-fi");
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, "George Danson");
        values.put(BookEntry.COLUMN_BOOK_PUBLISHER, "Penguin Books Inc.");
        values.put(BookEntry.COLUMN_BOOK_PRICE, 6.99);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, 10);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER, "FakeSupplier Co.");
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE, "5438943221");

        Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
    }

    /**
     * Helper to delete all pets from the db for testing purposes
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI,
                null,
                null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from book database");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection for the columns we are interested in
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,  // Parent activity context
                BookEntry.CONTENT_URI,        // Provider content to query
                projection,                   // Columns to include in resulting cursor
                null,                 // No selection clause
                null,              // No selection args
                null);               // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Pass resulting cursor into CursorAdapter
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
