package com.awell.app.windows;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.awell.app.R;
import com.awell.app.databinding.LayoutEqualizerWindowBinding;

public class EqualizerTypeWindow extends PopupWindow implements View.OnClickListener {
    private LayoutEqualizerWindowBinding mBinding;
    private Context mContext;
    private String[] mApsType;

    public void setSelectedPosition(int mSelectedPosition) {
        for (int i = 0; i < mApsType.length; i++) {
            View itemView = mBinding.layoutType.getChildAt(i);
            itemView.setSelected(i == mSelectedPosition);
        }
    }

    public void setListener(OnEqualizerTypeClickListener mListener) {
        this.mListener = mListener;
    }

    private OnEqualizerTypeClickListener mListener;

    public EqualizerTypeWindow(Context context) {
        super(context);
        mContext = context;
        View contentView = LayoutInflater.from(context).inflate(R.layout.layout_equalizer_window, null);
        setContentView(contentView);
        initView(contentView);
    }

    private void initView(View contentView) {
        mBinding = LayoutEqualizerWindowBinding.bind(contentView);
        mApsType = mContext.getResources().getStringArray(R.array.dsp_aps_type);
        mBinding.layoutType.removeAllViews();
        for (int i = 0; i < mApsType.length; i++) {
            ViewGroup itemView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.layout_item_equalizer_type, null);
            mBinding.layoutType.addView(itemView);
            TextView textView = (TextView) itemView.getChildAt(0);
            itemView.setTag(i);
            textView.setText(mApsType[i]);
            itemView.setOnClickListener(this);
            if (i == mApsType.length - 1) {
                itemView.getChildAt(1).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int position = (int) view.getTag();
        for (int i = 0; i < mBinding.layoutType.getChildCount(); i++) {
            mBinding.layoutType.getChildAt(i).setSelected(i == position);
        }
        if (mListener != null) {
            mListener.onEqualizerTypeClick(position, mApsType[position]);
        }
        dismiss();
    }


    public interface OnEqualizerTypeClickListener {
        void onEqualizerTypeClick(int position, String type);
    }

}

