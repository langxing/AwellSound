package com.awell.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.awell.app.R;

public class BallFragment extends SoundFragment{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mContentView == null) {
            mContentView = inflater.inflate(R.layout.fragment_ball, container, false);
        }
        return mContentView;
    }

    @Override
    protected void setBallBg(ImageView view, boolean flag) {
        view.setImageAlpha(flag ? 128 : 255);
    }

}
