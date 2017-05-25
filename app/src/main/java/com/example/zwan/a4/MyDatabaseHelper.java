package com.example.zwan.a4;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sersh on 2017/2/13.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_NOTE = "create table message ("
            + "id integer primary key, "
            + "content text, "
            + "picture text, "
            + "type integer, " //0 received 1 sent
            + "time integer)";

    private Context mContext;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_NOTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }

}
