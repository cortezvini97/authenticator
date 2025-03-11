package com.vcinsidedigital.authenticator.helper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vcinsidedigital.authenticator.model.Secret;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecretDAO implements ISecretDAO
{
    private SQLiteDatabase write;
    private SQLiteDatabase read;

    public SecretDAO(Context context)
    {
        DBHelper db = new DBHelper(context);
        this.write = db.getWritableDatabase();
        this.read = db.getReadableDatabase();
    }

    @Override
    public boolean save(Secret secret)
    {
        ContentValues cv = new ContentValues();
        cv.put("name", secret.getName());
        cv.put("code", secret.getCode());
        cv.put("type", secret.getType());
        try {
            write.insert(DBHelper.TABLE_SECRET, null, cv);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean update(Secret secret) {
        ContentValues cv = new ContentValues();
        cv.put("name", secret.getName());
        cv.put("code", secret.getCode());
        cv.put("type", secret.getType());

        String[] args = {secret.getId().toString()};

        try {
            write.update(DBHelper.TABLE_SECRET, cv, "id=?", args);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean delete(Secret secret) {
        String[] args = {secret.getId().toString()};
        try {
            write.delete(DBHelper.TABLE_SECRET, "id=?", args);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Secret> listAll() {
        List<Secret> secrets = new ArrayList<>();
        String sql = "SELECT * FROM " + DBHelper.TABLE_SECRET + " ;";
        Cursor c = read.rawQuery(sql, null);
        while (c.moveToNext()){
            @SuppressLint("Range") Long id = c.getLong(c.getColumnIndex("id"));
            @SuppressLint("Range") String name = c.getString(c.getColumnIndex("name"));
            @SuppressLint("Range") String secret = c.getString(c.getColumnIndex("code"));
            @SuppressLint("range") String type = c.getString(c.getColumnIndex("type"));

            Secret s = new Secret();
            s.setId(id);
            s.setName(name);
            s.setCode(secret);
            s.setType(type);
            secrets.add(s);

        }
        return secrets;
    }

    public static boolean deleteDatabse(Context context){
        try {
            context.deleteDatabase(DBHelper.NAME_DB);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
