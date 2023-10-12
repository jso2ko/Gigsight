package com.jso.gigsight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.jso.gigsight.adapter.ConcertAdapter;
import com.jso.gigsight.databinding.ActivityMainBinding;
import com.jso.gigsight.model.Concert;
import com.jso.gigsight.utils.Repository;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.parceler.Parcels;

import java.util.List;

import static com.jso.gigsight.utils.Constants.CONCERT_KEY;
import static com.jso.gigsight.utils.Constants.LOCATION_REQUEST_INTERVAL;
import static com.jso.gigsight.utils.Constants.REQUEST_CHECK_SETTINGS;
import static com.jso.gigsight.utils.Constants.STATE_KEY;
import static com.jso.gigsight.utils.Constants.TRACKING_LOCATION_KEY;
import static com.jso.gigsight.utils.NetworkUtils.OnGetConcertsCallback;
import static com.jso.gigsight.utils.NetworkUtils.calculateNoOfColumns;

public class MainActivity extends AppCompatActivity implements ConcertAdapter.ConcertClickHandler {

    private ActivityMainBinding mainBinding;
    private GridLayoutManager layoutManager;
    private Repository repository;
    private ConcertAdapter adapter;
    private boolean isFetchingConcerts;
    private int currentPage = 1;
    private boolean isTrackingLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Parcelable recyclerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mainBinding.toolbar);
        repository = Repository.getInstance();

        int noOfColumns = calculateNoOfColumns(this);
        layoutManager = new GridLayoutManager(this, noOfColumns);
        mainBinding.recyclerView.setLayoutManager(layoutManager);

        handlePermissions();
    }

    private void loadConcerts(int page, final Location location) {
        isFetchingConcerts = true;
        repository.getConcerts(page, location.getLatitude(), location.getLongitude(), new OnGetConcertsCallback() {
            @Override
            public void onSuccess(int page, List<Concert> concerts) {
                if (adapter == null) {
                    adapter = new ConcertAdapter(MainActivity.this,
                            concerts, MainActivity.this);
                    mainBinding.recyclerView.setAdapter(adapter);
                    if (recyclerState != null) {
                        mainBinding.recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
                    }
                } else {
                    adapter.appendConcerts(concerts);
                    if (recyclerState != null) {
                        mainBinding.recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
                    }
                }

                currentPage = page;
                isFetchingConcerts = false;
                mainBinding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this,
                        getString(R.string.error_message),
                        Toast.LENGTH_SHORT).show();
            }
        });

        mainBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    adapter = new ConcertAdapter(MainActivity.this,
                            concerts, MainActivity.this);
                    mainBinding.recyclerView.setAdapter(adapter);
                } else {
                    adapter.appendConcerts(concerts);
                }

                currentPage = page;
                isFetchingConcerts = false;

                mainBinding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this,
                        getString(R.string.error_message),
                        Toast.LENGTH_SHORT).show();
            }
        });

        mainBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        mainBinding.progressBar.setVisibility(View.VISIBLE);
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
                            rae.startResolutionForResult(MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                }
                mainBinding.progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void stopTrackingLocation() {
        if (isTrackingLocation) {
            isTrackingLocation = false;
        }
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
        recyclerState = mainBinding.recyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(STATE_KEY, recyclerState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            isTrackingLocation = savedInstanceState.getBoolean(TRACKING_LOCATION_KEY);
            recyclerState = savedInstanceState.getParcelable(STATE_KEY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTrackingLocation) {
            startTrackingLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTrackingLocation) {
            stopTrackingLocation();
            isTrackingLocation = true;
        }
    }

    @Override
    public void onClick(Concert concert) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(CONCERT_KEY, Parcels.wrap(concert));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.favorites) {
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
