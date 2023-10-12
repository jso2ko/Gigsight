package com.jso.gigsight.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Performer {

    @SerializedName("image")
    @Expose
    private String image;

    public Performer() {
        //Required empty constructor
    }

    public Performer(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}