package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.MotionEvent;
import android.view.View;

import com.awell.aidl.awellface.IAwellApi;
import com.awell.app.R;
import com.awell.app.utils.ApsStation;
import com.awell.app.utils.LogUtil;
import com.awell.app.utils.ToolClass;

import java.util.Arrays;

public class YKSSoundFragment extends SoundFragment {
    private IAwellApi mawellapi;

    @Override
    protected void initSoundRange() {
        soundRange = new int[]{0, 7};
    }

    @Override
    protected void initView() {
        super.initView();
        buttons[3].setVisibility(View.GONE);
    }

    @Override
    protected void setLayout(boolean send, boolean sound) {
        if (ball[1] > center_h - ball_h / 2) {
            LogUtil.i("YKSSound clamp: before t = " + ball[1] + " center_h = " + center_h + " ball_h = " + ball_h);
            ball[1] = (int) (center_h - ball_h / 2);
            ball[3] = ball[1] + ball_h;
            LogUtil.i("YKSSound clamp: after t = " + ball[1] + " b = " + ball[3]);
        }
        LogUtil.i("l = " + ball[0] + " t = " + ball[1] + " r = " + ball[2] + " b = " + ball[3]);
        aps_car_ball.layout(ball[0], ball[1], ball[2], ball[3]);

        LogUtil.i("location = " + location);

        if (location < 4 && isExist() < 0) {
            location = 4;
            for (View view : buttons) {
                view.setSelected(false);
            }
            buttons[location].setSelected(true);
        }

        if (sound) {
            int[] computed = computeSounds();
            if (computed == null) return;
            sounds = computed;
        }
        if (!send) return;
        LogUtil.i("sound[0] = " + sounds[0] + " sound[1] = " + sounds[1] + " sound[2] = " + sounds[2] + " sound[3] = " + sounds[3]);
        setSoundSetting(sounds);
        if (location == 4) {
            saveSQL(ApsStation.USER_MODE_LAYOUT_1, ApsStation.USER_MODE_1);
        } else if (location == 5) {
            saveSQL(ApsStation.USER_MODE_LAYOUT_2, ApsStation.USER_MODE_2);
        } else {
            saveSQL(ApsStation.NAME_LAYOUT, ApsStation.NAME_SEND);
        }
    }

    public void setLoudness(int open) {
        mLoudnessOpen = open == 1;
        // 这里只获取状态，UI更新还是用的本地缓存
        setLoudnessOpen(mLoudnessOpen, false);
    }

    @SuppressLint({"NonConstantResourceId", "ClickableViewAccessibility"})
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.aps_sound_range) {
            int touch_x = (int) event.getX();
            int touch_y = (int) event.getY();
            LogUtil.i("touch_x = " + touch_x + " touch_y = " + touch_y);
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (touch_x < ball_w / 2) {
                    touch_x = ball_w / 2;
                }
                if (touch_x > range_w - ball_w / 2) {
                    touch_x = range_w - ball_w / 2;
                }
                if (touch_y < ball_h / 2) {
                    touch_y = ball_h / 2;
                }
                if (touch_y > center_h) {
                    touch_y = (int) center_h;
                }
                ball = new int[]{touch_x - ball_w / 2, touch_y - ball_h / 2, touch_x + ball_w / 2, touch_y + ball_h / 2};
                setLayout(true, true);
            }
            return true;
        }
        return super.onTouch(v, event);
    }

    @Override
    protected void loudnessSwitch() {
        LogUtil.d("Loudness =" + mLoudnessOpen);
        if (mLoudnessOpen){
            ToolClass.setLoudnessGain(requireContext(), 1);
            ((SoundActivity)requireActivity()).setEqSetting(5, 1);
        } else {
            ToolClass.setLoudnessGain(requireContext(), 0);
            ((SoundActivity)requireActivity()).setEqSetting(5, 0);
        }
    }

    private void setSoundSetting(int[] sounds) {
        try {
            if (sounds == null || sounds.length < 4) return;
            byte[] data = new byte[6];
            data[0] = (byte) 0x0c;
            data[1] = 0;
            data[2] = (byte) sounds[0];
            data[3] = (byte) sounds[1];
            data[4] = (byte) sounds[2];
            data[5] = (byte) sounds[3];
            if (mawellapi == null){
                LogUtil.i("array = " + Arrays.toString(sounds));
                mawellapi = IAwellApi.Stub.asInterface(ServiceManager.getService("AwellAutoApi"));
            }
            mawellapi.sendDataToUart(data, 6);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
