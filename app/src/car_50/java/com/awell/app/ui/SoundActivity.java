package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
        applyStatusBarPadding(mBinding.getRoot());
        initView();
    }

    /**
     * car_50 卡片按状态栏高度下移，保证状态栏显示/隐藏时上下留白一致。
     */
    private void applyStatusBarPadding(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            view.setPadding(0, top, 0, 0);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    @SuppressLint("NonConstantResourceId")
    private void initView() {
        // Activity 重建（如切换分辨率）时，FragmentManager 会按 tag 恢复旧实例。
        // 这里必须复用恢复出来的实例，否则 new 出来的新实例不在 FragmentManager 里，
        // hide() 作用不到真正显示的旧页面，两个页面会重叠。
        FragmentManager fm = getSupportFragmentManager();
        mFragmentList.clear();
        mFragmentList.add(obtainFragment(fm, EqFragment.class.getSimpleName(), new EqFragment()));
        mFragmentList.add(obtainFragment(fm, BallFragment.class.getSimpleName(), new BallFragment()));

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
        switchFragment(0);
    }

    private Fragment obtainFragment(FragmentManager fm, String tag, Fragment created) {
        Fragment restored = fm.findFragmentByTag(tag);
        return restored != null ? restored : created;
    }

    private void switchFragment(int position) {
        if (position < 0 || position >= mFragmentList.size()) return;
        if (position == mCurrentTypePosition) return;

        Fragment targetFragment = mFragmentList.get(position);
        String targetTag = targetFragment.getClass().getSimpleName();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // 隐藏其他所有已加入的页面，而不是只隐藏“上一个”，避免重建或异常时序下残留显示
        for (Fragment fragment : mFragmentList) {
            if (fragment != targetFragment && fragment.isAdded()) {
                transaction.hide(fragment);
            }
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
