package com.awell.app.ui.equal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.awell.app.R;
import com.awell.app.databinding.FragmentSoundEqualBinding;
import com.awell.app.model.ApsData;
import com.awell.app.utils.ApsStation;
import com.awell.app.utils.LogUtil;
import com.awell.app.utils.ToolClass;
import com.awell.kpslibrary.Constant;
import com.awell.kpslibrary.module.AwellAudio;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

import java.util.ArrayList;
import java.util.Arrays;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class SoundEqualFragment extends Fragment implements Contract.EqualView {
    private final ArrayList<Integer> list = new ArrayList<>();
    private FragmentSoundEqualBinding mBinding;
    private ApsData apsData;
    /**
     * apsGain:当前seekbar的值，为增益值 0-40范围
     * length 8
     * apsFreq:底部HZ显示值
     * length 8
     */
    private int[] apsGain, apsFreq;
    private int mCurrentType = 0;
    private int[][] mDataArray;
    private Contract.EqualPresenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mBinding == null) {
            mBinding = FragmentSoundEqualBinding.inflate(inflater, container, false);
        }
        return mBinding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.waveview.setMaxGain(ApsData.gainMax);
        mBinding.waveview.setOnGainChange((index, progress) -> {
            try {
                View seekbarLayout = mBinding.layoutSeekbar.getChildAt(index);
                if (seekbarLayout != null) {
                    VerticalSeekBar seekBar = seekbarLayout.findViewById(R.id.seekbar);
                    seekBar.setProgress(progress);
                    setDataView(index, progress, false);
                }
            } catch (Exception e) {
                LogUtil.e("onGainChange："+ e.getMessage());
            }
            return null;
        });
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        mPresenter = new EqualPresenterImpl();
        mPresenter.setContext(requireContext());
        mPresenter.setView(this);
        mPresenter.initData();
    }

    @SuppressLint("SetTextI18n")
    private void updateSeekBar(int[] apsGain, boolean changeSeekbar) {
        mBinding.layoutSeekbar.removeAllViews();
        for (int i = 0; i < apsGain.length; i++) {
            View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_item_seekbar, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 1;
            view.setLayoutParams(layoutParams);
            view.setLayoutParams(layoutParams);
            mBinding.layoutSeekbar.addView(view);
            VerticalSeekBar seekBar = view.findViewById(R.id.seekbar);
            TextView tvValue = view.findViewById(R.id.tv_value);
            TextView tvHz = view.findViewById(R.id.tv_hz);
            tvValue.setText(apsGain[i] + "");
            seekBar.setMax(ApsData.gainMax);
            seekBar.setProgress(apsGain[i]);
            if (apsFreq != null) {
                tvHz.setText(apsFreq[i] + "Hz");
            }
            seekBar.setEnabled(changeSeekbar);
            mBinding.waveview.setNeedIntercept(changeSeekbar);
            if (changeSeekbar) {
                setSeekbarListener(seekBar, i);
            }
        }
    }

    private void setSeekbarListener(SeekBar seekBar, int index) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setDataView(index, progress, true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * 设置增益
     *
     * @param index
     * @param progress
     */
    @SuppressLint("SetTextI18n")
    private void setDataView(int index, int progress, boolean updateWaveView) {
        list.set(index, progress);
        if (updateWaveView) {
            mBinding.waveview.updateList(list);
        }
        setPlayGain(index, progress);
        View itemView = mBinding.layoutSeekbar.getChildAt(index);
        TextView tvValue = itemView.findViewById(R.id.tv_value);
        tvValue.setText(progress + "");
        ApsStation.updateApsInDb(requireContext(), index, progress, ApsStation.NAME_GAIN_CUSTOM);
    }

    /**
     * 设置当前模式
     */
    public void setType(int position) {
        LogUtil.i("position = " + position);
        mCurrentType = position;
        if (position == 0) {
            int[] apsGainCustom = ApsStation.getApsGain(requireContext(), ApsStation.NAME_GAIN_CUSTOM);
            setData(apsGainCustom, false);
        } else {
            setData(mDataArray[position], false);
        }
        ToolClass.setTypeFlag(requireContext(), position);
    }

    /**
     * 设置指定增益
     *
     * @param save
     */
    public void setData(int[] data, boolean save) {
        Context context = requireContext();
        if (save) {
            ApsStation.deleteApsInDb(context, ApsStation.NAME_GAIN_REAR);
            ApsStation.insertApsToDb(context, data, ApsStation.NAME_GAIN_REAR);
        }
        list.clear();
        updateSeekBar(data, mCurrentType == 0);
        for (int i = 0; i < data.length; i++) {
            setPlayGain(i, data[i]);
            list.add(data[i]);
        }
        mBinding.waveview.updateList(list);
    }

    /**
     * 发送增益更新数据库值
     *
     * @param index
     * @param gain
     */
    private void setPlayGain(int index, int gain) {
        ApsStation.updateApsInDb(requireContext(), index, gain, ApsStation.NAME_GAIN);
        int freq = apsData.getApsFreqSend()[index];
        LogUtil.i("index = " + index + "  freq = " + freq + " gain = " + gain);
        AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETBANDLEVEL.code, new int[]{freq, gain, 1}, 4);
    }

    @Override
    public void setData(int[][] data) {
        mDataArray = data;
        apsData = ApsData.getInstance();
        LogUtil.i(Arrays.toString(apsData.getApsFreqSend()));//获取实际频率
        apsFreq = apsData.apsFreq.clone();
        final int position = ToolClass.getTypeFlag(requireContext());
        apsGain = ApsStation.getApsGain(getContext(), ApsStation.NAME_GAIN);
        LogUtil.i("apsGain = " + Arrays.toString(apsGain));
        if (apsGain == null) {
            apsGain = mDataArray[0];
            ApsStation.insertApsToDb(requireContext(), apsGain, ApsStation.NAME_GAIN);
            ApsStation.insertApsToDb(requireContext(), apsGain, ApsStation.NAME_GAIN_REAR);
        }
        for (int gain : apsGain) {
            list.add(gain);
        }
        setType(position);
    }

}
