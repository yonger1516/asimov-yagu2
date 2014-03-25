package com.seven.asimov.test.tool.core;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * DataHelper.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class DataHelper {
    private static final String DATABASE_NAME = "example.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "table1";
    private Context mContext;
    private SQLiteDatabase mDb;
    private SQLiteStatement mInsertStmt;
    private static final String INSERT = "insert into " + TABLE_NAME + "(name) values (?)";
    private SQLiteStatement mStmtInsert;

    public DataHelper(Context context) {
        this.mContext = context;
        OpenHelper openHelper = new OpenHelper(this.mContext);
        this.mDb = openHelper.getWritableDatabase();
        this.mInsertStmt = this.mDb.compileStatement(INSERT);
        mStmtInsert = mDb.compileStatement("insert into " + TABLE_NAME + "(name) values (?)");
    }

    public long insert(String name) {
        this.mInsertStmt.bindString(1, name);
        return this.mInsertStmt.executeInsert();
    }

    public void deleteAll() {
        this.mDb.delete(TABLE_NAME, null, null);
    }

    public List<String> selectAll() {
        List<String> list = new ArrayList<String>();
        Cursor cursor = this.mDb.query(TABLE_NAME, new String[]{"name"}, null, null, null, null, "name desc");
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    /**
     * OpenHelper.
     *
     * @author Maksim Selivanov (mselivanov@seven.com)
     */
    private static class OpenHelper extends SQLiteOpenHelper {
        OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + "(id INTEGER PRIMARY KEY, name TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("Example", "Upgrading database, this will drop tables and recreate.");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
