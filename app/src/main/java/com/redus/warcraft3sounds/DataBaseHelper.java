package com.redus.warcraft3sounds;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseHelper extends SQLiteOpenHelper {
    private Context myContext;

    private static final String DB_NAME = "unit.db";
    private static final String TAG = "DatabaseHelper";
    private String db_path;

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public DataBaseHelper(Context context) throws IOException {
        super(context,DB_NAME,null,1);
        this.myContext = context;
        this.db_path = context.getFilesDir().getPath() + File.separator + DB_NAME;

        if (!databaseExists()) {
            Log.d(TAG, "Database doesn't exist. Creating.");
            createDatabase();
        }
    }

    public void createDatabase() throws IOException {
        if (!databaseExists()) {
            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch(IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean databaseExists() {
        File dbfile = new File(db_path);
        return dbfile.exists();
    }

    private void copyDatabase() throws IOException {
        //Open your local db as the input stream
        InputStream myinput = myContext.getAssets().open(DB_NAME);

        //Open the empty db as the output stream
        OutputStream myoutput = new FileOutputStream(db_path);

        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myinput.read(buffer))>0) {
            myoutput.write(buffer,0,length);
        }

        //Close the streams
        myoutput.flush();
        myoutput.close();
        myinput.close();
    }

    public SQLiteDatabase opendatabase(int option) throws SQLiteException {
        //the class that called openDatabase needs to close it.
        return SQLiteDatabase.openDatabase(db_path, null, option);
    }


}