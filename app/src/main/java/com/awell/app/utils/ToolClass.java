package com.awell.app.utils;

import android.content.Context;
import android.os.Environment;
import android.view.Gravity;
import android.widget.Toast;

import com.awell.kpslibrary.Constant;
import com.awell.kpslibrary.module.AwellAudio;
import java.util.Arrays;

public class ToolClass {

    private static final String Path = Environment.getExternalStorageDirectory() + "/system/awell/";
    private static final String DataName = "logcat.txt";

    private static Toast toast;

    private static final String TAG = ToolClass.class.getSimpleName();

    public static final int[] apsGainRange = {-14, 14};

    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        } else {
            toast.setText(msg);
        }
        float scale = context.getResources().getDisplayMetrics().density;
        int i = (int) (64 * scale + 0.5f);
        toast.setGravity(Gravity.BOTTOM, 0, i);
        toast.show();
    }

    public static boolean getModeFlag(Context context) {
        int modeFlag = android.provider.Settings.System.getInt(context.getContentResolver(), "DspApsModeFlag", -1); //0 前后模式  1 全车模式
        if (modeFlag == 0) {
            return true;
        } else if (modeFlag == 1) {
            return false;
        } else {
            int[] mode = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETEQMODE.code, null);
            LogUtil.i("MODE = " + Arrays.toString(mode));
            return !(mode != null && mode.length > 0 && mode[0] == 1);
        }
    }

    public static void setModeFlag(Context context, boolean flag) {
        int value = flag ? 0 : 1;
        android.provider.Settings.System.putInt(context.getContentResolver(), "DspApsModeFlag", value); //0 前后模式  1 全车模式
    }

    public static int getTypeFlag(Context context) {
        return android.provider.Settings.System.getInt(context.getContentResolver(), "SoundApsTypeFlag", 0);//模式 爵士 流行 等
    }

    public static void setTypeFlag(Context context, int value) {
        android.provider.Settings.System.putInt(context.getContentResolver(), "SoundApsTypeFlag", value);//模式 爵士 流行 等
    }

    public static int getLocationFlag(Context context) {
        return android.provider.Settings.System.getInt(context.getContentResolver(), "DspApsLocationFlag", 2);//声场当前位置
    }

    public static void setLocationFlag(Context context, int value) {
        android.provider.Settings.System.putInt(context.getContentResolver(), "DspApsLocationFlag", value);//声场当前位置
    }

    public static int getLoudnessGain(Context context) {
        return android.provider.Settings.System.getInt(context.getContentResolver(), "DspApsLoudnessGain", 1);//Loudness Gain
    }

    public static void setLoudnessGain(Context context, int value) {
        android.provider.Settings.System.putInt(context.getContentResolver(), "DspApsLoudnessGain", value);//Loudness Gain
    }

    public static int getTrebleGain(Context context) {
        return android.provider.Settings.System.getInt(context.getContentResolver(), "DspTrebleGain", 14);
    }

    public static void setTrebleGain(Context context, int value) {
        android.provider.Settings.System.putInt(context.getContentResolver(), "DspTrebleGain", value);
    }

    public static int getBassGain(Context context) {
        return android.provider.Settings.System.getInt(context.getContentResolver(), "DspBassGain", 14);
    }

    public static void setBassGain(Context context, int value) {
        android.provider.Settings.System.putInt(context.getContentResolver(), "DspBassGain", value);
    }

}
