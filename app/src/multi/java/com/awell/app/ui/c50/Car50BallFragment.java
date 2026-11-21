package com.awell.app.ui.c50;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.awell.app.R;
import com.awell.app.ui.SoundFragment;

public class Car50BallFragment extends SoundFragment{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mContentView == null) {
            mContentView = inflater.inflate(R.layout.c50_fragment_ball, container, false);
        }
        return mContentView;
    }

    @Override
    protected void setBallBg(ImageView view, boolean flag) {
        view.setImageAlpha(flag ? 128 : 255);
    }

}
