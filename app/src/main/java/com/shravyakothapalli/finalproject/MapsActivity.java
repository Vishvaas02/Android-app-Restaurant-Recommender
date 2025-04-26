package com.shravyakothapalli.finalproject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.shravyakothapalli.finalproject.databinding.ActivityMapsBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener{

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;
    LatLng userLocation;
    Call currentCall;
    String placeID = "", placeLat = "", placeLon = "", placeName = "";
    GeoApiContext geoApiContext = null;
    Boolean showNewMap = true;
    ArrayList<PolylineData> polylineData = new ArrayList<>();
    SupportMapFragment mapFragment;
    String priceLevel, radius = "5", allergies = "", cusinies = "", likes = "", dislikes = "";
    DatabaseHelper databaseHelper;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getData();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                allergies = cursor.getString(0);
                likes = cursor.getString(1);
                dislikes = cursor.getString(2);
                radius = cursor.getString(3);
                priceLevel = cursor.getString(4);
            }
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            placeID = extras.getString("Place ID");
            placeLat = extras.getString("Lat");
            placeLon = extras.getString("Lon");
            placeName = extras.getString("Place Name");
        }
        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder().apiKey("").build();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnPolylineClickListener(this);
        try {
            getUserLocation();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void getUserLocation () throws IOException, JSONException {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                try {
                    showUserLocationOnMap(location);
                } catch (IOException | JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            showUserLocationOnMap(lastKnownLocation);
            mMap.setMyLocationEnabled(true);
        }
    }

    public void showUserLocationOnMap(Location location) throws IOException, JSONException {
        mMap.clear();
        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions marker = new MarkerOptions().position(userLocation).title("This is your location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user));
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
        if(showNewMap) {
            if (placeID.length() > 0) {
                getDirections(marker);
            }
            showNewMap = false;
        }
    }

    public void find(View view) throws JSONException, IOException {
        getURL();
    }

    public void askChatGPT(View view) {
        String prompt = "What we know about the user: { Allergies: " + allergies + ", Favorite Cusinies: " + cusinies + ", Likes: " + likes + ", Dislikes: " + dislikes + " }";
        Intent intent = new Intent(MapsActivity.this, ChatGPTActivity.class);
        intent.putExtra("Prompt", prompt);
        startActivity(intent);
    }

    public void filter(View view) {
        startActivity(new Intent(MapsActivity.this, MainActivity.class));
    }

    public void getURL() throws IOException, JSONException {
        StringBuilder placesURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        placesURL.append("location=" + userLocation.latitude + "," + userLocation.longitude);
        placesURL.append("&radius=" + 1609 * Integer.parseInt(radius));
        placesURL.append("&type=restaurant");
        placesURL.append("&sensor=true");
//        if(priceLevel.length() > 0)
//            placesURL.append("&maxprice=" + Integer.parseInt(priceLevel));
        placesURL.append("&key=" + "");
        Log.i("Places", placesURL.toString());
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(placesURL.toString())
                .build();
        currentCall = client.newCall(request);
        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ContextCompat.getMainExecutor(MapsActivity.this).execute(()  -> {
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    try {
                        Log.i("Place", String.valueOf(response.isSuccessful()));
                        JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("results");
                        Log.i("Places", response.toString());
                        runOnUiThread(() -> {
                            Intent i = new Intent(MapsActivity.this, PlacesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("places", jsonArray.toString());
                            i.putExtras(bundle);
                            startActivity(i);
                        });
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    ContextCompat.getMainExecutor(MapsActivity.this).execute(()  -> {

                    });
                }

            }
        });
    }

    public void getDirections(MarkerOptions marker) throws IOException, JSONException {
        StringBuilder placesURL = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        placesURL.append("origin=" + userLocation.latitude + "," + userLocation.longitude);
        placesURL.append("&destination=place_id:" + placeID);
        placesURL.append("&key=" + "");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(placesURL.toString())
                .build();
        currentCall = client.newCall(request);
        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ContextCompat.getMainExecutor(MapsActivity.this).execute(()  -> {
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                        runOnUiThread(() -> {
                            calculateDirections(marker);
                        });
                } else {
                    ContextCompat.getMainExecutor(MapsActivity.this).execute(()  -> {

                    });
                }

            }
        });
    }

    public void calculateDirections(MarkerOptions marker){
        LatLng location = new LatLng(Double.valueOf(placeLat), Double.valueOf(placeLon));
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                location.latitude,
                location.longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        userLocation.latitude,
                        userLocation.longitude
                )
        );
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
            }
        });
    }

    public void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(polylineData.size() > 0) {
                    for(PolylineData polylineData1: polylineData) {
                        polylineData1.getPolyline().remove();
                    }
                    polylineData.clear();
                    polylineData = new ArrayList<>();
                }
                for(DirectionsRoute route: result.routes){
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> newDecodedPath = new ArrayList<>();
                    for(com.google.maps.model.LatLng latLng: decodedPath){
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(MapsActivity.this, R.color.grey));
                    polyline.setClickable(true);
                    polylineData.add(new PolylineData(polyline, route.legs[0]));
                    LatLng location = new LatLng(Double.valueOf(placeLat), Double.valueOf(placeLon));
                    LatLng endLocation = new LatLng(
                            location.latitude,
                            location.longitude
                    );
                    mMap.addMarker(new MarkerOptions().position(endLocation).title(placeName));
                }
            }
        });
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {
        for(PolylineData polylineData: polylineData){
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.blue));
                polylineData.getPolyline().setZIndex(1);
                LatLng location = new LatLng(Double.valueOf(placeLat), Double.valueOf(placeLon));
                LatLng endLocation = new LatLng(
                        location.latitude,
                        location.longitude
                );
                Marker marker = mMap.addMarker(new MarkerOptions().position(endLocation).title(placeName).snippet("Duration:" + polylineData.getLeg().duration));
                marker.showInfoWindow();
              CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(userLocation.latitude, userLocation.longitude))
                    .zoom(15)
                    .tilt(45)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                new AlertDialog.Builder(this)
                        .setTitle("Start Trip")
                        .setMessage("Duration: " + polylineData.getLeg().duration)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                builder.setMessage("Open Google Maps?")
                                        .setCancelable(true)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                                String latitude = String.valueOf(marker.getPosition().latitude);
                                                String longitude = String.valueOf(marker.getPosition().longitude);
                                                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                mapIntent.setPackage("com.google.android.apps.maps");

                                                try{
                                                    if (mapIntent.resolveActivity(MapsActivity.this.getPackageManager()) != null) {
                                                        startActivity(mapIntent);
                                                    }
                                                }catch (NullPointerException e){
                                                    Toast.makeText(MapsActivity.this, "Couldn't open map", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                                dialog.cancel();
                                            }
                                        });
                                final AlertDialog alert = builder.create();
                                alert.show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.grey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }
}