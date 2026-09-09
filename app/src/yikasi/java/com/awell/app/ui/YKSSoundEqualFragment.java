package com.awell.app.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.awell.app.model.ApsData;
import com.awell.app.ui.equal.SoundEqualFragment;
import com.awell.app.utils.ApsStation;
import com.awell.app.utils.LogUtil;
import com.awell.app.utils.ToolClass;
import com.awell.kpslibrary.Constant;
import com.awell.kpslibrary.module.AwellAudio;

import java.util.Arrays;

public class YKSSoundEqualFragment extends SoundEqualFragment {

    private int mPendingPosition = -1;

    public void onTypeUpdated(int position) {
        if (isAdded() && mDataArray != null) {
            setType(position, false);
        } else {
            LogUtil.i("onTypeUpdated fragment not ready, save position=" + position);
            mPendingPosition = position;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mPendingPosition >= 0 && mDataArray != null) {
            LogUtil.i("onViewCreated apply pending position=" + mPendingPosition);
            setType(mPendingPosition, false);
            mPendingPosition = -1;
        }
    }

    @Override
    public void setData(int[][] data) {
        mDataArray = data;
        apsFreq = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETBANDS.code, null);
        if (apsFreq == null || apsFreq.length != ApsData.getInstance().apsFreq.length){
            LogUtil.e("apsFreq is null ");
            apsFreq = ApsData.getInstance().apsFreq.clone();
        }
        final int typeFlag = ToolClass.getTypeFlag(requireContext());
        final int position = typeFlag == -1 ? 0 : typeFlag;
        LogUtil.i("setData typeFlag=" + typeFlag + " position=" + position + " dataArray length=" + (data != null ? data.length : "null"));
        apsGain = ApsStation.getApsGain(getContext(), ApsStation.NAME_GAIN);
        LogUtil.i("apsGain = " + Arrays.toString(apsGain));
        if (apsGain == null) {
            apsGain = mDataArray[0];
            ApsStation.insertApsToDb(requireContext(), apsGain, ApsStation.NAME_GAIN);
            ApsStation.insertApsToDb(requireContext(), apsGain, ApsStation.NAME_GAIN_CUSTOM);
        }
        setType(position, false);
    }

    @Override
    protected void setModel(int type) {
        super.setModel(type);
        LogUtil.i("type = " + type);
        if (type > 0) {
            // Type=6:模式(0:特效关1:古典2:流行
            //3:俱乐部4:现场 5:爵士
            //6:轻柔7:摇滚8:迪斯科)
            int temp;
            switch (type) {
                case 2:
                    temp = 5;
                    break;
                case 4:
                    temp = 7;
                    break;
                case 5:
                    temp = 1;
                    break;
                case 6:
                    temp = 3;
                    break;
                case 7:
                    temp = 4;
                    break;
                case 8:
                    temp = 6;
                    break;
                default:
                    temp = type - 1;
                    break;
            }
            ((SoundActivity)requireActivity()).setEqSetting(6, temp);
        }
    }

    @Override
    protected void sendGain(int lowValue, int highValue) {

    }

    @Override
    protected void saveGain(int index, int progress) {
        ApsStation.updateApsInDb(requireContext(), index, progress, ApsStation.NAME_GAIN);
        int[] gains = new int[2];
        int gainIndex = 0;
        if (index >= ApsData.DefaultData.apsFreqSend.length/2) {
            // 高音
            gainIndex = 2;
        }
        gains[0] = gainIndex;
        gains[1] = progress - gainMax / 2;
        LogUtil.i("gains = " + Arrays.toString(gains));
        ((SoundActivity)requireActivity()).setEqSetting(gains[0], gains[1]);
    }

}
