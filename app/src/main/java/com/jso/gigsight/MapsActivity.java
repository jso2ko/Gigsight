package com.jso.gigsight;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.jso.gigsight.model.Venue;

import org.parceler.Parcels;

import static com.jso.gigsight.utils.Constants.ANIMATION_DURATION;
import static com.jso.gigsight.utils.Constants.VENUE_KEY;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Venue currentVenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Fade fade = new Fade();
        fade.setDuration(ANIMATION_DURATION);
        getWindow().setEnterTransition(fade);

        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.BOTTOM);
        slide.setDuration(ANIMATION_DURATION);
        getWindow().setReturnTransition(slide);

        currentVenue = Parcels.unwrap(getIntent().getParcelableExtra(VENUE_KEY));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        String address = currentVenue.getAddress();
        String extendedAddress = currentVenue.getExtendedAddress();
        double latitude = currentVenue.getLocation().getLat();
        double longitude = currentVenue.getLocation().getLon();

        LatLng concertLocation = new LatLng(latitude, longitude);

        googleMap.addMarker(new MarkerOptions().position(concertLocation)
                .title(address + "\n" + extendedAddress)).showInfoWindow();

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(concertLocation));
        googleMap.setMinZoomPreference(10);
        googleMap.setMaxZoomPreference(20);

        setPoiClick(googleMap);
    }

    private void setPoiClick(final GoogleMap googleMap) {
        googleMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest poi) {
                googleMap.addMarker(new MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name)).showInfoWindow();
            }
        });
    }
}
