package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.awell.app.R;
import com.awell.app.adapter.MainFragmentAdapter;
import com.awell.app.databinding.ActivityLamborghiniBinding;
import com.awell.app.databinding.ActivityMain50Binding;
import com.awell.app.databinding.ActivityMainBinding;
import com.awell.app.ui.c50.Car50BallFragment;
import com.awell.app.ui.c50.Car50EqFragment;
import com.awell.app.ui.equal.SoundEqualFragment;
import com.awell.app.ui.lambo.BallActivity;
import com.awell.app.ui.lambo.EqActivity;
import com.awell.app.utils.SkinConfig;
import com.awell.app.utils.ToolClass;
import com.awell.app.windows.EqualizerTypeWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * 运行期 UI 入口：
 * persist.sys.current.lamborg.ui = 1 -> car_50
 * = 2 -> lamborghini
 * 其他值 -> gongban（保持原有公版 UI）
 */
public class SoundActivity extends AppCompatActivity implements View.OnClickListener, EqualizerTypeWindow.OnEqualizerTypeClickListener {

    private int mSkin;

    // gongban
    private ActivityMainBinding mGongbanBinding;
    private MainFragmentAdapter mGongbanAdapter;
    private SoundEqualFragment mGongbanEqualFragment;
    private final List<Fragment> mGongbanFragments = new ArrayList<>();
    private String[] mGongbanApsType;
    private int mGongbanTypePosition = 0;

    // car_50
    private ActivityMain50Binding mCar50Binding;
    private final List<Fragment> mCar50Fragments = new ArrayList<>();
    private int mCar50Position = -1;

    // lamborghini
    private ActivityLamborghiniBinding mLamboBinding;
    private String[] mLamboApsType;
    private int mLamboTypePosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        int uiType = SkinConfig.uiType();
        mSkin = (uiType == SkinConfig.CAR_50 || uiType == SkinConfig.LAMBORGHINI)
                ? uiType : SkinConfig.GONGBAN;
        if (mSkin == SkinConfig.CAR_50) {
            setTheme(R.style.Splash_Theme);
        } else {
            setTheme(R.style.SplashTheme);
        }
        super.onCreate(savedInstanceState);

        if (mSkin == SkinConfig.CAR_50) {
            initCar50Ui();
        } else if (mSkin == SkinConfig.LAMBORGHINI) {
            initLamborghiniUi();
        } else {
            initGongbanUi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSkin == SkinConfig.LAMBORGHINI && mLamboBinding != null) {
            initLamborghiniData();
        }
    }

    // ---------------------------------------------------------------------
    // gongban
    // ---------------------------------------------------------------------

    private void initGongbanUi() {
        mGongbanBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mGongbanBinding.getRoot());
        mGongbanAdapter = new MainFragmentAdapter(getSupportFragmentManager());
        mGongbanEqualFragment = new SoundEqualFragment();
        mGongbanFragments.clear();
        mGongbanFragments.add(mGongbanEqualFragment);
        mGongbanFragments.add(new SoundFragment());
        mGongbanAdapter.setFragments(mGongbanFragments);
        mGongbanBinding.viewPager.setCanScroll(false);
        mGongbanBinding.viewPager.setAdapter(mGongbanAdapter);
        mGongbanBinding.btnSound.setOnClickListener(this);
        mGongbanBinding.btnEqualizer.setOnClickListener(this);
        mGongbanBinding.btnEqualizer.setSelected(true);
        mGongbanBinding.layoutCustom.setOnClickListener(this);
        mGongbanApsType = getResources().getStringArray(R.array.dsp_aps_type);
        mGongbanTypePosition = ToolClass.getTypeFlag(this) == -1 ? 0 : ToolClass.getTypeFlag(this);
        mGongbanBinding.tvCustom.setText(mGongbanApsType[mGongbanTypePosition]);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        if (mSkin != SkinConfig.GONGBAN) {
            return;
        }
        switch (view.getId()) {
            case R.id.btn_equalizer:
            case R.id.btn_sound:
                switchGongbanPage(view.getId());
                break;
            case R.id.layout_custom:
                showGongbanTypeWindow();
                break;
            default:
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void switchGongbanPage(int id) {
        mGongbanBinding.btnEqualizer.setSelected(false);
        mGongbanBinding.btnSound.setSelected(false);
        switch (id) {
            case R.id.btn_equalizer:
                mGongbanBinding.btnEqualizer.setSelected(true);
                mGongbanBinding.viewPager.setCurrentItem(0);
                mGongbanBinding.layoutCustom.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_sound:
                mGongbanBinding.btnSound.setSelected(true);
                mGongbanBinding.viewPager.setCurrentItem(1);
                mGongbanBinding.layoutCustom.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void showGongbanTypeWindow() {
        mGongbanBinding.ivCustom.setRotation(0);
        EqualizerTypeWindow typeWindow = new EqualizerTypeWindow(this);
        typeWindow.setSelectedPosition(mGongbanTypePosition);
        typeWindow.setListener(this);
        typeWindow.showAsDropDown(mGongbanBinding.layoutCustom, 0, 0, Gravity.START);
    }

    @Override
    public void onEqualizerTypeClick(int position, String type) {
        if (mSkin != SkinConfig.GONGBAN || mGongbanBinding == null) {
            return;
        }
        mGongbanBinding.ivCustom.setRotation(180);
        mGongbanBinding.tvCustom.setText(type);
        if (mGongbanEqualFragment != null && mGongbanTypePosition != position) {
            mGongbanEqualFragment.setType(position);
        }
        mGongbanTypePosition = position;
    }

    // ---------------------------------------------------------------------
    // car_50
    // ---------------------------------------------------------------------

    private void initCar50Ui() {
        mCar50Binding = ActivityMain50Binding.inflate(getLayoutInflater());
        setContentView(mCar50Binding.getRoot());
        // car_50 卡片按状态栏高度下移，保证状态栏显示/隐藏时上下留白一致。
        applyStatusBarPadding(mCar50Binding.getRoot());

        // Activity 重建（如切换分辨率）时，FragmentManager 会按 tag 恢复旧实例，
        // 这里必须复用恢复出来的实例，否则新实例不在 FragmentManager 里，页面会重叠。
        FragmentManager fm = getSupportFragmentManager();
        mCar50Fragments.clear();
        mCar50Fragments.add(obtainFragment(fm, Car50EqFragment.class.getSimpleName(), new Car50EqFragment()));
        mCar50Fragments.add(obtainFragment(fm, Car50BallFragment.class.getSimpleName(), new Car50BallFragment()));

        mCar50Binding.rgTab.setOnCheckedChangeListener((radioGroup, id) -> {
            if (id == R.id.tab_eq) {
                switchCar50Fragment(0);
            } else if (id == R.id.tab_ball) {
                switchCar50Fragment(1);
            }
        });
        switchCar50Fragment(0);
    }

    private Fragment obtainFragment(FragmentManager fm, String tag, Fragment created) {
        Fragment restored = fm.findFragmentByTag(tag);
        return restored != null ? restored : created;
    }

    private void switchCar50Fragment(int position) {
        if (position < 0 || position >= mCar50Fragments.size()) {
            return;
        }
        if (position == mCar50Position) {
            return;
        }

        Fragment targetFragment = mCar50Fragments.get(position);
        String targetTag = targetFragment.getClass().getSimpleName();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        for (Fragment fragment : mCar50Fragments) {
            if (fragment != targetFragment && fragment.isAdded()) {
                transaction.hide(fragment);
            }
        }

        if (!targetFragment.isAdded()) {
            transaction.add(R.id.layout_content, targetFragment, targetTag);
        } else {
            transaction.show(targetFragment);
        }

        mCar50Position = position;
        transaction.commitAllowingStateLoss();
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

    // ---------------------------------------------------------------------
    // lamborghini
    // ---------------------------------------------------------------------

    private void initLamborghiniUi() {
        mLamboBinding = ActivityLamborghiniBinding.inflate(getLayoutInflater());
        setContentView(mLamboBinding.getRoot());

        mLamboBinding.tvMusic.setOnClickListener(v -> openMusic());
        mLamboBinding.ivPrev.setOnClickListener(v -> updateLamborghiniType(false));
        mLamboBinding.tvBal.setOnClickListener(v -> startActivity(BallActivity.class));
        mLamboBinding.tvEq.setOnClickListener(v -> startActivity(EqActivity.class));
        mLamboBinding.ivNext.setOnClickListener(v -> updateLamborghiniType(true));
        mLamboBinding.ivRest.setOnClickListener(v -> {
            mLamboTypePosition = 0;
            mLamboBinding.tvType.setText(mLamboApsType[mLamboTypePosition]);
        });

        initLamborghiniData();
    }

    private void initLamborghiniData() {
        mLamboApsType = getResources().getStringArray(R.array.dsp_aps_type);
        mLamboTypePosition = ToolClass.getTypeFlag(this) == -1 ? 0 : ToolClass.getTypeFlag(this);
        if (mLamboTypePosition < 0 || mLamboTypePosition >= mLamboApsType.length) {
            mLamboTypePosition = 0;
        }
        mLamboBinding.tvType.setText(mLamboApsType[mLamboTypePosition]);
    }

    private void openMusic() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.awell.localmusic");
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void startActivity(Class<? extends Activity> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
        // 传入 0 代表没有任何动画
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0);
        } else {
            overridePendingTransition(0, 0);
        }
    }

    private void updateLamborghiniType(boolean isNext) {
        if (mLamboApsType == null || mLamboApsType.length == 0) {
            return;
        }
        if (isNext) {
            if (mLamboTypePosition < mLamboApsType.length - 1) {
                mLamboTypePosition++;
            } else {
                mLamboTypePosition = 0;
            }
        } else {
            if (mLamboTypePosition > 0) {
                mLamboTypePosition--;
            } else {
                mLamboTypePosition = mLamboApsType.length - 1;
            }
        }
        ToolClass.setTypeFlag(this, mLamboTypePosition);
        mLamboBinding.tvType.setText(mLamboApsType[mLamboTypePosition]);
    }
}
