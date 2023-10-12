package com.jso.gigsight.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.jso.gigsight.data.FavoritesContract.FavoritesEntry;
import static com.jso.gigsight.utils.Constants.DATABASE_NAME;
import static com.jso.gigsight.utils.Constants.DATABASE_VERSION;

public class FavoritesDbHelper extends SQLiteOpenHelper {

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_FAVORITES_TABLE = "CREATE TABLE " + FavoritesEntry.TABLE_NAME + " (" +
                FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavoritesEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                FavoritesEntry.COLUMN_TITLE + " TEXT, " +
                FavoritesEntry.COLUMN_DATE + " TEXT, " +
                FavoritesEntry.COLUMN_IMAGE + " TEXT, " +
                FavoritesEntry.COLUMN_VENUE + " TEXT, " +
                FavoritesEntry.COLUMN_ADDRESS + " TEXT, " +
                FavoritesEntry.COLUMN_EXTENDED_ADDRESS + " TEXT, " +
                " UNIQUE (" + FavoritesEntry.COLUMN_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
