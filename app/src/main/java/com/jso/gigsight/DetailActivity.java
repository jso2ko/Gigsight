package com.jso.gigsight;

import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jso.gigsight.databinding.ActivityDetailBinding;
import com.jso.gigsight.model.Concert;
import com.jso.gigsight.utils.ImageAsyncTask;

import org.parceler.Parcels;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.jso.gigsight.data.FavoritesContract.FavoritesEntry;
import static com.jso.gigsight.utils.Constants.BLOCK_IMAGE_SIZE;
import static com.jso.gigsight.utils.Constants.CONCERT_KEY;
import static com.jso.gigsight.utils.Constants.DATE_INPUT;
import static com.jso.gigsight.utils.Constants.DATE_OUTPUT;
import static com.jso.gigsight.utils.Constants.DEFAULT_IMAGE_SIZE;
import static com.jso.gigsight.utils.Constants.DETAIL_LOADER_ID;
import static com.jso.gigsight.utils.Constants.EMPTY_ADDRESS_STRING;
import static com.jso.gigsight.utils.Constants.NULL_STRING;
import static com.jso.gigsight.utils.Constants.SQUARE_IMAGE_SIZE;
import static com.jso.gigsight.utils.Constants.TARGET_REPLACE;
import static com.jso.gigsight.utils.Constants.TBA_STRING;
import static com.jso.gigsight.utils.Constants.TBA_TIME;
import static com.jso.gigsight.utils.Constants.VENUE_KEY;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ActivityDetailBinding detailBinding;
    private Concert currentConcert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        detailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        detailBinding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        detailBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        currentConcert = Parcels.unwrap(getIntent().getParcelableExtra(CONCERT_KEY));
        showConcertDetails();
        clickDetailButtons();
        getSupportLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
    }

    private void showConcertDetails() {
        if (currentConcert.getPerformers() != null) {
            String image = currentConcert.getPerformers().get(0).getImage();
            if (image == null || image.equals(NULL_STRING)) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    detailBinding.posterImageView.setImageResource(R.drawable.block);
                } else {
                    detailBinding.posterImageView.setImageResource(R.drawable.square);
                }
            } else {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    image = image.replace(DEFAULT_IMAGE_SIZE, BLOCK_IMAGE_SIZE);
                } else {
                    image = image.replace(DEFAULT_IMAGE_SIZE, SQUARE_IMAGE_SIZE);
                }

                Glide.with(getApplicationContext())
                        .load(image)
                        .apply(RequestOptions.placeholderOf(R.drawable.loading)
                                .error(R.drawable.error))
                        .into(detailBinding.posterImageView);
            }
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (currentConcert.getPerformers() != null) {
                String thumbnail = currentConcert.getPerformers().get(0).getImage();
                if (thumbnail == null || thumbnail.equals(NULL_STRING)) {
                    detailBinding.thumbnailImageView.setImageResource(R.drawable.square);
                } else {
                    thumbnail = thumbnail.replace(DEFAULT_IMAGE_SIZE, SQUARE_IMAGE_SIZE);
                    try {
                        URL url = new URL(thumbnail);
                        new ImageAsyncTask(detailBinding.thumbnailImageView).execute(url);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        detailBinding.titleTextView.setText(currentConcert.getTitle());

        try {
            String dateTimeLocal = currentConcert.getDateTime().replace(TARGET_REPLACE, " ");
            DateFormat dateInput = new SimpleDateFormat(DATE_INPUT, Locale.getDefault());
            DateFormat dateOutput = new SimpleDateFormat(DATE_OUTPUT, Locale.getDefault());

            Date dateObject = dateInput.parse(dateTimeLocal);
            String formattedDate = dateOutput.format(dateObject);

            if (formattedDate.contains(TBA_TIME)) {
                String formattedDateWithTba = formattedDate.replace(TBA_TIME, TBA_STRING);
                detailBinding.dateTimeTextView.setText(formattedDateWithTba);
            } else {
                detailBinding.dateTimeTextView.setText(formattedDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (currentConcert.getVenue() != null) {
            String venue = currentConcert.getVenue().getName();
            if (venue == null || venue.equals(NULL_STRING)) {
                detailBinding.venueTextView.setText(TBA_STRING);
            } else {
                detailBinding.venueTextView.setText(venue);
            }
        }

        if (currentConcert.getVenue() != null) {
            String address = currentConcert.getVenue().getAddress();
            String extendedAddress = currentConcert.getVenue().getExtendedAddress();
            if ((address == null || address.equals(NULL_STRING)) &&
                    (extendedAddress == null || extendedAddress.equals(NULL_STRING))) {
                detailBinding.addressTextView.setText(EMPTY_ADDRESS_STRING);
                detailBinding.addressTextView.setVisibility(View.VISIBLE);
                detailBinding.extendedAddressTextView.setVisibility(View.GONE);
            } else {
                detailBinding.addressTextView.setText(address);
                detailBinding.extendedAddressTextView.setText(extendedAddress);
            }
        }
    }

    private void clickDetailButtons() {
        detailBinding.ticketsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentConcert.getUrl()));
                startActivity(intent);
            }
        });

        detailBinding.mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = currentConcert.getVenue().getAddress();
                String extendedAddress = currentConcert.getVenue().getExtendedAddress();
                if ((address == null || address.equals(NULL_STRING)) &&
                        (extendedAddress == null || extendedAddress.equals(NULL_STRING))) {
                    detailBinding.mapButton.setEnabled(false);
                } else {
                    detailBinding.mapButton.setEnabled(true);
                }

                Intent intent = new Intent(DetailActivity.this, MapsActivity.class);
                intent.putExtra(VENUE_KEY, Parcels.wrap(currentConcert.getVenue()));
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(DetailActivity.this);
                startActivity(intent, options.toBundle());

            }
        });

        detailBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFavorite()) {
                    insertFavorite();
                } else {
                    deleteFavorite();
                }
            }
        });
    }

    private boolean isFavorite() {
        Cursor cursor = getContentResolver().query(FavoritesEntry.CONTENT_URI,
                null,
                FavoritesEntry.COLUMN_ID + " = " + currentConcert.getId(),
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return true;
        }
        return false;
    }

    private void insertFavorite() {
        if (currentConcert.getVenue() != null && currentConcert.getPerformers() != null) {
            ContentValues values = new ContentValues();
            values.put(FavoritesEntry.COLUMN_ID, currentConcert.getId());
            values.put(FavoritesEntry.COLUMN_TITLE, currentConcert.getTitle());
            values.put(FavoritesEntry.COLUMN_DATE, currentConcert.getDateTime());
            values.put(FavoritesEntry.COLUMN_IMAGE, currentConcert.getPerformers().get(0).getImage());
            values.put(FavoritesEntry.COLUMN_VENUE, currentConcert.getVenue().getName());
            values.put(FavoritesEntry.COLUMN_ADDRESS, currentConcert.getVenue().getAddress());
            values.put(FavoritesEntry.COLUMN_EXTENDED_ADDRESS,
                    currentConcert.getVenue().getExtendedAddress());

            getContentResolver().insert(FavoritesEntry.CONTENT_URI, values);
        }

        detailBinding.fab.setImageResource(R.drawable.ic_favorite_mark);
        detailBinding.fab.setContentDescription(getString(R.string.favorite_marked_button));
        Toast.makeText(this, getString(R.string.favorite_saved_message), Toast.LENGTH_SHORT).show();
    }

    private void deleteFavorite() {
        getContentResolver().delete(FavoritesEntry.CONTENT_URI,
                FavoritesEntry.COLUMN_ID + " = " + currentConcert.getId(),
                null);

        detailBinding.fab.setImageResource(R.drawable.ic_favorite_unmark);
        detailBinding.fab.setContentDescription(getString(R.string.favorite_unmarked_button));
        Toast.makeText(this, getString(R.string.favorite_removed_message), Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                FavoritesEntry.COLUMN_ID,
                FavoritesEntry.COLUMN_TITLE,
                FavoritesEntry.COLUMN_DATE,
                FavoritesEntry.COLUMN_IMAGE,
                FavoritesEntry.COLUMN_VENUE,
                FavoritesEntry.COLUMN_ADDRESS,
                FavoritesEntry.COLUMN_EXTENDED_ADDRESS
        };

        return new CursorLoader(DetailActivity.this,
                FavoritesEntry.CONTENT_URI,
                projection,
                FavoritesEntry.COLUMN_ID + " = " + currentConcert.getId(),
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            detailBinding.fab.setImageResource(R.drawable.ic_favorite_mark);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
