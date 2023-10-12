package com.jso.gigsight.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Venue {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("address")
    @Expose
    private String address;

    @SerializedName("extended_address")
    @Expose
    private String extendedAddress;

    @SerializedName("location")
    @Expose
    private Location location;

    public Venue() {
        //Required empty constructor
    }

    public Venue(String name, String address, String extendedAddress) {
        this.name = name;
        this.address = address;
        this.extendedAddress = extendedAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getExtendedAddress() {
        return extendedAddress;
    }

    public void setExtendedAddress(String extendedAddress) {
        this.extendedAddress = extendedAddress;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    //Location class
    @Parcel
    public static class Location {
        @SerializedName("lat")
        @Expose
        private double lat;

        @SerializedName("lon")
        @Expose
        private double lon;

        public Location() {
            //Required empty constuctor
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }
    }
}
