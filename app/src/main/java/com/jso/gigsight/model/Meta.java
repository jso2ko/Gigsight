package com.jso.gigsight.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Meta {

    @SerializedName("page")
    @Expose
    private int page;

    public Meta() {
        //Required empty constructor
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}