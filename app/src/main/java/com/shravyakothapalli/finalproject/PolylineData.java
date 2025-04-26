package com.shravyakothapalli.finalproject;

import com.google.android.gms.maps.model.Polyline;
import com.google.maps.model.DirectionsLeg;

public class PolylineData {

    private Polyline polyline;
    private DirectionsLeg leg;

    public PolylineData(Polyline polyline, DirectionsLeg leg) {
        this.polyline = polyline;
        this.leg = leg;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public DirectionsLeg getLeg() {
        return leg;
    }

    @Override
    public String toString() {
        return "PolylineData{" +
                "polyline=" + polyline +
                ", leg=" + leg +
                '}';
    }
}
