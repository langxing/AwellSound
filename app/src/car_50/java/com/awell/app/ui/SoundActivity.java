package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.awell.app.R;
import com.awell.app.databinding.ActivityMain50Binding;

import java.util.ArrayList;
import java.util.List;

public class SoundActivity extends AppCompatActivity {
    private int mCurrentTypePosition = -1;
    private ActivityMain50Binding mBinding;
    private List<Fragment> mFragmentList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMain50Binding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        initView();
    }

    @SuppressLint("NonConstantResourceId")
    private void initView() {
        mBinding.rgTab.setOnCheckedChangeListener((radioGroup, id) -> {
            switch (id) {
                case R.id.tab_eq:
                    switchFragment(0);
                    break;
                case R.id.tab_ball:
                    switchFragment(1);
                    break;
            }
        });
        mFragmentList.add(new EqFragment());
        mFragmentList.add(new BallFragment());
        switchFragment(0);
    }

    private void switchFragment(int position) {
        FragmentManager fm = getSupportFragmentManager();
        String targetTag = mFragmentList.get(position)
                .getClass().getSimpleName();
        Fragment targetFragment = fm.findFragmentByTag(targetTag);
        if (targetFragment == null) {
            targetFragment = mFragmentList.get(position);
        }

        if (position == mCurrentTypePosition) return;

        FragmentTransaction transaction = fm.beginTransaction();
        if (mCurrentTypePosition != -1) {
            transaction.hide(mFragmentList.get(mCurrentTypePosition));
        }

        if (!targetFragment.isAdded()) {
            transaction.add(R.id.layout_content, targetFragment, targetTag);
        } else {
            transaction.show(targetFragment);
        }

        mCurrentTypePosition = position;
        transaction.commitAllowingStateLoss();
    }

}
