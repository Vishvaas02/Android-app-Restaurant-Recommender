package com.shravyakothapalli.finalproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlacesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Adapter adapter;
    JSONArray places;
    Data[] locations;
    private AtomicInteger imagesDownloaded = new AtomicInteger(0);
    MediaType JSON = MediaType.get("application/json");
    OkHttpClient client = new OkHttpClient();
    Call currentCall;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_places);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Bundle bundle = getIntent().getExtras();
        try {
            places = new JSONArray(bundle.getString("places"));
            locations = new Data[places.length()];
            for (int i = 0; i < places.length(); i++) {
                int placeID = i;
                ImageDownloader downloader = new ImageDownloader();
                downloader.downloadImage(places.getJSONObject(i).getJSONArray("photos").getJSONObject(0).getString("photo_reference"), new ImageDownloadCallback() {
                    @Override
                    public void onImageDownloaded(Bitmap image) throws JSONException {
                        locations[placeID] = new Data(places.getJSONObject(placeID).getString("name"), places.getJSONObject(placeID).getString("vicinity"), image, Float.valueOf(places.getJSONObject(placeID).getString("rating")), places.getJSONObject(placeID).getString("place_id"), places.getJSONObject(placeID).getJSONObject("geometry").getJSONObject("location").getString("lat"), places.getJSONObject(placeID).getJSONObject("geometry").getJSONObject("location").getString("lng"));
                        if (imagesDownloaded.incrementAndGet() == places.length()) {
                            adapter = new Adapter(locations, PlacesActivity.this);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void goBack(View view) {
        startActivity(new Intent(PlacesActivity.this, MapsActivity.class));
    }

    public void askChatGPT(View view) throws JSONException, IOException {
        callAPI();
    }

    public void callAPI() throws IOException, JSONException {
        Request request;
        final String[] content = {""};
        JSONObject jsonBody = new JSONObject();
        JSONObject temp = new JSONObject();
        temp.put("role", "user");
        temp.put("content", "Choose a random restaurant for the user based on what you know about them. " + getStorage() + "Return the place id, latitude, longitude and rating in a json array. This is the list of places: " + places + ". Send only the place ID value no other characters.");
        JSONArray tempArray = new JSONArray();
        tempArray.put(temp);
        jsonBody.put("model", "gpt-4o");
        jsonBody.put("max_tokens", 4000);
        jsonBody.put("temperature", 0);
        jsonBody.put("messages", tempArray);
        RequestBody requestBody = RequestBody.create(jsonBody.toString(),JSON);
        .header("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY)        currentCall = client.newCall(request);
        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ContextCompat.getMainExecutor(PlacesActivity.this).execute(()  -> {
                    Toast.makeText(PlacesActivity.this, "Failed to load response. Please try again!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("choices");
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        JSONObject messageObject = jsonObject.getJSONObject("message");
                        content[0] = messageObject.getString("content");
                        Log.i("placeid", content[0]);
                        showDetails(content[0]);
//                        getPlaceDetails(content[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ContextCompat.getMainExecutor(PlacesActivity.this).execute(()  -> {
                        Toast.makeText(PlacesActivity.this, "Failed to load response. Please try again!", Toast.LENGTH_SHORT).show();
                    });
                }

            }
        });
    }

    public String getStorage() {
        String data = "";
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getData();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                data = "Allergies: " + cursor.getString(0) + ", Likes: " + cursor.getString(1) + ", Dislikes: " + cursor.getString(2) + ". ";
            }
        }
        return data;
    }

    public void showDetails(String placeID) throws JSONException {
        for (int i = 0 ; i < places.length(); i++) {
            JSONObject obj = places.getJSONObject(i);
            if (obj.getString("place_id").equals(placeID)) {
                Log.i("lat", obj.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                runOnUiThread(() -> {
                    try {
                        new AlertDialog.Builder(this)
                                .setTitle(obj.getString("name"))
                                .setMessage("Location: " + obj.getString("vicinity") + "\nRating: " + obj.getString("rating") + "/5")
                                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            Intent intent = new Intent(PlacesActivity.this, MapsActivity.class);
                                            intent.putExtra("Place ID", placeID);
                                            intent.putExtra("Lat", obj.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                                            intent.putExtra("Lon", obj.getJSONObject("geometry").getJSONObject("location").getString("lng"));
                                            intent.putExtra("Place Name", obj.getString("name"));
                                            startActivity(intent);
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}