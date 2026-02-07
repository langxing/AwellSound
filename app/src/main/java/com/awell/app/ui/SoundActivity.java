package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.awell.app.R;
import com.awell.app.model.ApsData;
import com.awell.app.utils.ApsStation;
import com.awell.app.utils.LogUtil;
import com.awell.app.utils.NoFastClickUtils;
import com.awell.app.utils.ToolClass;
import com.awell.kpslibrary.Constant;
import com.awell.kpslibrary.module.AwellAudio;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

import java.util.Arrays;

public class SoundActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = SoundActivity.class.getSimpleName();
    private final int[] btnId = {R.id.btn_drive_left,
            R.id.btn_drive_right,
            R.id.btn_drive_center,
            R.id.btn_drive_rear,
            R.id.btn_drive_user1,
            R.id.btn_drive_user2};
    private TextView[] buttons;
    private RelativeLayout aps_sound_range;
    private ImageView aps_car_ball;
    private SwitchCompat mIvLoudness;
    private TextView mTvDefault;
    private VerticalSeekBar mHighSeekbar, mLowSeekBar;
    private int ball_w, ball_h, range_w, range_h;
    /**
     * ball 小球的边框: 左、上、右、下
     */
    private int[] soundRange, ball;
    private int[][] apsSound;
    private float center_w, center_h, dataScale_w = 0f, dataScale_h = 0f;
    private boolean mLoudnessOpen = false;
    private int gainMax = 0;
    private boolean isFirst = false;
    private SoundHandler soundHandler;
    private final int APS_SOUND_LONG = 0x10;
    private boolean isSoundLong = false;

    private final int[] btnKey = {R.id.dsp_sound_up, R.id.dsp_sound_down, R.id.dsp_sound_left, R.id.dsp_sound_right};
    private final int size = btnKey.length;

    private int location;
    private int[] sounds = new int[4];
    private final float APS = 30 / 80f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sound);
        initView();
        initData();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initView() {
        aps_sound_range = findViewById(R.id.aps_sound_range);
        mHighSeekbar = findViewById(R.id.seekbar_high);
        mTvDefault = findViewById(R.id.tv_default);
        mLowSeekBar = findViewById(R.id.seekbar_low);
        aps_car_ball = findViewById(R.id.aps_car_ball);
        mIvLoudness = findViewById(R.id.sb_loudness);
        aps_sound_range.setOnTouchListener(this);

        for (int i = 0; i < size; i++) {
            findViewById(btnKey[i]).setOnTouchListener(this);
        }

        buttons = new TextView[btnId.length];
        for (int i = 0; i < btnId.length; i++) {
            buttons[i] = findViewById(btnId[i]);
            buttons[i].setOnClickListener(this);
        }

        mHighSeekbar.setOnSeekBarChangeListener(this);
        mLowSeekBar.setOnSeekBarChangeListener(this);
        mTvDefault.setOnClickListener(this);
        mIvLoudness.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (!isFirst) {
                return;
            }
            mLoudnessOpen = checked;
            loudnessSwitch();
        });
    }

    protected void initData() {
        HandlerThread handlerThread = new HandlerThread("SoundFragment");
        handlerThread.start();
        soundHandler = new SoundHandler(handlerThread.getLooper());

        ball = ApsStation.getSoundData(this, ApsStation.NAME_LAYOUT);
        location = ToolClass.getLocationFlag(this);

        int[] apsGainRange = null;
        try {
            apsGainRange = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETBANDLEVELRANGE.code, null);
        }catch (Exception e){
            LogUtil.e("Exception = " + e.toString());
        }
        if (apsGainRange == null){
            LogUtil.e("apsGainRange is null");
            apsGainRange = ToolClass.apsGainRange.clone();
        }

        if (apsGainRange.length > 1) {
            LogUtil.i("apsGainRange[0] = " + apsGainRange[0] + " apsGainRange[1] = " + apsGainRange[1]);
            gainMax = apsGainRange[1] - apsGainRange[0];
        }

        mHighSeekbar.setMax(gainMax);
        mLowSeekBar.setMax(gainMax);

        int bass = ToolClass.getBassGain(this);
        int treble = ToolClass.getTrebleGain(this);
        mHighSeekbar.setProgress(treble);
        mLowSeekBar.setProgress(bass);
        apsSound = new int[4][4];
        mLoudnessOpen = ToolClass.getLoudnessGain(this) == 1;
        mIvLoudness.setChecked(mLoudnessOpen);
        soundRange = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETSOUNDFIELDRANGE.code, null);
        if (soundRange == null) {
            soundRange = ApsData.DefaultData.soundRange.clone();
        }
        LogUtil.d("soundRange[0] = " + soundRange[0] + " soundRange[1] = " + soundRange[1]);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFirst = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        ToolClass.setLocationFlag(this, location);
    }

    private void init() {
        aps_sound_range.postDelayed(() -> {
            range_w = aps_sound_range.getWidth();
            range_h = aps_sound_range.getHeight();
            ball_w = aps_car_ball.getWidth();
            ball_h = aps_car_ball.getHeight();
            center_w = range_w / 2f;
            center_h = range_h / 2f;
            dataScale_w = (center_w - ball_w / 2f) / (soundRange[1] - soundRange[0]);
            dataScale_h = (center_h - ball_h / 2f) / (soundRange[1] - soundRange[0]);
            LogUtil.d("ball_w = " + ball_w + " ball_h = " + ball_h);
            LogUtil.d("range_w = " + range_w + " range_h = " + range_h);
            LogUtil.d("dataScale_w = " + dataScale_w + " dataScale_h = " + dataScale_h);

            setFixedValue();

            runOnUiThread(() -> {
                LogUtil.i("location = " + location);
                buttons[location].setSelected(true);
                if (location >= 4) {
                    setSounds();
                    setLayout();
                } else {
                    if (ball == null) {
                        ball = new int[4];
                        saveBallData(apsSound[2]);
                    } else {
                        setLayout(false, true);
                    }
                }
            });
        }, 70);
    }

    /**
     * 显示用户模式 sound
     */
    private void setSounds() {
        sounds = ApsStation.getSoundData(this, location == 4 ? ApsStation.USER_MODE_1 : ApsStation.USER_MODE_2);
        LogUtil.i(Arrays.toString(sounds));
        if (sounds == null) {
            sounds = new int[]{soundRange[1], soundRange[1], soundRange[1], soundRange[1]};
            ApsStation.insertSoundToDb(this, sounds, location == 4 ? ApsStation.USER_MODE_1 : ApsStation.USER_MODE_2);
        }
        setUserBall();
    }

    private void setUserBall() {
        ball = ApsStation.getSoundData(this, location == 4 ? ApsStation.USER_MODE_LAYOUT_1 : ApsStation.USER_MODE_LAYOUT_2);
        if (ball == null) ball = apsSound[2].clone();
    }

    /**
     * 右边按键的固定值
     */
    private void setFixedValue() {
        apsSound[0] = new int[]{0, 0, ball_w, ball_h};
        apsSound[1] = new int[]{range_w - ball_w, 0, range_w, ball_h};
        apsSound[2] = new int[]{(int) center_w - ball_w / 2, (int) center_h - ball_h / 2, (int) (center_w + ball_w / 2f), (int) (center_h + ball_h / 2f)};
        apsSound[3] = new int[]{(int) center_w - ball_w / 2, range_h - ball_h, (int) (center_w + ball_w / 2f), range_h};
    }

    /**
     * 存数据
     *
     * @param l
     * @param t
     * @param r
     * @param b
     */
    private void saveBallData(int l, int t, int r, int b) {
        ball = new int[4];
        ball[0] = l;
        ball[1] = t;
        ball[2] = r;
        ball[3] = b;
        setLayoutVolumeText();
    }

    /**
     * 存数据
     *
     * @param da
     */
    private void saveBallData(int[] da) {
        ball = da.clone();
        setLayout(true, true);
    }

    /**
     * 根据点击上下左右加减按钮 数据变化及存数据
     *
     * @param viewId
     */
    private void setBallData(int viewId) {
        int data, progress = 5;
        if (viewId == R.id.dsp_sound_up) {
            data = ball[1] - progress;
            if (data < 0) data = 0;
            ball[1] = data;
            ball[3] = data + ball_h;
            setLayoutVolumeText();
        } else if (viewId == R.id.dsp_sound_down) {
            data = ball[1] + progress;
            if (data > range_h - ball_h) data = range_h - ball_h;
            ball[1] = data;
            ball[3] = data + ball_h;
            setLayoutVolumeText();
        } else if (viewId == R.id.dsp_sound_left) {
            data = ball[0] - progress;
            if (data < 0) data = 0;
            ball[0] = data;
            ball[2] = data + ball_w;
            setLayoutVolumeText();
        } else if (viewId == R.id.dsp_sound_right) {
            data = ball[0] + progress;
            if (data > range_w - ball_w) data = range_w - ball_w;
            ball[0] = data;
            ball[2] = data + ball_w;
            setLayoutVolumeText();
        }
    }

    /**
     * 设置小球显示位置 发送功能数据 保存数据
     */
    private void setLayoutVolumeText() {
        setLayout(true, true);
    }

    /**
     * 设置小球显示位置
     */
    private void setLayout() {
        LogUtil.i("l = " + ball[0] + " t = " + ball[1] + " r = " + ball[2] + " b = " + ball[3]);
        aps_car_ball.layout(ball[0], ball[1], ball[2], ball[3]);
    }

    /**
     * 设置小球显示位置 发送功能数据 保存数据
     *
     * @param send
     */
    private void setLayout(boolean send, boolean sound) {
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
            sounds[0] = (int) (lf / dataScale_h);
            sounds[1] = (int) (rf / dataScale_h);
            sounds[2] = (int) (lr / dataScale_w);
            sounds[3] = (int) (rr / dataScale_w);
        }
        if (!send) return;
        LogUtil.i("sound[0] = " + sounds[0] + " sound[1] = " + sounds[1] + " sound[2] = " + sounds[2] + " sound[3] = " + sounds[3]);
        AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETLFOUTFADER.code, new int[]{sounds[0]}, 1);
        AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETRFOUTFADER.code, new int[]{sounds[1]}, 1);
        AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETLROUTFADER.code, new int[]{sounds[2]}, 1);
        AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETRROUTFADER.code, new int[]{sounds[3]}, 1);

        if (location == 4) {
            saveSQL(ApsStation.USER_MODE_LAYOUT_1, ApsStation.USER_MODE_1);
        } else if (location == 5) {
            saveSQL(ApsStation.USER_MODE_LAYOUT_2, ApsStation.USER_MODE_2);
        } else {
            saveSQL(ApsStation.NAME_LAYOUT, ApsStation.NAME_SEND);
        }
    }

    private void saveSQL(int layout, int send) {
        ApsStation.deleteSoundInDb(this, layout);
        ApsStation.deleteSoundInDb(this, send);
        ApsStation.insertSoundToDb(this, ball, layout);
        ApsStation.insertSoundToDb(this, sounds, send);
    }

    /**
     * 判断是否存在该位置
     *
     * @return
     */
    private int isExist() {
        int i, j;
        boolean exist;
        for (i = 0; i < apsSound.length; i++) {
            exist = false;
            for (j = 0; j < ball.length; j++) {
                if (apsSound[i][j] >= ball[j] + 2 || apsSound[i][j] <= ball[j] - 2) {
                    break;
                }
                if (j == ball.length - 1) exist = true;
            }
            if (exist) return i;
        }
        return -1;
    }

    /**
     * 上下左右按钮 高亮
     *
     * @param view
     * @param flag
     */
    private void setBallBg(ImageView view, boolean flag) {
        int id = view.getId();
        if (id == R.id.dsp_sound_up) {
            view.setImageResource(flag ? R.drawable.dsp_sound_up_d_new : R.drawable.dsp_sound_up_new);
        } else if (id == R.id.dsp_sound_down) {
            view.setImageResource(flag ? R.drawable.dsp_sound_down_d_new : R.drawable.dsp_sound_down_new);
        } else if (id == R.id.dsp_sound_left) {
            view.setImageResource(flag ? R.drawable.dsp_sound_left_d_new : R.drawable.dsp_sound_left_new);
        } else if (id == R.id.dsp_sound_right) {
            view.setImageResource(flag ? R.drawable.dsp_sound_right_d_new : R.drawable.dsp_sound_right_new);
        }
    }

    /**
     * 长按事件
     *
     * @param view
     * @param action
     */
    private void setLongClick(View view, int action) {
        if (action == MotionEvent.ACTION_DOWN) {
            isSoundLong = true;
            setBallBg((ImageView) view, true);
            soundHandler.removeMessages(APS_SOUND_LONG);
            Message msg = Message.obtain();
            msg.what = APS_SOUND_LONG;
            msg.obj = view.getId();
            soundHandler.sendMessageDelayed(msg, 1000);
        } else if (action == MotionEvent.ACTION_UP) {
            isSoundLong = false;
            setBallBg((ImageView) view, false);
            soundHandler.removeMessages(APS_SOUND_LONG);
            setBallData(view.getId());
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (NoFastClickUtils.isFastClickSpeaker()) return;
        if (v.getId() == R.id.tv_default) {
            resetConfig();
        } else {
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i] == v) {
                    location = i;
                    buttons[i].setSelected(true);
                    if (i > 3) {
                        setSounds();
                        setLayout(true, false);
                        LogUtil.i("click the user mode 1 sounds = " + Arrays.toString(sounds));
                    } else {
                        saveBallData(apsSound[i]);
                    }
                    ToolClass.setLocationFlag(this, location);
                } else {
                    buttons[i].setSelected(false);
                }
            }
        }
    }

    private void loudnessSwitch() {
        mIvLoudness.setChecked(mLoudnessOpen);
        LogUtil.d("Loudness =" + mLoudnessOpen);
        if (mLoudnessOpen){
            AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETLOUDNESSGAIN.code, new int[]{1}, 1);
            ToolClass.setLoudnessGain(this, 1);
        } else {
            AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETLOUDNESSGAIN.code, new int[]{0}, 1);
            ToolClass.setLoudnessGain(this, 0);
        }
    }

    private void resetConfig() {
        // 响度开
        mLoudnessOpen = true;
        mIvLoudness.setChecked(true);
        AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETLOUDNESSGAIN.code, new int[]{0}, 1);
        ToolClass.setLoudnessGain(this, 1);
        // 高音
        mHighSeekbar.setProgress(gainMax/2);
        ToolClass.setTrebleGain(this, gainMax/2);
        saveGain(2, gainMax/2);
        mLowSeekBar.setProgress(gainMax/2);
        ToolClass.setBassGain(this, gainMax/2);
        saveGain(1, gainMax/2);
        // 模式
        soundRange = ApsData.DefaultData.soundRange.clone();
        ball = new int[4];
        location = 2;
        for (int i = 0; i < buttons.length; i ++) {
            if (i == location) {
                buttons[location].setSelected(true);
            } else {
                buttons[i].setSelected(false);
            }
        }
        ToolClass.setLocationFlag(this, location);
        saveBallData(apsSound[2]);
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
                if (touch_y > range_h - ball_h / 2) {
                    touch_y = range_h - ball_h / 2;
                }
                saveBallData(touch_x - ball_w / 2, touch_y - ball_h / 2, touch_x + ball_w / 2, touch_y + ball_h / 2);
            }
        } else {
            setLongClick(v, event.getAction());
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;
        if (seekBar == mHighSeekbar) {
            ToolClass.setTrebleGain(this, progress);
            saveGain(2, progress);
        } else if (seekBar == mLowSeekBar) {
            ToolClass.setBassGain(this, progress);
            saveGain(1, progress);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void saveGain(int index, int progress) {
        int[] gains = new int[2];
        gains[0] = index;
        gains[1] = progress - gainMax / 2;
        LogUtil.d(Arrays.toString(gains));
        AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETBANDLEVEL.code, gains, 2);
    }

    private class SoundHandler extends Handler {

        public SoundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            int APS_SOUND_ADJUST = 0x20;
            if (what == APS_SOUND_LONG) {
                int status = (int) msg.obj;
                soundHandler.obtainMessage(APS_SOUND_ADJUST, status).sendToTarget();
            } else if (what == APS_SOUND_ADJUST) {
                int status = (int) msg.obj;
                LogUtil.i("status = " + status);
                int index = 30;
                if (status == R.id.dsp_sound_left || status == R.id.dsp_sound_right) {
                    index = range_w;
                } else if (status == R.id.dsp_sound_up || status == R.id.dsp_sound_down) {
                    index = range_h;
                }
                LogUtil.d("index = " + index);
                while (index > 0) {
                    try {
                        runOnUiThread(() -> setBallData(status));
                        Thread.sleep(30);
                        if (!isSoundLong) break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    index--;
                }
            }
        }
    }
}
