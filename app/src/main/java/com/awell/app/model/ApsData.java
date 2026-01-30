package com.awell.app.model;

import com.awell.app.utils.LogUtil;
import com.awell.kpslibrary.Constant;
import com.awell.kpslibrary.module.AwellAudio;

import java.util.Arrays;

public class ApsData {

    private static ApsData apsData = null;

    /**
     * Hz
     */
    public int[] apsFreq = {20, 30, 40, 50, 60, 70, 80, 95, 110, 125,   //10
            150, 175, 200, 235, 275, 315, 375, 435, 500, 600,           //10
            700, 800, 860, 920, 1000, 1100, 1200, 1300, 1500, 1700, 2000, 2400, 2800, 3200,     //14
            3800, 4400, 5000, 6000, 7000, 8000, 9500, 11000, 12500, 14000,      //10
            15000, 16000, 17000, 18000};


    private int[] apsFreqSend; //实际频率
    /**
     * 底部Q值增益
     */
    private float[] apsQ;
    private int[] apsGainRange; //增益范围
    /**
     * 用户模式选择
     * 标准-爵士-流行-...
     */
    private int[][] typeValue;

    public static int gainRange = 10;
    public static int gainMax = 5;
    public static int gainMin = -5;

    /**
     * Q值范围
     */
    public static float[] apsQRange;

//    private int[] typeId = {R.array.aps_default, R.array.aps_default, R.array.aps_jazz,
//            R.array.aps_pop, R.array.aps_rock, R.array.aps_classical, R.array.aps_soft,
//            R.array.aps_hall, R.array.aps_cinema};

    public static ApsData getInstance() {
        if (apsData == null)
            apsData = new ApsData();
        return apsData;
    }

    private ApsData() {

    }

    //    [20, 31, 50, 80, 125, 200, 315, 500, 800, 1250, 2000, 3150, 5000, 8000, 12500, 20000]
    public int[] getApsFreqSend() {
        if (apsFreqSend == null) {
            apsFreqSend = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETBANDS.code, null);
            if (apsFreqSend == null) {
                apsFreqSend = DefaultData.apsFreqSend.clone();
            }
        }
        return apsFreqSend;
    }

    public float[] getApsQ() {
        if (apsQ == null) {
            int[] freq = getApsFreqSend();
            LogUtil.i(Arrays.toString(freq));
            apsQ = new float[freq.length];
            float[] apsQDef;
            for (int i = 0; i < freq.length; i++) {
                apsQDef = AwellAudio.getFloatParameter(Constant.IAUDIOCONTROL.CMD.GETCURQFACTORREAR.code, String.valueOf(freq[i]));
                if (apsQDef != null && apsQDef.length > 0) {
                    apsQ[i] = apsQDef[0] == 0f ? 1.7f : apsQDef[0];
                }
            }
        }
        return apsQ;
    }

    public int[] getApsGainRange(int size) {
        if (apsGainRange == null) {
            apsGainRange = new int[size];
            int[] apsGainRange1;
            for (int i = 0; i < size; i++) {
                apsGainRange1 = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETBANDLEVELRANGE.code, String.valueOf(apsFreqSend[i]));
                LogUtil.i("apsGainRange1 = " + Arrays.toString(apsGainRange1));
                if (apsGainRange1 != null && apsGainRange1.length > 1) {
                    apsGainRange[i] = apsGainRange1[1] - apsGainRange1[0];
                    gainRange = apsGainRange[i];
                    gainMax = apsGainRange1[1];
                    gainMin = apsGainRange1[0];
                }
            }
        }
        return apsGainRange;
    }

    public void getApsQRange(int size) {
        if (apsQRange == null) {
            for (int i = 0; i < size; i++) {
                apsQRange = AwellAudio.getFloatParameter(Constant.IAUDIOCONTROL.CMD.GETQFACTORRANGE.code, String.valueOf(apsFreqSend[i]));
                LogUtil.i("apsQRange = " + Arrays.toString(apsQRange));
            }
            if (apsQRange == null)
                apsQRange = DefaultData.apsQRange.clone();
        }
    }

//    public int[][] getTypeValue(Context context) {
//        if (typeValue != null)
//            return typeValue;
//        String[] typeStrings = context.getResources().getStringArray(R.array.dsp_aps_type);
//        int i, j;
//        int size = typeStrings.length;
//        typeValue = new int[size][apsFreq.length];
//        for (i = 0; i < size; i++) {
//            typeValue[i] = context.getResources().getIntArray(typeId[i]);
//            for (j = 0; j < apsFreq.length; j++) {
//                typeValue[i][j] = typeValue[i][j] - (12 - apsData.gainMax);
//            }
//        }
//        return typeValue;
//    }

    public interface DefaultData {
        int[] apsFreqSend = {20, 31, 50, 80, 125, 200, 315, 500, 800, 1250, 2000, 3150, 5000, 8000, 12500, 20000};
        int[] surroundRange = {21};
        int[] apsFreq = {54, 68, 86, 108, 134, 172, 214};
        float[] apsQRange = {0.7f, 1.7f, 2.7f, 3.7f, 4.7f, 5.7f, 6.7f};
        int[] soundRange = {0, 79};
        int[] apsFreqFilter = {0, 25, 31, 40, 50, 63, 80, 100, 125, 160, 200, 250};
    }
}
