package com.jso.gigsight.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class Concert {

    @SerializedName("events")
    @Expose
    private List<Concert> concerts;

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("datetime_local")
    @Expose
    private String dateTime;

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("venue")
    @Expose
    private Venue venue;

    @SerializedName("performers")
    @Expose
    private List<Performer> performers = null;

    @SerializedName("meta")
    @Expose
    private Meta meta;

    public Concert() {
        //Required empty constructor
    }

    public Concert(int id, String title, String dateTime, Venue venue, List<Performer> performers) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.venue = venue;
        this.performers = performers;
    }

    public List<Concert> getConcerts() {
        return concerts;
    }

    public void setConcerts(List<Concert> concerts) {
        this.concerts = concerts;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public List<Performer> getPerformers() {
        return performers;
    }

    public void setPerformers(List<Performer> performers) {
        this.performers = performers;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
