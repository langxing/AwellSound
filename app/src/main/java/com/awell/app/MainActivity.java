package com.awell.app;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.awell.app.databinding.ActivityMainBinding;
import com.awell.app.ui.SoundFragment;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_root, new SoundFragment())
                .commitAllowingStateLoss();

    }

}
