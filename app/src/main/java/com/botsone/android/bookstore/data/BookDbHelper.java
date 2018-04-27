package com.botsone.android.bookstore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.botsone.android.bookstore.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper{

    /**
     * name of the database file
     */
    private static final String DATABASE_NAME = "bookstore.db";

    /**
     * Database version, if you change schema, increment this
     */
    private static final int DATABASE_VERSION = 1;

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREATE TABLE books (id INTEGER PRIMARY KEY AUTOINCREMENT, picture BLOB...
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + "("
                + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookEntry.COLUMN_BOOK_PICTURE + " BLOB, "
                + BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, "
                + BookEntry.COLUMN_BOOK_SECTION + " TEXT NOT NULL, "
                + BookEntry.COLUMN_BOOK_AUTHOR + " TEXT, "
                + BookEntry.COLUMN_BOOK_PUBLISHER + " TEXT, "
                + BookEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL, "
                + BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + BookEntry.COLUMN_BOOK_SUPPLIER + " TEXT, "
                + BookEntry.COLUMN_BOOK_SUPPLIER_PHONE + " TEXT);";

        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
