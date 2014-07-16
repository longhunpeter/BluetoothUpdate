package com.chronocloud.update.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chronocloud.update.db.ClientDBHelper;
import com.chronocloud.update.db.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lxl on 14-3-12.
 */
public class UploadInfo {

    public static final Table table = new Table("upload_buffer",
            UploadField.values(), UploadField.LocalModifyTime);

//    public String sign;

    public String dimensionalCode;

    /**
     *缓存存入
     * @param dimensionalCode
     * @param dbHelper
     */
    public static void bufferInsert(String dimensionalCode, ClientDBHelper dbHelper) {
        ContentValues values = new ContentValues();
        values.put(UploadField.dimensionalCode.name(), dimensionalCode);
        try {
            table.insert(values, dbHelper);
        } finally {
            final SQLiteDatabase db = dbHelper.getWritableDatabase();
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * 查询缓存
     *
     * @param dbHelper
     * @return
     */
    public static List<UploadInfo> queryBuffer(ClientDBHelper dbHelper) {

        Cursor c = null;
        List<UploadInfo> bufferList = new ArrayList<UploadInfo>();
        try {
            c = table.query(null, null, null, null, dbHelper);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                UploadInfo mImageInfo = bufferByCursor(c);
                bufferList.add(mImageInfo);
                c.moveToNext();
            }
            return bufferList;
        } finally {
            if (c != null)
                c.close();
            final SQLiteDatabase db = dbHelper.getWritableDatabase();
            if (db.isOpen()) {
                db.close();
            }
        }

    }

    /**
     * 删除缓存
     *
     * @param dbHelper
     */
    public static void deleteBuffer(String bufferDimensionalCode,ClientDBHelper dbHelper) {
        table.delete("dimensionalCode = ?",new String[]{bufferDimensionalCode} , dbHelper);

    }

    public static UploadInfo bufferByCursor(Cursor cursor) {
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.dimensionalCode = cursor.getString(UploadField.dimensionalCode.order());

        return uploadInfo;
    }
}
