package com.jso.gigsight.utils;

import android.support.annotation.NonNull;

import com.jso.gigsight.model.Concert;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.jso.gigsight.utils.Constants.BASE_URL;
import static com.jso.gigsight.utils.Constants.CLIENT_ID;
import static com.jso.gigsight.utils.Constants.DATE_ASC;
import static com.jso.gigsight.utils.Constants.TYPE_CONCERT;
import static com.jso.gigsight.utils.NetworkUtils.ConcertApi;
import static com.jso.gigsight.utils.NetworkUtils.OnGetConcertsCallback;

public class Repository {

    private static Repository repository;
    private ConcertApi api;

    private Repository(ConcertApi api) {
        this.api = api;
    }

    public static Repository getInstance() {
        if (repository == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            repository = new Repository(retrofit.create(ConcertApi.class));
        }
        return repository;
    }

    public void getConcerts(int page, double latitude, double longitude, final OnGetConcertsCallback callback) {
        api.getConcerts(TYPE_CONCERT, CLIENT_ID, DATE_ASC, latitude, longitude, page)
                .enqueue(new Callback<Concert>() {
                    @Override
                    public void onResponse(@NonNull Call<Concert> call, @NonNull Response<Concert> response) {
                        Concert concertResponse = response.body();
                        if (concertResponse != null) {
                            callback.onSuccess(concertResponse.getMeta().getPage(),
                                    concertResponse.getConcerts());
                        } else {
                            callback.onError();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Concert> call, @NonNull Throwable t) {
                        callback.onError();
                    }
                });
    }

    public void getAll(int page, final OnGetConcertsCallback callback) {
        api.getAll(TYPE_CONCERT, CLIENT_ID, DATE_ASC, page).enqueue(new Callback<Concert>() {
            @Override
            public void onResponse(@NonNull Call<Concert> call, @NonNull Response<Concert> response) {
                Concert concertResponse = response.body();
                if (concertResponse != null && concertResponse.getConcerts() != null) {
                    callback.onSuccess(concertResponse.getMeta().getPage(),
                            concertResponse.getConcerts());
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Concert> call, @NonNull Throwable t) {
                callback.onError();
            }
        });
    }
}