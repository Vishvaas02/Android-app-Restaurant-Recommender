package com.shravyakothapalli.finalproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

interface ImageDownloadCallback {
    void onImageDownloaded(Bitmap image) throws JSONException;
}

public class ImageDownloader {

    public void downloadImage(final String photoReference, final ImageDownloadCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String imageUrl = getURL(photoReference);
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    final Bitmap image = BitmapFactory.decodeStream(input);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                try {
                                    callback.onImageDownloaded(image);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String getURL(String photoReference) {
        StringBuilder imageURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
        imageURL.append("photo_reference=" + photoReference);
        imageURL.append("&maxheight=" + 1000);
        imageURL.append("&maxwidth=" + 1000);
        imageURL.append("&key=" + BuildConfig.GOOGLE_API_KEY);
        return imageURL.toString();
    }
}
