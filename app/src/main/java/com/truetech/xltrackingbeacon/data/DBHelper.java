package com.truetech.xltrackingbeacon.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.truetech.xltrackingbeacon.Utils.Constant.*;
import static com.truetech.xltrackingbeacon.Utils.Util.getContext;


public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper sInstance;

    public static synchronized DBHelper getInstance() {
    if (sInstance == null) {
        sInstance = new DBHelper(getContext(), NAME_DB, null, VERSION_DB);
    }
    return sInstance;
}

    private DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String sql="create table " + NAME_TABLE_LOC
                    + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_DATA + " blob,"
                    + COL_DATE_INSERT + " integer);";
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.e(TAG,"create table "+NAME_TABLE_LOC,e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
