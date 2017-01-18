package com.kovtun.producdetector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by kovtun on 29.07.2016.
 */
public class SQLiteDB {
    private static SQLiteDatabase database;
    private static SQLiteDBHelper connectDBOpenHelper;

    public  SQLiteDB(Context context)
    {
        if(this.connectDBOpenHelper == null) {
            this.connectDBOpenHelper = new SQLiteDBHelper(context);
            this.database = connectDBOpenHelper.getWritableDatabase();
        }
    }
    public void beginTransaction()
    {
        this.database.beginTransaction();
    }
    public void endTransaction()
    {
        this.database.endTransaction();
    }
    public void endTransactionSuccessful()
    {
        this.database.setTransactionSuccessful();
        this.database.endTransaction();
    }
    public void connectClose()
    {
        this.database.close();
        //this.connectDBOpenHelper.close();
    }
    public Cursor Query(String sql)
    {
        return database.rawQuery(sql,null);
    }
    public void executeSQL(String sql)
    {
        database.execSQL(sql);
    }
    public void insertData(String table_name, ContentValues contentValues)
    {
        database.insert(table_name, null, contentValues);
    }

}
