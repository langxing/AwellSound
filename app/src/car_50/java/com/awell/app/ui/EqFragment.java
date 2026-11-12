package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.awell.app.R;
import com.awell.app.ui.equal.SoundEqualFragment;
import com.awell.app.utils.LogUtil;
import com.awell.app.utils.ToolClass;
import com.awell.app.windows.EqualizerTypeWindow;

import java.util.Arrays;

public class EqFragment extends SoundEqualFragment implements View.OnClickListener, EqualizerTypeWindow.OnEqualizerTypeClickListener {
    private String[] mApsType;
    private View layoutCustom;
    private TextView tvCustom;
    private ImageView ivCustom;

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

        int gridW = requireContext().getResources().getDisplayMetrics().widthPixels;
        waveview.setShowVerticalLine(true);
        waveview.setShowHorizontalLine(true);
        waveview.setShowTopValue(false);
        // car_50 浅色背景，白粒子看不清，用强调蓝
        waveview.setParticleColor(Color.parseColor("#1769FF"));
        // car_50 不画波浪顶部描边，其他 flavor 保持默认的 true
        waveview.setShowTopOutline(false);
        waveview.setTopOffset(ToolClass.dp2px(requireContext(), 30f));
        waveview.setPaddingBottom(ToolClass.dp2px(requireContext(), 30f));
        // 渐变、网格线用 car_50 的配色覆盖父类默认样式
        // setWaveFillGradient(colors, heightPx, widthPx)：heightPx=0、widthPx=网格宽 → 左到右渐变
        waveview.setWaveFillGradient(new int[]{Color.parseColor("#C3E5FE"),
                Color.parseColor("#C0E9FF"), Color.parseColor("#D2D3FF")}, 0, gridW);
        Paint gridPaint = waveview.getGridPaint();
        // 0.5dp 在 mdpi 上只有半个像素，加上白色在浅色背景上几乎不可见
        gridPaint.setStrokeWidth(ToolClass.dp2px(requireContext(), 0.5f));
        gridPaint.setColor(Color.parseColor("#8AA2BE"));
        // 设置完主动重绘一次，避免首帧还是父类默认样式
        waveview.invalidate();

        layoutCustom = view.findViewById(R.id.layout_custom);
        tvCustom = view.findViewById(R.id.tv_custom);
        ivCustom = view.findViewById(R.id.iv_custom);
        layoutCustom.setOnClickListener(this);

        // 父类已根据保存的旗标恢复当前模式到 mCurrentType
        mApsType = getResources().getStringArray(R.array.dsp_aps_type);
        tvCustom.setText(mApsType[mCurrentType]);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setData(int[][] data) {
        super.setData(data);
        LogUtil.i("freq = " + Arrays.toString(apsFreq));
        waveview.updateFreq(apsFreq);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.layout_custom) {
            showTypeWindow();
        }
    }

    private void showTypeWindow() {
        ivCustom.setRotation(0);
        EqualizerTypeWindow typeWindow = new EqualizerTypeWindow(requireContext());
        typeWindow.setSelectedPosition(mCurrentType);
        typeWindow.setListener(this);
        typeWindow.showAsDropDown(layoutCustom, 0, 0, Gravity.START);
    }

    @Override
    public void onEqualizerTypeClick(int position, String type) {
        ivCustom.setRotation(180);
        tvCustom.setText(type);
        if (mCurrentType != position) {
            setType(position);
        }
    }

}
