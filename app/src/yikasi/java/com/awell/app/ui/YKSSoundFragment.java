package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.awell.aidl.awellface.IAwellApi;
import com.awell.app.R;
import com.awell.app.utils.ApsStation;
import com.awell.app.utils.LogUtil;
import com.awell.app.utils.ToolClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            for (TextView textView : buttons) {
                textView.setSelected(false);
            }
            buttons[location].setSelected(true);
        }

        if (sound) {
            float x = ball[0] - center_w + ball_w / 2f;
            float y = ball[1] - center_h + ball_h / 2f;
            LogUtil.i("x = " + x + " y = " + y + " dataScale_h = " + dataScale_h + " dataScale_w " + dataScale_w);
            float lr, lf, rr, rf;
            // dataScale_w 和 dataScale_h 是UI的坐标与要设置功能的比例值；
            // 所以得出来的 lf,lr,rf,rr 都要x的除以dataScale_w，y的除以dataScale_h
            if (x >= 0) {
                if (y > 0) {
                    lf = x > y ? center_w - x : center_h - y;
                    lr = center_w - x;
                    rf = center_h - y;
                    rr = x > y ? center_w : center_h;
                } else {
                    lf = center_w - x;
                    lr = x > -y ? center_w - x : center_h + y;
                    rf = x > y ? center_w : center_h;
                    rr = center_h + y;
                }
            } else {
                if (y > 0) {
                    lf = center_h - y;
                    lr = x > y ? center_w : center_h;
                    rf = -x > y ? center_w + x : center_h - y;
                    rr = center_w + x;
                } else {
                    lf = x > y ? center_w : center_h;
                    lr = center_h + y;
                    rf = center_w + x;
                    rr = -x > -y ? center_w + x : center_h + y;
                }
            }

            lf -= ball_w / 2f;
            lr -= ball_w / 2f;
            rf -= ball_w / 2f;
            rr -= ball_w / 2f;

            LogUtil.i("x = " + x + " y = " + y + "  lf = " + lf + " rf = " + rf + " lr = " + lr + " rr = " + rr);
            sounds = new int[4];

            sounds[0] = new BigDecimal(lf).divide(new BigDecimal(dataScale_h), 0, RoundingMode.HALF_UP).intValue();
            sounds[1] = new BigDecimal(rf).divide(new BigDecimal(dataScale_h), 0, RoundingMode.HALF_UP).intValue();
            sounds[2] = new BigDecimal(lr).divide(new BigDecimal(dataScale_w), 0, RoundingMode.HALF_UP).intValue();
            sounds[3] = new BigDecimal(rr).divide(new BigDecimal(dataScale_w), 0, RoundingMode.HALF_UP).intValue();
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
