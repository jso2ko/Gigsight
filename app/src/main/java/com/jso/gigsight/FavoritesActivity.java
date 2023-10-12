package com.jso.gigsight;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.jso.gigsight.adapter.ConcertAdapter;
import com.jso.gigsight.databinding.ActivityFavoritesBinding;
import com.jso.gigsight.model.Concert;
import com.jso.gigsight.model.Performer;
import com.jso.gigsight.model.Venue;
import com.jso.gigsight.utils.NetworkUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jso.gigsight.data.FavoritesContract.FavoritesEntry;
import static com.jso.gigsight.utils.Constants.CONCERT_KEY;
import static com.jso.gigsight.utils.Constants.FAVORITES_LOADER_ID;
import static com.jso.gigsight.utils.Constants.PERFORMERS_KEY;

public class FavoritesActivity extends AppCompatActivity implements ConcertAdapter.ConcertClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    private ActivityFavoritesBinding favoritesBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favoritesBinding = DataBindingUtil.setContentView(this, R.layout.activity_favorites);
        setSupportActionBar(favoritesBinding.toolbar);

        favoritesBinding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        favoritesBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        int noOfColumns = NetworkUtils.calculateNoOfColumns(this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, noOfColumns);
        favoritesBinding.recyclerView.setLayoutManager(layoutManager);

        getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, this);
    }

    @Override
    public void onClick(Concert concert) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(CONCERT_KEY, Parcels.wrap(concert));
        startActivity(intent);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle args) {
        String[] projection = {
                FavoritesEntry.COLUMN_ID,
                FavoritesEntry.COLUMN_TITLE,
                FavoritesEntry.COLUMN_DATE,
                FavoritesEntry.COLUMN_IMAGE,
                FavoritesEntry.COLUMN_VENUE,
                FavoritesEntry.COLUMN_ADDRESS,
                FavoritesEntry.COLUMN_EXTENDED_ADDRESS
        };

        return new CursorLoader(FavoritesActivity.this,
                FavoritesEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        List<Concert> favoriteConcerts = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(cursor.getColumnIndex(FavoritesEntry.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndex(FavoritesEntry.COLUMN_TITLE));
            String date = cursor.getString(cursor.getColumnIndex(FavoritesEntry.COLUMN_DATE));
            String image = cursor.getString(cursor.getColumnIndex(FavoritesEntry.COLUMN_IMAGE));
            String venue = cursor.getString(cursor.getColumnIndex(FavoritesEntry.COLUMN_VENUE));
            String address = cursor.getString(cursor.getColumnIndex(FavoritesEntry.COLUMN_ADDRESS));
            String extendedAddress =
                    cursor.getString(cursor.getColumnIndex(FavoritesEntry.COLUMN_EXTENDED_ADDRESS));

            Venue venueObject = new Venue(venue, address, extendedAddress);
            List<Performer> performerList = new ArrayList<>();
            performerList.add(new Performer(image));
            Map<String, List<Performer>> hashMap = new HashMap<>();
            hashMap.put(PERFORMERS_KEY, performerList);
            List<Performer> performers = hashMap.get(PERFORMERS_KEY);

            favoriteConcerts.add(new Concert(id, title, date, venueObject, performers));
            cursor.moveToNext();
        }

        ConcertAdapter adapter = new ConcertAdapter(this, favoriteConcerts, this);
        favoritesBinding.recyclerView.setAdapter(adapter);

        if (favoriteConcerts.isEmpty()) {
            favoritesBinding.emptyImageView.setVisibility(View.VISIBLE);
            favoritesBinding.emptyTitleTextView.setVisibility(View.VISIBLE);
            favoritesBinding.emptySubtitleTextView.setVisibility(View.VISIBLE);
        } else {
            favoritesBinding.emptyImageView.setVisibility(View.GONE);
            favoritesBinding.emptyTitleTextView.setVisibility(View.GONE);
            favoritesBinding.emptySubtitleTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
