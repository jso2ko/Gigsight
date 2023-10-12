package com.jso.gigsight.widget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.jso.gigsight.R;
import com.jso.gigsight.adapter.WidgetAdapter;
import com.jso.gigsight.databinding.ActivityWidgetBinding;
import com.jso.gigsight.model.Concert;
import com.jso.gigsight.utils.Repository;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.jso.gigsight.utils.Constants.DATE_INPUT;
import static com.jso.gigsight.utils.Constants.DATE_OUTPUT;
import static com.jso.gigsight.utils.Constants.LOCATION_REQUEST_INTERVAL;
import static com.jso.gigsight.utils.Constants.NULL_STRING;
import static com.jso.gigsight.utils.Constants.REQUEST_CHECK_SETTINGS;
import static com.jso.gigsight.utils.Constants.TBA_STRING;
import static com.jso.gigsight.utils.Constants.TBA_TIME;
import static com.jso.gigsight.utils.Constants.TRACKING_LOCATION_KEY;
import static com.jso.gigsight.utils.NetworkUtils.OnGetConcertsCallback;
import static com.jso.gigsight.utils.NetworkUtils.calculateNoOfColumns;

public class WidgetActivity extends AppCompatActivity implements WidgetAdapter.WidgetClickHandler {

    private ActivityWidgetBinding widgetBinding;
    private Repository repository;
    private WidgetAdapter adapter;
    private boolean isFetchingConcerts;
    private int currentPage = 1;
    private GridLayoutManager layoutManager;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private boolean isTrackingLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        widgetBinding = DataBindingUtil.setContentView(this, R.layout.activity_widget);
        repository = Repository.getInstance();

        handlePermissions();
        startTrackingLocation();

        if (savedInstanceState != null) {
            isTrackingLocation = savedInstanceState.getBoolean(TRACKING_LOCATION_KEY);
        }

        int noOfColumns = calculateNoOfColumns(this);
        layoutManager = new GridLayoutManager(this, noOfColumns);
        widgetBinding.widgetRecyclerView.setLayoutManager(layoutManager);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    private void loadConcerts(int page, final Location location) {
        isFetchingConcerts = true;
        repository.getConcerts(page, location.getLatitude(),
                location.getLongitude(), new OnGetConcertsCallback() {
                    @Override
                    public void onSuccess(int page, List<Concert> concerts) {
                        if (adapter == null) {
                            adapter = new WidgetAdapter(WidgetActivity.this,
                                    concerts, WidgetActivity.this);
                            widgetBinding.widgetRecyclerView.setAdapter(adapter);
                        } else {
                            if (page == 1) adapter.clearConcerts();
                            adapter.appendConcerts(concerts);
                        }
                        currentPage = page;
                        isFetchingConcerts = false;
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(WidgetActivity.this,
                                getString(R.string.error_message),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        widgetBinding.widgetRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int totalItemCount = layoutManager.getItemCount();
                int visibleItemCount = layoutManager.getChildCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (firstVisibleItemPosition + visibleItemCount > totalItemCount / 2) {
                    if (!isFetchingConcerts) {
                        loadConcerts(currentPage + 1, location);
                    }
                }
            }
        });
    }

    private void loadAll(int page) {
        isFetchingConcerts = true;
        repository.getAll(page, new OnGetConcertsCallback() {
            @Override
            public void onSuccess(int page, List<Concert> concerts) {
                if (adapter == null) {
                    adapter = new WidgetAdapter(WidgetActivity.this,
                            concerts, WidgetActivity.this);
                    widgetBinding.widgetRecyclerView.setAdapter(adapter);
                } else {
                    if (page == 1) adapter.clearConcerts();
                    adapter.appendConcerts(concerts);
                }
                currentPage = page;
                isFetchingConcerts = false;
            }

            @Override
            public void onError() {
                Toast.makeText(WidgetActivity.this,
                        getString(R.string.error_message),
                        Toast.LENGTH_SHORT).show();
            }
        });

        widgetBinding.widgetRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int totalItemCount = layoutManager.getItemCount();
                int visibleItemCount = layoutManager.getChildCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (firstVisibleItemPosition + visibleItemCount > totalItemCount / 2) {
                    if (!isFetchingConcerts) {
                        loadAll(currentPage + 1);
                    }
                }
            }
        });
    }

    private void startTrackingLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (isTrackingLocation) {
                    loadConcerts(currentPage, locationResult.getLastLocation());
                }
            }
        };

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        fusedLocationClient.requestLocationUpdates(locationRequest,
                                locationCallback, Looper.myLooper());
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException rae = (ResolvableApiException) e;
                            rae.startResolutionForResult(WidgetActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    private void handlePermissions() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        isTrackingLocation = true;
                        startTrackingLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        isTrackingLocation = false;
                        loadAll(currentPage);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;

                    case Activity.RESULT_CANCELED:
                        isTrackingLocation = false;
                        loadAll(currentPage);
                        break;
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TRACKING_LOCATION_KEY, isTrackingLocation);
    }

    @Override
    public void onClick(Concert concert) {
        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.concert_widget);
        views.setTextViewText(R.id.widgetTitleTextView, concert.getTitle());

        try {
            String dateTimeLocal = concert.getDateTime().replace("T", " ");
            DateFormat dateInput = new SimpleDateFormat(DATE_INPUT, Locale.getDefault());
            DateFormat dateOutput = new SimpleDateFormat(DATE_OUTPUT, Locale.getDefault());

            Date dateObject = dateInput.parse(dateTimeLocal);
            String formattedDate = dateOutput.format(dateObject);

            if (formattedDate.contains(TBA_TIME)) {
                String formattedDateWithTba = formattedDate.replace(TBA_TIME, TBA_STRING);
                views.setTextViewText(R.id.widgetDateTimeTextView, formattedDateWithTba);
            } else {
                views.setTextViewText(R.id.widgetDateTimeTextView, formattedDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String venue = concert.getVenue().getName();
        if (venue == null || venue.equals(NULL_STRING)) {
            views.setTextViewText(R.id.widgetVenueTextView, TBA_STRING);
        } else {
            views.setTextViewText(R.id.widgetVenueTextView, venue);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
