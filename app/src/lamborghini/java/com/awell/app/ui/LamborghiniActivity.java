package com.awell.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.awell.app.R;
import com.awell.app.databinding.ActivityLamborghiniBinding;
import com.awell.app.utils.ToolClass;

public class LamborghiniActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityLamborghiniBinding mBinding;
    private int mCurrentTypePosition = 0;
    private String[] mApsType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLamborghiniBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initView();
        initData();
    }

    private void initData() {
        mApsType = getResources().getStringArray(R.array.dsp_aps_type);
        mCurrentTypePosition = ToolClass.getTypeFlag(this);
        mBinding.tvType.setText(mApsType[mCurrentTypePosition]);
    }

    private void initView() {
        mBinding.tvMusic.setOnClickListener(this);
        mBinding.ivPrev.setOnClickListener(this);
        mBinding.tvBal.setOnClickListener(this);
        mBinding.tvEq.setOnClickListener(this);
        mBinding.ivNext.setOnClickListener(this);
        mBinding.ivRest.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBinding != null) {
            initData();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.tv_music:
                intent  = getPackageManager().getLaunchIntentForPackage("com.awell.localmusic");
                startActivity(intent);
                break;
            case R.id.tv_bal:
                intent = new Intent(this, BallActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_eq:
                intent = new Intent(this, EqActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_rest:
                mCurrentTypePosition = 0;
                mBinding.tvType.setText(mApsType[mCurrentTypePosition]);
                break;
            case R.id.iv_prev:
                updateType(false);
                break;
            case R.id.iv_next:
                updateType(true);
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
        ToolClass.setTypeFlag(this, mCurrentTypePosition);
        mBinding.tvType.setText(mApsType[mCurrentTypePosition]);
    }

}
