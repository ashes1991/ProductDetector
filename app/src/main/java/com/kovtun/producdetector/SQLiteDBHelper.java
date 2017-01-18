package com.kovtun.producdetector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

/**
 * Created by kovtun on 29.07.2016.
 */
public class SQLiteDBHelper  extends SQLiteOpenHelper{
    private final static int version = 1;
    private static String DB_NAME = null;
    private final String createTable="create table EventLog (id integer primary key autoincrement," +
            "event text," +
            "product_id integer," +
            "name text," +
            "barcode text," +
            "price double," +
            "read_date datetime);";
    private static String getDB_NAME()
    {
        if(DB_NAME == null) {
            File sdPath = Environment.getExternalStorageDirectory();
            sdPath = new File(sdPath.getAbsolutePath() + "/ProductDetector");
            sdPath.mkdirs();
            DB_NAME = sdPath.getAbsolutePath() + "/log.db";
        }
        return DB_NAME;
    }
    public SQLiteDBHelper(Context context) {
        super(context, getDB_NAME(), null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        db.execSQL(createTable);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
