package com.vcinsidedigital.authenticator.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper
{
    public static String NAME_DB = "DB_Authenticator";
    public static int VERSION = 1;
    public static String TABLE_SECRET = "secret";

    public DBHelper(@Nullable Context context)
    {
        super(context, NAME_DB, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_SECRET
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " name TEXT NOT NULL, "
                + " code TEXT NOT NULL, "
                + " type TEXT NOT NULL); ";
        try {
            db.execSQL(sql);
            Log.i("DB_HELPER", "onCreate: Creating table " + TABLE_SECRET);
        }catch (Exception e){
            Log.e("DB_HELPER", "onCreate: Error creating table " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_SECRET + " ;";
        try {
            db.execSQL(sql);
            onCreate(db);
            Log.i("DB_HELPER", "onUpgrade: update table " + TABLE_SECRET);
        }catch (Exception e){
            Log.e("DB_HELPER", "onUpgrade: error update table " + e.getMessage());
        }
    }
}
