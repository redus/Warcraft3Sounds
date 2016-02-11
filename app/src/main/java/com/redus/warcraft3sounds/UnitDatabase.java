package com.redus.warcraft3sounds;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;

/**
 * Created by redus on 2015-11-09.
 * Receives unitId (index of the unit grid), and
 * returns unit name, or R.drawable.id for unit ability image name.
 */
public class UnitDatabase {

    private static UnitDatabase instance;
    private SQLiteDatabase unit_db;

    private UnitDatabase(){
    }

    public static UnitDatabase getInstance(){
        if (instance == null){
            instance = new UnitDatabase();
        }
        return instance;
    }

    public void initDatabase(Context context){
        try{
            DataBaseHelper dbHelper = new DataBaseHelper(context);
            unit_db = dbHelper.opendatabase(SQLiteDatabase.OPEN_READONLY);
        } catch(SQLiteException e){
            Log.e(this.getClass().toString(), "Error while opening db");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getCount(){
        String select = "SELECT name FROM unit_db";
        Cursor c = unit_db.rawQuery(select, new String[]{});
        int total = c.getCount();
        c.close();
        return total;
    }

    public synchronized void closeDatabase(){
        unit_db.close();
    }

    // helper for retrieving image
    private Bitmap getImage(int unitid, String colName){
        Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        String select = String.format("SELECT %s FROM unit_db WHERE id=%d", colName, unitid);

        Cursor c = unit_db.rawQuery(select, new String[]{});
        if(c.moveToFirst()){
            byte[] image = c.getBlob(c.getColumnIndex(colName));
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        }
        c.close();
        return bitmap;
    }
    public Bitmap getUnitImage(int unitid) {
        return getImage(unitid, "f_image");
    }

    public Bitmap getAbilityImage(int unitid){
        return getImage(unitid, "s_image");
    }

    private String getText(int unitid, String colName){
        String result = "";
        String select = String.format("SELECT %s FROM unit_db WHERE id=%d", colName, unitid);

        Cursor c = unit_db.rawQuery(select, new String[]{});
        if (c.moveToFirst()){
            result = c.getString(c.getColumnIndex(colName));
        }
        c.close();
        return result;
    }
    public String getName(int unitid){
        return getText(unitid, "name");
    }

    public String[] getSounds(int unitid){
        return getText(unitid, "sounds").split("\\|");
    }


    public String getRace(int unitId) {
        return getText(unitId, "race");
    }


}
