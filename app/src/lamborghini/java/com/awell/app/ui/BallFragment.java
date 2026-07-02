package com.awell.app.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.awell.app.R;
import com.awell.app.utils.LogUtil;
import com.awell.app.view.CrosshairView;

public class BallFragment extends SoundFragment{
    private CrosshairView mLineView;
    private Button btnFL, btnFR, btnRL, btnRR;
    private float parentX, parentY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mContentView == null) {
            mContentView = inflater.inflate(R.layout.fragment_ball, container, false);
        }
        return mContentView;
    }

    @Override
    protected void initView() {
        super.initView();
        mLineView = mContentView.findViewById(R.id.lineView);
        btnFL = mContentView.findViewById(R.id.btn_fl);
        btnFR = mContentView.findViewById(R.id.btn_fr);
        btnRL = mContentView.findViewById(R.id.btn_rl);
        btnRR = mContentView.findViewById(R.id.btn_rr);
        aps_car_ball.addOnLayoutChangeListener((v, left, top, right, bottom,
                                                oldLeft, oldTop, oldRight, oldBottom) -> {
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                // 父容器坐标系的中心
                float centerX = v.getX() + v.getWidth() * 0.5f;
                float centerY = v.getY() + v.getHeight() * 0.5f;
                if (parentX == 0 || parentY == 0) {
                    parentX = aps_sound_range.getWidth();
                    parentY = aps_sound_range.getHeight();
                    LogUtil.d("parentX = " + parentX + " parentY = " + parentY);
                }
                mLineView.updateCrosshair(centerX, centerY);
                updateUI((int) centerX, (int) centerY);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(int centerX, int centerY) {
        LogUtil.d("centerX = " + centerX + " centerY = " + centerY + " X = " + parentX/2 + " Y = " + parentY/2);
        if (parentY == 0 || parentX == 0) return;
        int perc;
        // 计算FL
        if (centerX <= (int) parentX /2 && centerY <= (int) parentY /2) {
            btnFL.setText("FL 100%");
        } else {
            float x = Math.abs(parentX - centerX - ((float) aps_car_ball.getWidth() /2));
            float y = Math.abs(parentY - centerY - ((float) aps_car_ball.getHeight() /2));
            if (x <= y) {
                perc = (int) (x * 100 / (parentX/2));
            } else {
                perc = (int) (y * 100 / (parentY/2));
            }
            btnFL.setText("FL " + perc + "%");
        }
        // 计算FR
        if (centerX >= (int)parentX/2 && centerY <= (int)parentY/2) {
            btnFR.setText("FR 100%");
        } else {
            float x = Math.abs(centerX - ((float) aps_car_ball.getWidth() /2));
            float y = Math.abs(parentY - centerY - ((float) aps_car_ball.getHeight() /2));
            if ((centerX >= (int)parentX/2 && y <= 1)) {
                // 误差小于等于1
               perc = 100;
            } else if (x <= y) {
                perc = (int) (x * 100 / (parentX/2));
            } else {
                perc = (int) (y * 100 / (parentY/2));
            }
            btnFR.setText("FR " + perc + "%");
        }
        // 计算RL
        if (centerX <= (int) parentX /2 && centerY >= (int) parentY /2) {
            btnRL.setText("RL 100%");
        } else {
            float x = Math.abs(parentX - centerX - ((float) aps_car_ball.getWidth() /2));
            float y = Math.abs(centerY - ((float) aps_car_ball.getHeight() /2));
            if (x <= y) {
                perc = (int) (x * 100 / (parentX/2));
            } else {
                perc = (int) (y * 100 / (parentY/2));
            }
            btnRL.setText("RL " + perc + "%");
        }
        // 计算RR
        if (centerX >= (int) parentX /2 && centerY >= (int) parentY /2) {
            btnRR.setText("RL 100%");
        } else {
            float x = Math.abs(centerX - ((float) aps_car_ball.getWidth() /2));
            float y = Math.abs(centerY - ((float) aps_car_ball.getHeight() /2));
            if (x <= y) {
                perc = (int) (x * 100 / (parentX/2));
            } else {
                perc = (int) (y * 100 / (parentY/2));
            }
            btnRR.setText("RL " + perc + "%");
        }
    }

}
