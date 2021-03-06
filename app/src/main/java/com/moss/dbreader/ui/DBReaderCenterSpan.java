package com.moss.dbreader.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.ViewUtils;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;
import android.util.TypedValue;
import android.view.View;

import java.lang.reflect.TypeVariable;

/**
 * Created by tangqif on 2017/9/28.
 */

public class DBReaderCenterSpan  extends ReplacementSpan {
    private int fontSize;
    private int margin;
    public DBReaderCenterSpan(int fontSize,int margin){
        this.fontSize = fontSize;
        this.margin = margin;
    }
    @Override
    public int getSize(@NonNull Paint paint, CharSequence charSequence, @IntRange(from = 0) int i, @IntRange(from = 0) int i1, @Nullable Paint.FontMetricsInt fontMetricsInt) {
        charSequence = charSequence.subSequence(i, i1);
        Paint p = GetCustomerTextPaint(paint);
        return (int) p.measureText(charSequence.toString());
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence charSequence, @IntRange(from = 0) int i, @IntRange(from = 0) int i1, float v, int i2, int i3, int i4, @NonNull Paint paint) {
        charSequence = charSequence.subSequence(i, i1);
        Paint p = GetCustomerTextPaint(paint);
        int w = canvas.getWidth();
        int len = (int)p.measureText(charSequence.toString());
        int x = (w-2*margin- len)/2;
        x += margin;
        if(len >= w-2*margin){
            x = 0;
        }
        canvas.drawText(charSequence.toString(),x,i3,p);
    }

    private Paint GetCustomerTextPaint(Paint src){
        Paint paint = new Paint(src);
        paint.setTextSize(fontSize);
        paint.setFakeBoldText(true);
        return paint;
    }
}
