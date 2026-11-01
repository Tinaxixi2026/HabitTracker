package com.example.habittracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/** 简易柱状图：每月一根柱，柱顶显示次数、底部显示月份。 */
public class BarChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<HabitStore.MonthCount> data = new ArrayList<>();

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setColor(Color.parseColor("#8CC63F"));
        labelPaint.setColor(Color.parseColor("#8A8A8E"));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(Color.parseColor("#1B1B1B"));
        valuePaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(dp(11));
        valuePaint.setTextSize(dp(10));
    }

    public void setData(List<HabitStore.MonthCount> data) {
        this.data = data == null ? new ArrayList<>() : data;
        invalidate();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = data.size();
        if (n == 0) {
            return;
        }

        float w = getWidth();
        float h = getHeight();
        float labelH = dp(18);
        float topPad = dp(16);
        float chartBottom = h - labelH;
        float chartHeight = chartBottom - topPad;
        if (chartHeight <= 0) {
            return;
        }

        int max = 0;
        for (HabitStore.MonthCount mc : data) {
            max = Math.max(max, mc.count);
        }
        if (max == 0) {
            max = 1;
        }

        float slot = w / n;
        float barW = slot * 0.5f;

        for (int i = 0; i < n; i++) {
            HabitStore.MonthCount mc = data.get(i);
            float cx = slot * i + slot / 2f;
            float barH = chartHeight * mc.count / max;
            float left = cx - barW / 2f;
            float top = chartBottom - barH;

            if (barH > 0) {
                RectF rect = new RectF(left, top, left + barW, chartBottom);
                canvas.drawRoundRect(rect, barW / 2f, barW / 2f, barPaint);
                canvas.drawText(String.valueOf(mc.count), cx, top - dp(4), valuePaint);
            }
            canvas.drawText(mc.label, cx, chartBottom + labelH - dp(4), labelPaint);
        }
    }
}
