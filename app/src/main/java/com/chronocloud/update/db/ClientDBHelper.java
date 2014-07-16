package com.chronocloud.update.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chronocloud.update.data.UploadInfo;

/**
 * Created with IntelliJ IDEA.
 * User: l
 * Date: 13-10-16
 * Time: 上午10:49
 * To change this template use File | Settings | File Templates.
 */
public class ClientDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tft_database";
    private static final int DATABASE_VERSION = 1;

    public ClientDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //To change body of implemented methods use File | Settings | File Templates.
        UploadInfo.table.dropTable(db);
        UploadInfo.table.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //To change body of implemented methods use File | Settings | File Templates.
        // 更新表
        UploadInfo.table.dropTable(db);
        UploadInfo.table.createTable(db);
    }
}
