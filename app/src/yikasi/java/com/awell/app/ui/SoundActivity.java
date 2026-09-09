package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.awell.aidl.awellface.IAwellApi;
import com.awell.app.R;
import com.awell.app.adapter.MainFragmentAdapter;
import com.awell.app.databinding.ActivityMainBinding;
import com.awell.app.ui.equal.SoundEqualFragment;
import com.awell.app.utils.LogUtil;
import com.awell.app.utils.ToolClass;
import com.awell.app.windows.EqualizerTypeWindow;
import com.awell.carevent.AwellCarEventData;
import com.awell.carevent.CarEvent;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundActivity extends AppCompatActivity implements View.OnClickListener, EqualizerTypeWindow.OnEqualizerTypeClickListener {
    private MainFragmentAdapter mFragmentAdapter;
    private ActivityMainBinding mBinding;
    private YKSSoundEqualFragment mEqualFragment;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private String[] mApsType;
    private IAwellApi mawellapi;
    private int[] data;
    private AwellCarEventData awellCarEventData;
    public static boolean isBack = false;

    private int mCurrentTypePosition = 0;
    private final AwellCarEventData.OnCarEventDataListener carEventDataListener = carEvent -> {
        LogUtil.i("CarEvent = " + carEvent.eventType + " subType = " + carEvent.subType + " otherData = " + Arrays.toString(carEvent.otherData));
        if (carEvent.eventType == CarEvent.CarEventType.CAR_EVENT_OTHER && carEvent.subType == 0x11) {
            data = carEvent.otherData;
            setData();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());

        awellCarEventData = new AwellCarEventData();
        setContentView(mBinding.getRoot());
        CrashReport.initCrashReport(getApplicationContext(), "43c9ae4625", false);

        mFragmentAdapter = new MainFragmentAdapter(getSupportFragmentManager());
        mEqualFragment = new YKSSoundEqualFragment();
        mFragmentList.add(mEqualFragment);
        mFragmentList.add(new YKSSoundFragment());
        mFragmentAdapter.setFragments(mFragmentList);
        mBinding.viewPager.setCanScroll(false);
        mBinding.viewPager.setAdapter(mFragmentAdapter);
        mBinding.btnSound.setOnClickListener(this);
        mBinding.btnEqualizer.setOnClickListener(this);
        mBinding.btnEqualizer.setSelected(true);
        mBinding.layoutCustom.setOnClickListener(this);
        mApsType = getResources().getStringArray(R.array.dsp_aps_type);
        mCurrentTypePosition = ToolClass.getTypeFlag(this);
        if (mCurrentTypePosition == 0) {
           mBinding.tvCustom.setText(mApsType[mCurrentTypePosition]);
        } else {
            setEqSetting(1, 6, 0);
        }

        awellCarEventData.init(this);
        awellCarEventData.setCarEventDataListener(carEventDataListener);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_equalizer:
            case R.id.btn_sound:
                switchPage(view.getId());
            break;
            case R.id.layout_custom:
                showTypeWindow();
                break;
        }
    }

    private static final int[] MODE_VALUE_TO_TYPE_INDEX = {
            1,  // 0: 特效关 → User
            5,  // 1: 古典 → Classical
            3,  // 2: 流行 → Pop
            6,  // 3: 俱乐部 → Bass
            7,  // 4: 现场 → Treble
            2,  // 5: 爵士 → Jazz
            8,  // 6: 轻柔 → Gentle
            4,  // 7: 摇滚 → Rock
            9   // 8: 迪斯科 → Disco
    };

    private void setData() {
        if (data != null && data.length >= 3 && !mFragmentList.isEmpty()) {
            int temp = data[0];
            LogUtil.i("setData data[0]=" + data[0] + " data[1]=" + data[1] + " data[2]=" + data[2]);
            if (temp == 0x9C) {
                temp = data[1];
                isBack = true;
                if (temp == 5 && mFragmentList.get(1).isAdded()) {
                    ((YKSSoundFragment)mFragmentList.get(1)).setLoudness(data[2]);
                } else if (temp == 6) {
                    int modeValue = data[2];
                    if (modeValue >= 0 && modeValue < MODE_VALUE_TO_TYPE_INDEX.length) {
                        int typeIndex = MODE_VALUE_TO_TYPE_INDEX[modeValue];
                        if (typeIndex >= 0 && typeIndex < mApsType.length) {
                            updateTypeUI(typeIndex, mApsType[typeIndex]);
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void switchPage(int id) {
        mBinding.btnEqualizer.setSelected(false);
        mBinding.btnSound.setSelected(false);
        switch (id) {
            case R.id.btn_equalizer:
                mBinding.btnEqualizer.setSelected(true);
                mBinding.viewPager.setCurrentItem(0);
                mBinding.layoutCustom.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_sound:
                mBinding.btnSound.setSelected(true);
                mBinding.viewPager.setCurrentItem(1);
                mBinding.layoutCustom.setVisibility(View.GONE);
                break;
        }
    }

    private void showTypeWindow() {
        mBinding.ivCustom.setRotation(0);
        EqualizerTypeWindow typeWindow = new EqualizerTypeWindow(this);
        if (mCurrentTypePosition < 0 || mCurrentTypePosition >= mApsType.length) {
            mCurrentTypePosition = 0;
        }
        typeWindow.setSelectedPosition(mCurrentTypePosition);
        typeWindow.setListener(this);
        typeWindow.showAsDropDown(mBinding.layoutCustom, 0, 0, Gravity.START);
    }

    public void setEqSetting(int type, int gain) {
        setEqSetting(0, type, gain);
    }

    /**
     * 0:低音
     * 1:请求
     * 1:中音
     * 2:高音
     * 3:重低音
     * 4:中心频点
     * 5:等响度
     * 6:模式
     * 7:ALL
     *
     * @param type
     * @param gain
     */
    public void setEqSetting(int data0, int type, int gain) {
        try {
            byte[] data = new byte[7];
            data[0] = (byte) 0x0b;
            data[1] = (byte) data0;
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

    public void updateTypeUI(int position, String type) {
        mBinding.ivCustom.setRotation(180);
        mBinding.tvCustom.setText(type);
        LogUtil.i("updateTypeUI position = " + position + " type = " + type);
        if (mEqualFragment != null) {
            mEqualFragment.onTypeUpdated(position);
        }
        mCurrentTypePosition = position;
    }

    @Override
    public void onEqualizerTypeClick(int position, String type) {
        mBinding.ivCustom.setRotation(180);
        mBinding.tvCustom.setText(type);
        LogUtil.i("onEqualizerTypeClick position = " + position + " type = " + type);
        if (mEqualFragment != null && mEqualFragment.isAdded() && mCurrentTypePosition != position) {
            mEqualFragment.setType(position);
        }
        mCurrentTypePosition = position;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        awellCarEventData.deinit();

    }

}
