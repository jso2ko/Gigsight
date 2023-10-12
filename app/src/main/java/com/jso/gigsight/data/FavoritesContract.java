package com.jso.gigsight.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoritesContract {

    static final String AUTHORITY = "com.jso.gigsight";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    static final String PATH_FAVORITES = "favorites";

    public static final class FavoritesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_VENUE = "venue";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_EXTENDED_ADDRESS = "extendedAddress";
    }
}
