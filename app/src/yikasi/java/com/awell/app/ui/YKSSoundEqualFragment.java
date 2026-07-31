package com.awell.app.ui;

import android.os.RemoteException;
import com.awell.aidl.awellface.IAwellApi;
import com.awell.app.model.ApsData;
import com.awell.app.ui.equal.SoundEqualFragment;
import com.awell.app.utils.ApsStation;
import com.awell.app.utils.LogUtil;

import android.os.ServiceManager;

import java.util.Arrays;

public class YKSSoundEqualFragment extends SoundEqualFragment {
    private IAwellApi mawellapi;

    @Override
    protected void setModel(int type) {
        super.setModel(type);
        LogUtil.i("type = " + type);
        if (type > 0) {
            // Type=6:模式(0:特效关1:古典2:流行
            //3:俱乐部4:现场 5:爵士
            //6:轻柔7:摇滚8:迪斯科)
            setEqSetting(6, type - 1);
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
        setEqSetting(gains[0], gains[1]);
    }

    private void setEqSetting(int type, int gain) {
        try {
            byte[] data = new byte[7];
            data[0] = (byte) 0x0b;
            data[1] = 0;
            data[2] = (byte) type;
            data[3] = (byte) gain;
            data[4] = 0;
            data[5] = 0;
            data[6] = 0;
            if (mawellapi == null){
                mawellapi = IAwellApi.Stub.asInterface(ServiceManager.getService("AwellAutoApi"));
            }
            LogUtil.i("eq data = " + Arrays.toString(data));
            mawellapi.sendDataToUart(data, 7);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
