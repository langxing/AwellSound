package com.awell.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class CrosshairView extends View {
    private float centerX = 0f;
    private float centerY = 0f;
    private Paint linePaint;

    public CrosshairView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#80FFEB3B")); // 黄色半透明线
        linePaint.setStrokeWidth(1f); // 线宽
        linePaint.setStyle(Paint.Style.STROKE);
    }

    public void updateCrosshair(float x, float y) {
        this.centerX = x;
        this.centerY = y;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        // 初始状态如果还没触摸，默认画在 View 正中心
        if (centerX == 0f && centerY == 0f) {
            centerX = getWidth() / 2f;
            centerY = getHeight() / 2f;
        }
        // 画水平线：从最左边到最右边
        canvas.drawLine(0, centerY, getWidth(), centerY, linePaint);
        // 画垂直线：从最顶端到最底端
        canvas.drawLine(centerX, 0, centerX, getHeight(), linePaint);
    }

}
