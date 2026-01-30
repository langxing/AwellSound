package com.awell.app;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.awell.app.utils.ApsStation;
import com.awell.app.utils.LogUtil;

public class DspProvider extends ContentProvider {

    private SQLiteDatabase mSqlDb = null;
    private DatabaseHelper mDbHelper = null;

    private static final String DATABASE_NAME = "awellAutoDsp.db";

    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME_APS = "station_aps";
    private static final String TABLE_NAME_SOUND = "station_sound";

    private static final int STATION_APS = 1;
    private static final int STATION_SOUND = 2;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(ApsStation.AUTHORITY, ApsStation.APS, STATION_APS);
        URI_MATCHER.addURI(ApsStation.AUTHORITY, ApsStation.SOUND, STATION_SOUND);
    }

    /**
     * Helper to operate database
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        /**
         * initial database name and database version
         *
         * @param context application context
         */
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * Create database table
         *
         * @param db The database
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create the table
            LogUtil.d("onCreate, create the database");
            db.execSQL("CREATE TABLE " + TABLE_NAME_APS + "(" + setColumn() + ")");

            db.execSQL("CREATE TABLE " + TABLE_NAME_SOUND + "("
                    + ApsStation.Station._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ApsStation.Station.APS_NAME + " INTEGER UNIQUE,"
                    + ApsStation.Station.APS_SOUND_L + " INTEGER DEFAULT 0,"
                    + ApsStation.Station.APS_SOUND_T + " INTEGER DEFAULT 0,"
                    + ApsStation.Station.APS_SOUND_R + " INTEGER DEFAULT 20,"
                    + ApsStation.Station.APS_SOUND_B + " INTEGER DEFAULT 20"
                    + ");"
            );
        }

        public StringBuilder setColumn() {
            StringBuilder sb = new StringBuilder();
            sb.append(ApsStation.Station._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,");
            sb.append(ApsStation.Station.APS_NAME + " INTEGER UNIQUE");
            for (int i = 0; i < 48; i++) {
                sb.append("," + ApsStation.Station.APS_SEEK + i + " INTEGER DEFAULT 0");
            }
            return sb;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO: reimplement this when dB version changes
            LogUtil.i("onUpgrade, upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_APS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SOUND);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return (null == mDbHelper) ? false : true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rows = 0;
        mSqlDb = mDbHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case STATION_APS:
                rows = mSqlDb.delete(TABLE_NAME_APS, selection, selectionArgs);
                break;
            case STATION_SOUND:
                rows = mSqlDb.delete(TABLE_NAME_SOUND, selection, selectionArgs);
                break;
            default:
                LogUtil.e("delete, unkown URI to delete: " + uri);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri rowUri = null, apsUri = null;
        long rowId = 0;
        mSqlDb = mDbHelper.getWritableDatabase();
        ContentValues v = new ContentValues(values);
        switch (URI_MATCHER.match(uri)) {
            case STATION_APS:
                rowId = mSqlDb.insert(TABLE_NAME_APS, null, v);
                apsUri = ApsStation.Station.CONTENT_URI_APS;
                break;
            case STATION_SOUND:
                rowId = mSqlDb.insert(TABLE_NAME_SOUND, null, v);
                apsUri = ApsStation.Station.CONTENT_URI_SOUND;
                break;
            default:
                LogUtil.e("delete, unkown URI to delete: " + uri);
                return rowUri;
        }
        if (rowId <= 0) {
            LogUtil.e("insert, failed to insert row into " + uri);
        }
        rowUri = ContentUris.appendId(apsUri.buildUpon(), rowId).build();
        getContext().getContentResolver().notifyChange(rowUri, null);
        return rowUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = null;
        switch (URI_MATCHER.match(uri)) {
            case STATION_APS:
                qb.setTables(TABLE_NAME_APS);
                break;
            case STATION_SOUND:
                qb.setTables(TABLE_NAME_SOUND);
                break;
            default:
                LogUtil.e("delete, unkown URI to delete: " + uri);
                return c;
        }
        c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (null != c) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rows = 0;
        mSqlDb = mDbHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case STATION_APS:
                rows = mSqlDb.update(TABLE_NAME_APS, values, selection, selectionArgs);
                break;
            case STATION_SOUND:
                rows = mSqlDb.update(TABLE_NAME_SOUND, values, selection, selectionArgs);
                break;
            default:
                LogUtil.e("update, unkown URI to update: " + uri);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
