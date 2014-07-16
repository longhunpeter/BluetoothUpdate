package com.chronocloud.update.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chronocloud.update.data.Field;

/**
 * Created with IntelliJ IDEA.
 * User: l
 * Date: 13-10-16
 * Time: 上午11:02
 * To change this template use File | Settings | File Templates.
 */
public class Table {


    private final String tableName;
    private final Field[] fields;
    private final Field lastModifyDate;
    private String[] columns = null;

    /**
     * 构造一个SQLite数据库的一张表.
     *
     * @param tableName      表名
     * @param fields         字段集合
     * @param lastModifyDate 最后修改
     */
    public Table(String tableName, Field[] fields, Field lastModifyDate) {
        this.tableName = tableName;
        this.fields = fields;
        this.lastModifyDate = lastModifyDate;
    }

    /**
     * 删除表
     *
     * @param db
     */
    public void dropTable(SQLiteDatabase db) {
        db.execSQL(sql2Drop());
    }

    /**
     * 创建表
     *
     * @param db
     */
    public void createTable(SQLiteDatabase db) {
        db.execSQL(sql2Create());
    }

    /**
     * 插入数据
     *
     * @param values
     * @param dbHelper
     * @return
     */
    public long insert(ContentValues values, ClientDBHelper dbHelper) {
        if (values.get(lastModifyDate.name()) == null) {
            values.put(lastModifyDate.name(), System.currentTimeMillis());
        }
        return dbHelper.getWritableDatabase().insert(tableName, null, values);
    }

    /**
     * 更新数据
     *
     * @param values
     * @param whereClause
     * @param whereArgs
     * @param dbHelper
     * @return
     */
    public int update(ContentValues values, String whereClause, String[] whereArgs,
                      ClientDBHelper dbHelper) {
        values.put(lastModifyDate.name(), System.currentTimeMillis());
        return dbHelper.getWritableDatabase().update(tableName, values, whereClause, whereArgs);
    }

    /**
     * 查询数据
     *
     * @param selection
     * @param selectionArgs
     * @param orderBy
     * @param dbHelper
     * @return
     */
    public Cursor query(String selection, String[] selectionArgs, Field orderBy,
                        ClientDBHelper dbHelper) {
        return dbHelper.getWritableDatabase().query(tableName(), columns(), selection,
                selectionArgs, null, null, orderBy == null ? null : orderBy.name());
    }

    /**
     * 查询数据
     *
     * @param selection
     * @param selectionArgs
     * @param orderBy
     * @param dbHelper
     * @return
     */
    public Cursor query(String selection, String[] selectionArgs, String orderBy,
                        ClientDBHelper dbHelper) {
        return dbHelper.getWritableDatabase().query(tableName(), columns(), selection,
                selectionArgs, null, null, orderBy == null ? null : orderBy);
    }


    /**
     * 查询数据
     *
     * @param selection
     * @param selectionArgs
     * @param orderBy
     * @param dbHelper
     * @param limit
     * @return
     */
    public Cursor query(String selection, String[] selectionArgs, String orderBy,
                        ClientDBHelper dbHelper, String limit) {
        return dbHelper.getWritableDatabase().query(tableName(), columns(), selection,
                selectionArgs, null, null, orderBy == null ? null : orderBy, limit);
    }

    /**
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param orderBy
     * @param dbHelper
     * @return
     */
    public Cursor query(String selection, String[] selectionArgs, String groupBy, String orderBy,
                        ClientDBHelper dbHelper) {
        return dbHelper.getWritableDatabase().query(tableName(), columns(), selection,
                selectionArgs, groupBy == null ? null : groupBy, null, orderBy == null ? null : orderBy);
    }

    /**
     * 查询数据
     *
     * @param selection
     * @param selectionArgs
     * @param orderBy
     * @param dbHelper
     * @param limit
     * @return
     */
    public Cursor query(String selection, String[] selectionArgs, Field orderBy,
                        ClientDBHelper dbHelper, String limit) {
        return dbHelper.getWritableDatabase().query(tableName(), columns(), selection,
                selectionArgs, null, null, orderBy == null ? null : orderBy.name(), limit);
    }

    /**
     * 删除数据(根据ID)
     *
     * @param idField
     * @param id
     * @param dbHelper
     */
    public void delete(Field idField, Long id, ClientDBHelper dbHelper) {
        String sql = "DELETE from " + tableName() + " where " + idField.name() + " = " + id;
        dbHelper.getWritableDatabase().execSQL(sql);
    }

    /**
     * 删除数据(根据条件)
     *
     * @param whereClause
     * @param whereArgs
     * @param dbHelper
     */
    public void delete(String whereClause, String[] whereArgs, ClientDBHelper dbHelper) {
        int l = dbHelper.getWritableDatabase().delete(tableName, whereClause, whereArgs);
    }

    /**
     * 返回删除表的SQL语句
     */
    public String sql2Drop() {
        return "DROP TABLE IF EXISTS " + tableName + ";";
    }

    /**
     * 返回创建表的SQL语句
     */
    public String sql2Create() {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ").append(tableName).append(" (");
        boolean first = true;
        for (Field f : fields) {
            if (!first)
                builder.append(",");
            first = false;
            builder.append(f.name()).append(" ").append(f.type());
        }
        return builder.append(")").toString();
    }

    /**
     * 返回表名
     */
    public String tableName() {
        return tableName;
    }

    /**
     * 返回表字段集合
     */
    public Field[] fields() {
        return fields;
    }

    /**
     * 返回表的字段集合.
     */
    public String[] columns() {
        if (columns == null) {
            int lenght = fields.length;
            columns = new String[lenght];
            for (int i = 0; i < lenght; i++) {
                columns[i] = fields[i].name();
            }
        }
        return columns;
    }


}
