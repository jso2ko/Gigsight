package com.jso.gigsight.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import com.jso.gigsight.model.Concert;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static com.jso.gigsight.utils.Constants.PATH_EVENTS;
import static com.jso.gigsight.utils.Constants.QUERY_CLIENT_ID;
import static com.jso.gigsight.utils.Constants.QUERY_LAT;
import static com.jso.gigsight.utils.Constants.QUERY_LON;
import static com.jso.gigsight.utils.Constants.QUERY_PAGE;
import static com.jso.gigsight.utils.Constants.QUERY_SORT;
import static com.jso.gigsight.utils.Constants.QUERY_TYPE;

public class NetworkUtils {

    public interface OnGetConcertsCallback {
        void onSuccess(int page, List<Concert> concerts);

        void onError();
    }

    public interface ConcertApi {
        @GET(PATH_EVENTS)
        Call<Concert> getConcerts
                (@Query(QUERY_TYPE) String type,
                 @Query(QUERY_CLIENT_ID) String clientId,
                 @Query(QUERY_SORT) String sort,
                 @Query(QUERY_LAT) double latitude,
                 @Query(QUERY_LON) double longitude,
                 @Query(QUERY_PAGE) int page);

        @GET(PATH_EVENTS)
        Call<Concert> getAll
                (@Query(QUERY_TYPE) String type,
                 @Query(QUERY_CLIENT_ID) String clientId,
                 @Query(QUERY_SORT) String sort,
                 @Query(QUERY_PAGE) int page);
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / 180);
    }
}
