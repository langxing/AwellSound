package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.awell.app.R;
import com.awell.app.ui.equal.SoundEqualFragment;
import com.awell.app.utils.LogUtil;
import com.awell.app.utils.ToolClass;
import com.awell.kpslibrary.Constant;
import com.awell.kpslibrary.module.AwellAudio;

import java.util.Arrays;

public class EqFragment extends SoundEqualFragment implements View.OnClickListener {
    private SwitchCompat mIvLoudness;
    private TextView tvLoudness;
    private ImageView ivPrev;
    private ImageView ivNext;
    private TextView tvType;
    private TextView tvReset;
    private TextView tvMax;
    private TextView tvMin;
    private String[] mApsType;
    private boolean isFirst = false;
    private boolean mLoudnessOpen = false;
    private int mCurrentTypePosition = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mContentView == null) {
            mContentView = inflater.inflate(R.layout.fragment_eq, container, false);
        }
        return mContentView;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIvLoudness = mContentView.findViewById(R.id.sb_loudness);
        tvLoudness = mContentView.findViewById(R.id.tv_loudness);
        ivPrev = mContentView.findViewById(R.id.iv_prev);
        ivNext = mContentView.findViewById(R.id.iv_next);
        tvType = mContentView.findViewById(R.id.tv_type);
        tvReset = mContentView.findViewById(R.id.tv_reset);
        tvMax = mContentView.findViewById(R.id.tv_max);
        tvMin = mContentView.findViewById(R.id.tv_min);
        tvLoudness.setSelected(true);
        waveview.setShowVerticalLine(true);
        waveview.setShowTopValue(true);
        waveview.setTopOffset(ToolClass.dp2px(requireContext(), 30f));
        waveview.setPaddingBottom(ToolClass.dp2px(requireContext(), 30f));
        float high = ToolClass.dp2px(requireContext(), 300f);
        Paint borderPaint = waveview.getBorderPaint();
        borderPaint.setColor(Color.parseColor("#ffe866"));
        Paint gridPaint = waveview.getGridPaint();
        gridPaint.setPathEffect(new DashPathEffect(new float[]{ToolClass.dp2px(requireContext(), 3f),
                ToolClass.dp2px(requireContext(), 3f)}, 0f));
        gridPaint.setColor(Color.parseColor("#ffffff"));
        ivPrev.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        tvReset.setOnClickListener(this);
        // 2. 画渐变
        waveview.setWaveFillGradient(new int[]{Color.parseColor("#66ffe866"),
                Color.TRANSPARENT}, high, 0);
        waveview.setOnGainChange((index, progress) -> {
            try {
                setDataView(index, progress, false);
            } catch (Exception e) {
                LogUtil.e("onGainChange："+ e.getMessage());
            }
            return null;
        });
        mIvLoudness.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (!isFirst) {
                return;
            }
            mLoudnessOpen = checked;
            loudnessSwitch();
        });
        mLoudnessOpen = ToolClass.getLoudnessGain(requireContext()) == 1;
        mIvLoudness.setChecked(mLoudnessOpen);
        tvMax.setText("+" + gainMax/2);
        tvMin.setText("-" + gainMax/2);
        initData();
    }

    private void initData() {
        mApsType = getResources().getStringArray(R.array.dsp_aps_type);
        mCurrentTypePosition = ToolClass.getTypeFlag(requireContext());
        updateWave(mCurrentTypePosition);
    }

    private void updateWave(int position) {
        setType(mCurrentTypePosition);
        tvType.setText(mApsType[mCurrentTypePosition]);
        waveview.setNeedIntercept(position == 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        isFirst = true;
    }

    private void loudnessSwitch() {
        LogUtil.d("Loudness =" + mLoudnessOpen);
        if (mLoudnessOpen){
            AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETLOUDNESSGAIN.code, new int[]{1}, 1);
            ToolClass.setLoudnessGain(requireContext(), 1);
        } else {
            AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETLOUDNESSGAIN.code, new int[]{0}, 1);
            ToolClass.setLoudnessGain(requireContext(), 0);
        }
    }

    @Override
    protected void updateSeekBar(int[] apsGain, boolean changeSeekbar) {}

    @SuppressLint("SetTextI18n")
    @Override
    public void setData(int[][] data) {
        super.setData(data);
        LogUtil.i("freq = " + Arrays.toString(apsFreq));
        waveview.updateFreq(apsFreq);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_prev:
                updateType(false);
                break;
            case R.id.iv_next:
                updateType(true);
                break;
            case R.id.tv_reset:
                mCurrentTypePosition = 0;
//                tvType.setText(mApsType[mCurrentTypePosition]);
//                setType(mCurrentTypePosition);
                updateWave(mCurrentTypePosition);
                break;
            default:
                break;
        }
    }

    private void updateType(boolean isNext) {
        if (isNext) {
            if (mCurrentTypePosition < mApsType.length - 1) {
                mCurrentTypePosition ++;
            } else {
                mCurrentTypePosition = 0;
            }
        } else {
            if (mCurrentTypePosition > 0) {
                mCurrentTypePosition --;
            } else {
                mCurrentTypePosition = mApsType.length - 1;
            }
        }
        updateWave(mCurrentTypePosition);
//        tvType.setText(mApsType[mCurrentTypePosition]);
//        setType(mCurrentTypePosition);
    }

}
