package com.awell.app.ui.equal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

public class SoundEqualFragment extends Fragment implements Contract.EqualView {
    private final ArrayList<Integer> list = new ArrayList<>();
    private FragmentSoundEqualBinding mBinding;
    /**
     * apsGain:当前seekbar的值
     * apsFreq:底部HZ显示值
     */
    private int[] apsGain, apsFreq;
    private int mCurrentType = 0;
    private int gainMax = 0;
    private int[] mUserGain;
    private int[][] mDataArray;
    private Contract.EqualPresenter mPresenter;
    private SharedPreferences sp;

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
        sp = requireContext().getSharedPreferences("eq_gain", Context.MODE_PRIVATE);
        int[] apsGainRange = null;
        try {
            apsGainRange = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETBANDLEVELRANGE.code, null);
        }catch (Exception e){
            LogUtil.e("Exception = " + e);
        }
        if (apsGainRange == null){
            LogUtil.e("apsGainRange is null");
            apsGainRange = ToolClass.apsGainRange.clone();
        }

        if (apsGainRange.length > 1) {
            LogUtil.i("apsGainRange[0] = " + apsGainRange[0] + " apsGainRange[1] = " + apsGainRange[1]);
            gainMax = apsGainRange[1] - apsGainRange[0];
        }
        mBinding.waveview.setMaxGain(gainMax);
        mPresenter = new EqualPresenterImpl();
        mPresenter.setContext(requireContext());
        mPresenter.setView(this);
        mPresenter.initData();
    }

    @SuppressLint("SetTextI18n")
    private void updateSeekBar(int[] apsGain, boolean changeSeekbar) {
        mBinding.layoutSeekbar.removeAllViews();
        for (int i = 0; i < apsFreq.length; i++) {
            View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_item_seekbar, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 1;
            view.setLayoutParams(layoutParams);
            mBinding.layoutSeekbar.addView(view);
            VerticalSeekBar seekBar = view.findViewById(R.id.seekbar);
            TextView tvValue = view.findViewById(R.id.tv_value);
            TextView tvHz = view.findViewById(R.id.tv_hz);
            seekBar.setMax(gainMax);
            seekBar.setProgress(apsGain[i]);
            tvValue.setText("" + (apsGain[i] - gainMax/2));
            if (apsFreq != null) {
                int apsHz = apsFreq[i];
                if (apsHz < 1000) {
                    tvHz.setText(apsFreq[i] + "Hz");
                } else {
                    float hzValue = new BigDecimal(apsHz).divide(new BigDecimal(1000), 1, RoundingMode.DOWN)
                            .floatValue();
                    tvHz.setText(hzValue + "kHz");
                }
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
                    LogUtil.i("type = " + mCurrentType + " ---- index = " + index + " ---- progress = " + progress);
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
        if (index >= list.size()/2) {
            // 高音
            mUserGain[1] = progress;
            ToolClass.setTrebleGain(requireContext(), progress);
        } else {
            // 低音
            mUserGain[0] = progress;
            ToolClass.setBassGain(requireContext(), progress);
        }
        LogUtil.d("type = " + mCurrentType + " index = " + index + " progress = " + progress + " userGain = " + Arrays.toString(mUserGain));
        if (updateWaveView) {
            mBinding.waveview.updateList(list);
        }
        saveGain(index, progress);
        int high = 0;
        if (index >= ApsData.DefaultData.apsFreqSend.length/2) {
            high = 1;
        }
        saveGain(mCurrentType, high, progress);
        View itemView = mBinding.layoutSeekbar.getChildAt(index);
        TextView tvValue = itemView.findViewById(R.id.tv_value);
        tvValue.setText("" + (progress - gainMax/2));
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
            if (apsGainCustom == null) {
                apsGainCustom = mDataArray[position];
            }
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
        int size = ApsData.DefaultData.apsFreqSend.length;
        if (mUserGain == null) {
            mUserGain = new int[]{ToolClass.getBassGain(requireContext()), ToolClass.getTrebleGain(requireContext())};
        }
        for (int i = 0; i < size; i++) {
            int gain = data[i];
            list.add(gain);
            if (mCurrentType == 0) {
                if (i == size - 1) {
                    gain = mUserGain[1];
                    saveGain(i, gain);
                } else if (i == (size/2) - 1){
                    gain = mUserGain[0];
                    saveGain(i, gain);
                }
            } else if (mCurrentType == 6 || mCurrentType == 7) {
                // 只有2位数字是真正有效的，所以取第一段、第二段结尾数字
                if (i == size - 1 || i == (size/2) - 1) {
                    saveGain(i, gain);
                }
            }
        }
        int lowGain = 0;
        int highGain = 0;
        switch (mCurrentType) {
            case 1: // 标准
                lowGain = 14;
                highGain = 14;
                sendGain(lowGain, highGain);
                break;
            case 2: // 爵士
                lowGain = 4;
                highGain = 28;
                sendGain(lowGain, highGain);
                break;
            case 3: // 流行
                lowGain = 20;
                highGain = 19;
                sendGain(lowGain, highGain);
                break;
            case 4: // 摇滚
                lowGain = 28;
                highGain = 24;
                sendGain(lowGain, highGain);
                break;
            case 5: // 古典
                lowGain = 26;
                highGain = 14;
                sendGain(lowGain, highGain);
                break;
        }
        LogUtil.i("curType = " + mCurrentType + " low = " + lowGain + " high = " + highGain);
        updateSeekBar(data, mCurrentType == 0);
        mBinding.waveview.updateList(list);
    }

    private void sendGain(int lowValue, int highValue) {
        // 低音
        int[] lowGains = new int[2];
        lowGains[0] = 1;
        lowGains[1] = lowValue - gainMax / 2;
        LogUtil.d(Arrays.toString(lowGains));
        AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETBANDLEVEL.code, lowGains, 2);
        // 高音
        int[] highGains = new int[2];
        highGains[0] = 2;
        highGains[1] = highValue - gainMax / 2;
        LogUtil.d(Arrays.toString(highGains));
        AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETBANDLEVEL.code, highGains, 2);
    }

    /**
     * 发送增益更新数据库值
     *
     * @param index
     */
    private void saveGain(int index, int progress) {
        ApsStation.updateApsInDb(requireContext(), index, progress, ApsStation.NAME_GAIN);
        int[] gains = new int[2];
        int gainIndex = 1;
        if (index >= ApsData.DefaultData.apsFreqSend.length/2) {
            // 高音
            gainIndex = 2;
        }
        gains[0] = gainIndex;
        gains[1] = progress - gainMax / 2;
        LogUtil.d(Arrays.toString(gains));
        AwellAudio.setIntParameter(Constant.IAUDIOCONTROL.CMD.SETBANDLEVEL.code, gains, 2);
    }

    @Override
    public void setData(int[][] data) {
        mDataArray = data;
        apsFreq = AwellAudio.getIntParameter(Constant.IAUDIOCONTROL.CMD.GETBANDS.code, null);
        if (apsFreq == null || apsFreq.length != ApsData.getInstance().apsFreq.length){
            LogUtil.e("apsFreq is null ");
            apsFreq = ApsData.getInstance().apsFreq.clone();
        }
        final int position = ToolClass.getTypeFlag(requireContext());
        apsGain = ApsStation.getApsGain(getContext(), ApsStation.NAME_GAIN);
        LogUtil.i("apsGain = " + Arrays.toString(apsGain));
        if (apsGain == null) {
            apsGain = mDataArray[0];
            ApsStation.insertApsToDb(requireContext(), apsGain, ApsStation.NAME_GAIN);
            ApsStation.insertApsToDb(requireContext(), apsGain, ApsStation.NAME_GAIN_CUSTOM);
        }
        setType(position);
    }

    private void saveGain(int type, int high, int gain) {
        sp.edit().putInt(type + "_" + high, gain).apply();
    }

    private int getLow(int def) {
        return sp.getInt(mCurrentType + "_" + 0, def);
    }

    private int getHigh(int def) {
        return sp.getInt(mCurrentType + "_" + 1, def);
    }

}
