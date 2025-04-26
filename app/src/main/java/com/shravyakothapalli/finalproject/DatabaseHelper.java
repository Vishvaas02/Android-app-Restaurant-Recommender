package com.shravyakothapalli.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, "User.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE user_details(allergies TEXT, likes TEXT, dislikes TEXT, radius INTEGER, min_price_level INTEGER, max_price_level INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user_details");
    }

    public Cursor getData() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM user_details", null);
        return cursor;
    }

    public Boolean insertUserPerferences(String allergies, String likes, String dislikes, int radius, float minPriceLevel, float maxPriceLevel) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("allergies", allergies);
        contentValues.put("likes", likes);
        contentValues.put("dislikes", dislikes);
        contentValues.put("radius", radius);
        contentValues.put("min_price_level", minPriceLevel);
        contentValues.put("max_price_level", maxPriceLevel);
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM user_details", null);
        if(cursor.getCount() > 0) {
            long result = sqLiteDatabase.update("user_details", contentValues, "rowid=(SELECT MIN(rowid) FROM user_details)", null);
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            sqLiteDatabase.insert("user_details", null, contentValues);
            return true;
        }
    }
}
