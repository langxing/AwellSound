package com.awell.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Toast;

import com.awell.kpslibrary.Constant;
import com.awell.kpslibrary.module.AwellAudio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ToolClass {

    private static final String Path = Environment.getExternalStorageDirectory() + "/system/awell/";
    private static final String DataName = "logcat.txt";

    private static Toast toast;

    public static final String GET_BESLOUDNESS_STATUS = "GetBesLoudnessStatus";
    public static final String GET_BESLOUDNESS_STATUS_ENABLED = "GetBesLoudnessStatus=1";
    public static final String SET_BESLOUDNESS_ENABLED = "SetBesLoudnessStatus=1";
    public static final String SET_BESLOUDNESS_DISABLED = "SetBesLoudnessStatus=0";
    private static final String TAG = ToolClass.class.getSimpleName();

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
        return android.provider.Settings.System.getInt(context.getContentResolver(), "DspApsTypeFlag", 0);//模式 爵士 流行 等
    }

    public static void setTypeFlag(Context context, int value) {
        android.provider.Settings.System.putInt(context.getContentResolver(), "DspApsTypeFlag", value);//模式 爵士 流行 等
    }

    public static int getLocationFlag(Context context) {
        return android.provider.Settings.System.getInt(context.getContentResolver(), "DspApsLocationFlag", 2);//声场当前位置
    }

    public static void setLocationFlag(Context context, int value) {
        android.provider.Settings.System.putInt(context.getContentResolver(), "DspApsLocationFlag", value);//声场当前位置
    }

    public static int getLoudnessGain(Context context) {
        return android.provider.Settings.System.getInt(context.getContentResolver(), "DspApsLoudnessGain", 0);//Loudness Gain
    }

    public static void setLoudnessGain(Context context, int value) {
        android.provider.Settings.System.putInt(context.getContentResolver(), "DspApsLoudnessGain", value);//Loudness Gain
    }

    //Increase Gain start
    private static String INCREASEGAIN = "DspApsIncreaseGain";
    private static String INCREASEGAINDEF = "DspApsIncreaseGainDef";

//    public static int getIncreaseGain(Context context, int type) {
//        int freq = android.provider.Settings.System.getInt(context.getContentResolver(), INCREASEGAIN + type, -1);
//        LogUtil.i("freq = " + freq);
//        if (freq < 0) {
//
//            int[] parameter = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETCURBASSBOOSTGAIN.code,
//                    String.valueOf(type == ApsView.REAR ? 1 : 0));
//            if (parameter != null && parameter.length > 0) {
//                LogUtil.i("parameter = " + parameter[0]);
//                freq = parameter[0];
//                android.provider.Settings.System.putInt(context.getContentResolver(), INCREASEGAIN + type, freq);
//                android.provider.Settings.System.putInt(context.getContentResolver(), INCREASEGAINDEF + type, freq);
//            }
//        }
//        return freq;
//    }

    public static void setIncreaseGain(Context context, int value, int type) {
        android.provider.Settings.System.putInt(context.getContentResolver(), INCREASEGAIN + type, value);
    }

    public static int getIncreaseGainDef(Context context, int type) {
        return android.provider.Settings.System.getInt(context.getContentResolver(), INCREASEGAINDEF + type, -1);
    }
    //Increase Gain end

    //Increase Freq start
    private static String INCREASEFREQ = "DspApsIncreaseFreq";
    private static String INCREASEFREQDEF = "DspApsIncreaseFreqDef";

//    public static int getIncreaseFreq(Context context, int[] apsFreq, int type) {
//        int freqIndex = android.provider.Settings.System.getInt(context.getContentResolver(), INCREASEFREQ + type, -1);
//        LogUtil.i("freq = " + freqIndex);
//        if (freqIndex < 0) {
//
//            int[] parameter = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETCURBASSBOOSTCUTFREQ.code,
//                    String.valueOf(type == ApsView.REAR ? 1 : 0));
//            if (parameter != null && parameter.length > 0) {
//                for (int i = 0; i < apsFreq.length; i++) {
//                    if (apsFreq[i] == parameter[0]) {
//                        freqIndex = i;
//                    }
//                }
//                LogUtil.i("freq = " + freqIndex);
//                if (freqIndex < 0) {
//                    freqIndex = apsFreq.length - 1;
//                    AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETBASSBOOSTCUTFREQ.code, new int[]{apsFreq[freqIndex], 1}, 2);
//                }
//                setIncreaseFreq(context, freqIndex, type);
//                android.provider.Settings.System.putInt(context.getContentResolver(), INCREASEFREQDEF + type, freqIndex);
//            }
//        }
//        return freqIndex;
//    }

    public static void setIncreaseFreq(Context context, int value, int type) {
        android.provider.Settings.System.putInt(context.getContentResolver(), INCREASEFREQ + type, value);
    }

    public static int getIncreaseFreqDef(Context context, int type) {
        return android.provider.Settings.System.getInt(context.getContentResolver(), INCREASEFREQDEF + type, -1);
    }
    //Increase Freq end

    //Filter start
    private static String FILTER = "DspApsFilterValue";
    private static String FILTERDEF = "DspApsFilterValueDef";

//    public static int getFilterGain(Context context, int[] apsFreq, int type) {
//        int freq = android.provider.Settings.System.getInt(context.getContentResolver(), FILTER + type, -1);
//        LogUtil.i("freq = " + freq);
//        if (freq < 0) {
//            int[] parameter = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETCURHPFBAND.code, String.valueOf(type == ApsView.REAR ? 0 : 1));
//
//            if (parameter != null && parameter.length > 0) {
//                LogUtil.i("parameter = " + parameter[0]);
//                for (int i = 0; i < apsFreq.length; i++) {
//                    if (apsFreq[i] == parameter[0]) {
//                        freq = i;
//                    }
//                }
//                LogUtil.i("freq = " + freq);
//                setFilterGain(context, freq, type);
//                android.provider.Settings.System.putInt(context.getContentResolver(), FILTERDEF + type, freq);
//            }
//        }
//        return freq;
//    }

    public static void setFilterGain(Context context, int value, int type) {
        android.provider.Settings.System.putInt(context.getContentResolver(), FILTER + type, value);
    }

    public static int getFilterGainDef(Context context, int type) {
        return android.provider.Settings.System.getInt(context.getContentResolver(), FILTERDEF + type, -1);
    }
    //Filter end

    public static int getStateBar(Context mContext) {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int[] getDisplayInfo(Activity activity) {
        int[] displayInfo = new int[2];
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        displayInfo[0] = dm.widthPixels;
        displayInfo[1] = dm.heightPixels;
        return displayInfo;
    }

    public static int[] getDisplayInfo(Context context) {
        int[] displayInfo = new int[2];
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        displayInfo[0] = dm.widthPixels;
        displayInfo[1] = dm.heightPixels;
        return displayInfo;
    }

    public static String bytesToHexString(byte[] src, int size) {
//        String.format("%02x", size)
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null) return "";
        size = src.length < size ? src.length : size;

        for (int i = 0; i < size; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv + " ");
        }
        return stringBuilder.toString();
    }

    public static String bytesToHexString(int[] src, int size) {
//        String.format("%02x", size)
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null) return "";
        size = src.length < size ? src.length : size;

        for (int i = 0; i < size; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv + " ");
        }
        return stringBuilder.toString();
    }

    /**
     * 输出日志到外部存储
     */
    public static void writeToSdcard(String msg) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) { // 如果sdcard存在
            File file = new File(Path);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(Path, DataName);
            PrintStream out = null;
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSSSS");
                Date curDate = new Date(System.currentTimeMillis());
                String str = formatter.format(curDate);
                out = new PrintStream(new FileOutputStream(file, true));
                out.println(str + msg);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }
}
