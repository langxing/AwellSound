package com.awell.app.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class ApsStation {

    public static final String AUTHORITY = "com.awell.app";

    public static final String APS = "aps_aps";
    public static final String SOUND = "aps_sound";

    //aps
    public static final int NAME_GAIN = 10;
    //改为需要发送的实际值
    public static final int NAME_GAIN_CUSTOM = 20;
    public static final int NAME_GAIN_REAR = 20;  //改为需要发送的实际值
    public static final int NAME_Q_FRONT = 30;  //改为显示
    public static final int NAME_Q_DEFAULT_FRONT = 31;
    public static final int NAME_Q_REAR = 40;  //改为需要发送的实际值

    //sound
    public static final int NAME_LAYOUT = 100;
    public static final int NAME_SEND = 110;
    public static final int USER_MODE_LAYOUT_1 = 120;
    public static final int USER_MODE_LAYOUT_2 = 130;
    public static final int USER_MODE_1 = 140;
    public static final int USER_MODE_2 = 150;

    //surround
    public static final int NAME_LAYOUT_S = 200;
    public static final int NAME_SEND_S = 210;

    public static final class Station implements BaseColumns {
        public static final Uri CONTENT_URI_APS = Uri.parse("content://" + AUTHORITY + "/" + APS);
        public static final Uri CONTENT_URI_SOUND = Uri.parse("content://" + AUTHORITY + "/" + SOUND);

        public static final String APS_SEEK = "aps_seek_";  //
        public static final String APS_NAME = "aps_name";   // 增益
        public static final String APS_SOUND_L = "aps_sound_l"; // 声场坐标 左
        public static final String APS_SOUND_T = "aps_sound_t"; // 声场坐标 上
        public static final String APS_SOUND_R = "aps_sound_r"; // 声场坐标 右
        public static final String APS_SOUND_B = "aps_sound_b"; // 声场坐标 下
    }

    public static void insertApsToDb(Context context, int[] gain, int name) {
        int i, size = gain.length;
        ContentValues values = new ContentValues(size + 1);
        values.put(Station.APS_NAME, name);
        for (i = 0; i < size; i++) {
            values.put(Station.APS_SEEK + i, gain[i]);
        }
        context.getContentResolver().insert(Station.CONTENT_URI_APS, values);
    }

    public static void insertApsToDb(Context context, float[] apsQ, int name) {
        int i, size = apsQ.length;
        ContentValues values = new ContentValues(size + 1);
        values.put(Station.APS_NAME, name);
        for (i = 0; i < size; i++) {
            values.put(Station.APS_SEEK + i, apsQ[i]);
        }
        context.getContentResolver().insert(Station.CONTENT_URI_APS, values);
    }

    public static void deleteApsInDb(Context context, int name) {
        if (name <= 0) {
            context.getContentResolver().delete(
                    Station.CONTENT_URI_APS, null, null);
        } else {
            context.getContentResolver().delete(
                    Station.CONTENT_URI_APS, Station.APS_NAME + "=?", new String[]{String.valueOf(name)});
        }
    }

    private static void updateApsInDb(Context context, ContentValues values, int name) {
        context.getContentResolver().update(
                Station.CONTENT_URI_APS, values, Station.APS_NAME + "=?", new String[]{String.valueOf(name)});
    }

    public static void updateApsInDb(Context context, int index, int progress, int name) {
        LogUtil.i("index = " + index + "  progress = " + progress + "  name = " + name);
        ContentValues values = new ContentValues(1);
        values.put(Station.APS_SEEK + index, progress);
        updateApsInDb(context, values, name);
    }

    public static void updateApsInDb(Context context, int index, float progress, int name) {
        ContentValues values = new ContentValues(1);
        values.put(Station.APS_SEEK + index, progress);
        updateApsInDb(context, values, name);
    }

    public static Cursor queryApsDb(Context context, int name) {
        return context.getContentResolver().query(
                Station.CONTENT_URI_APS, null, Station.APS_NAME + "=?",
                new String[]{String.valueOf(name)}, Station._ID);
    }

    public static int getApsDbCount(Context context) {
        int stationNus = 0;
        // 推荐写法，一个类实现了AutoCloseable会自动关闭
        try (Cursor cursor = context.getContentResolver().query(
                Station.CONTENT_URI_APS,
                new String[]{Station._ID}, null, null, null)) {
            if (cursor != null) {
                stationNus = cursor.getCount();
            }
        }
        return stationNus;
    }

    @SuppressLint("Range")
    public static int[] getApsGain(Context context, int name) {
        Cursor cursor = null;
        int[] gain = null;
        try {
            cursor = queryApsDb(context, name);
            if (cursor != null && cursor.moveToFirst()) {
                LogUtil.i("cursor.getColumnCount() = " + cursor.getColumnCount());
                gain = new int[cursor.getColumnCount() - 2];
                int i;
                for (i = 0; i < gain.length; i++) {
                    gain[i] = cursor.getInt(cursor.getColumnIndex(Station.APS_SEEK + i));
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return gain;
    }

    @SuppressLint("Range")
    public static float[] getApsQ(Context context, int name) {
        Cursor cursor = null;
        float[] apsQ = null;
        try {
            cursor = queryApsDb(context, name);
            if (cursor != null && cursor.moveToFirst()) {
                LogUtil.i("cursor.getColumnCount() = " + cursor.getColumnCount());
                apsQ = new float[cursor.getColumnCount() - 2];
                int i;
                for (i = 0; i < apsQ.length; i++) {
                    apsQ[i] = cursor.getFloat(cursor.getColumnIndex(Station.APS_SEEK + i));
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return apsQ;
    }

    public static void insertSoundToDb(Context context, int[] data, int name) {
        ContentValues values = new ContentValues(5);
        values.put(Station.APS_NAME, name);
        values.put(Station.APS_SOUND_L, data[0]);
        values.put(Station.APS_SOUND_T, data[1]);
        values.put(Station.APS_SOUND_R, data[2]);
        values.put(Station.APS_SOUND_B, data[3]);
        context.getContentResolver().insert(Station.CONTENT_URI_SOUND, values);
    }

    public static void deleteSoundInDb(Context context, int name) {
        if (name <= 0) {
            context.getContentResolver().delete(
                    Station.CONTENT_URI_SOUND, null, null);
        } else {
            context.getContentResolver().delete(
                    Station.CONTENT_URI_SOUND, Station.APS_NAME + "=?", new String[]{String.valueOf(name)});
        }
    }

    public static void updateSoundInDb(Context context, String location, int sound, int name) {
        ContentValues values = new ContentValues(1);
        values.put(location, sound);
        context.getContentResolver().update(
                Station.CONTENT_URI_SOUND, values, Station.APS_NAME + "=?", new String[]{String.valueOf(name)});
        context.getContentResolver().insert(Station.CONTENT_URI_SOUND, values);
    }

    public static Cursor querySoundInDb(Context context, int name) {
        return context.getContentResolver().query(
                Station.CONTENT_URI_SOUND, null, Station.APS_NAME + "=?",
                new String[]{String.valueOf(name)}, Station._ID);
    }

    @SuppressLint("Range")
    public static int[] getSoundData(Context context, int name) {
        int[] data = null;
        try (Cursor cursor = querySoundInDb(context, name)) {
            if (cursor.moveToFirst()) {
                data = new int[4];
                data[0] = cursor.getInt(cursor.getColumnIndex(Station.APS_SOUND_L));
                data[1] = cursor.getInt(cursor.getColumnIndex(Station.APS_SOUND_T));
                data[2] = cursor.getInt(cursor.getColumnIndex(Station.APS_SOUND_R));
                data[3] = cursor.getInt(cursor.getColumnIndex(Station.APS_SOUND_B));
            }
        }
        return data;
    }
}
