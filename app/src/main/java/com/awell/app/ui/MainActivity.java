package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.awell.app.R;
import com.awell.app.adapter.MainFragmentAdapter;
import com.awell.app.databinding.ActivityMainBinding;
import com.awell.app.ui.equal.SoundEqualFragment;
import com.awell.app.utils.ToolClass;
import com.awell.app.windows.EqualizerTypeWindow;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EqualizerTypeWindow.OnEqualizerTypeClickListener {
    private MainFragmentAdapter mFragmentAdapter;
    private ActivityMainBinding mBinding;
    private SoundEqualFragment mEqualFragment;
    private List<Fragment> mFragmentList = new ArrayList<>();
    private String[] mApsType;
    private int mCurrentTypePosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mFragmentAdapter = new MainFragmentAdapter(getSupportFragmentManager());
        mEqualFragment = new SoundEqualFragment();
        mFragmentList.add(mEqualFragment);
        mFragmentList.add(new SoundFragment());
        mFragmentAdapter.setFragments(mFragmentList);
        mBinding.viewPager.setCanScroll(false);
        mBinding.viewPager.setAdapter(mFragmentAdapter);
        mBinding.btnSound.setOnClickListener(this);
        mBinding.btnEqualizer.setOnClickListener(this);
        mBinding.btnEqualizer.setSelected(true);
        mBinding.layoutCustom.setOnClickListener(this);
        mApsType = getResources().getStringArray(R.array.dsp_aps_type);
        mCurrentTypePosition = ToolClass.getTypeFlag(this);
        mBinding.tvCustom.setText(mApsType[mCurrentTypePosition]);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        mBinding.btnEqualizer.setSelected(false);
        mBinding.btnSound.setSelected(false);
        switch (view.getId()) {
            case R.id.btn_equalizer:
                mBinding.btnEqualizer.setSelected(true);
                mBinding.viewPager.setCurrentItem(0);
                mBinding.layoutCustom.setVisibility(View.VISIBLE);
            break;
            case R.id.btn_sound:
                mBinding.btnSound.setSelected(true);
                mBinding.viewPager.setCurrentItem(1);
                mBinding.layoutCustom.setVisibility(View.GONE);
            break;
            case R.id.layout_custom:
                showTypeWindow();
                break;
        }
    }

    private void showTypeWindow() {
        mBinding.ivCustom.setRotation(180);
        EqualizerTypeWindow typeWindow = new EqualizerTypeWindow(this);
        typeWindow.setSelectedPosition(mCurrentTypePosition);
        typeWindow.setListener(this);
        typeWindow.showAsDropDown(mBinding.layoutCustom, 0, 0, Gravity.START);
    }

    @Override
    public void onEqualizerTypeClick(int position, String type) {
        mBinding.ivCustom.setRotation(0);
        mBinding.tvCustom.setText(type);
        if (mEqualFragment != null && mCurrentTypePosition != position) {
            mEqualFragment.setType(position);
        }
        mCurrentTypePosition = position;
    }

}
