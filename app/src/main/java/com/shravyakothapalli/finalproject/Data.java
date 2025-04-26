package com.shravyakothapalli.finalproject;

import android.graphics.Bitmap;

public class Data {
    public String placeName;
    public String placeDesc;
    public Bitmap placeImage;
    public Float placeRating;
    public String placeID;
    String lat, lon;

    public Data(String placeName, String placeDesc, Bitmap placeImage, Float placeRating, String placeID, String lat, String lon) {
        this.placeName = placeName;
        this.placeDesc = placeDesc;
        this.placeImage = placeImage;
        this.placeRating = placeRating;
        this.placeID = placeID;
        this.lat = lat;
        this.lon = lon;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getPlaceDesc() {
        return placeDesc;
    }

    public Bitmap getPlaceImage() {
        return placeImage;
    }

    public Float getPlaceRating() {
        return placeRating;
    }

    public String getPlaceID() {
        return placeID;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

}
