package com.jso.gigsight.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.jso.gigsight.data.FavoritesContract.AUTHORITY;
import static com.jso.gigsight.data.FavoritesContract.FavoritesEntry;
import static com.jso.gigsight.data.FavoritesContract.PATH_FAVORITES;
import static com.jso.gigsight.utils.Constants.CODE_FAVORITES;
import static com.jso.gigsight.utils.Constants.INSERT_FAILURE;
import static com.jso.gigsight.utils.Constants.UNKNOWN_URI;

public class FavoritesProvider extends ContentProvider {

    private FavoritesDbHelper dbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH_FAVORITES, CODE_FAVORITES);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new FavoritesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_FAVORITES:
                cursor = db.query(FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException(UNKNOWN_URI + uri);
        }
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri returnedUri;
        switch (sUriMatcher.match(uri)) {
            case CODE_FAVORITES:
                long id = db.insert(FavoritesEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnedUri = ContentUris.withAppendedId(FavoritesEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException(INSERT_FAILURE + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException(UNKNOWN_URI + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnedUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int row;
        switch (sUriMatcher.match(uri)) {
            case CODE_FAVORITES:
                row = db.delete(FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException(UNKNOWN_URI + uri);
        }
        if (getContext() != null && row != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return row;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }
}
