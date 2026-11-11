package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.DashPathEffect;
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

        float high = ToolClass.dp2px(requireContext(), 300f);
        waveview.setShowVerticalLine(true);
        waveview.setShowHorizontalLine(true);
        waveview.setShowTopValue(false);
        waveview.setTopOffset(ToolClass.dp2px(requireContext(), 30f));
        waveview.setPaddingBottom(ToolClass.dp2px(requireContext(), 36f));
        // 渐变、边框、网格线用 car_50 的配色覆盖父类默认样式
        waveview.setWaveFillGradient(new int[]{Color.parseColor("#C3E5FE"),
                Color.TRANSPARENT}, high, 0);
        Paint borderPaint = waveview.getBorderPaint();
        borderPaint.setColor(Color.parseColor("#ffe866"));
        Paint gridPaint = waveview.getGridPaint();
        // 0.5dp 在 mdpi 上只有半个像素，加上白色在浅色背景上几乎不可见
        gridPaint.setStrokeWidth(ToolClass.dp2px(requireContext(), 1f));
        gridPaint.setColor(Color.parseColor("#9FB0C4"));
        // 上面这些是绘制参数，设置完主动重绘一次，避免首帧还是父类默认样式
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
