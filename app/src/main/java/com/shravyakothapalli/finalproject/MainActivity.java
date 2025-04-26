package com.shravyakothapalli.finalproject;

import static java.lang.Math.min;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.slider.RangeSlider;

public class MainActivity extends AppCompatActivity {

    EditText allergies;
    EditText likes;
    EditText dislikes;
    RangeSlider slider;
    float minPriceLevel, maxPriceLevel;
    DatabaseHelper databaseHelper;
    int radiusValue = 5;
    EditText radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        allergies = findViewById(R.id.allergies);
        likes = findViewById(R.id.likes);
        dislikes = findViewById(R.id.dislikes);
        radius = findViewById(R.id.radius);
        slider = findViewById(R.id.slider);
        slider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                minPriceLevel = slider.getValues().get(0);
                maxPriceLevel = slider.getValues().get(1);

            }
        });
        databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getData();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                allergies.setText(cursor.getString(0));
                likes.setText(cursor.getString(1));
                dislikes.setText(cursor.getString(2));
                radius.setText(cursor.getString(3));
                slider.setValues(Float.parseFloat(cursor.getString(4)), Float.parseFloat(cursor.getString(5)));
            }
        }
    }

    public void find(View view) {
        if(radius.getText().toString().length() > 0) {
            radiusValue = Integer.parseInt(radius.getText().toString());
        }
        databaseHelper.insertUserPerferences(allergies.getText().toString(), likes.getText().toString(), dislikes.getText().toString(), radiusValue, minPriceLevel, maxPriceLevel);
        startActivity(new Intent(MainActivity.this, MapsActivity.class));
    }
}