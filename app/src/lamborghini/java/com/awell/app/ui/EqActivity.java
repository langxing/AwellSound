package com.awell.app.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.awell.app.R;

public class EqActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ball);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new EqFragment())
                .commitAllowingStateLoss();
    }

}
