package com.moss.dbreader.ui;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by tangqif on 11/5/2017.
 */

public class ReaderPanel extends View {

    private int outRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
    private int midRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
    private int inRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 27, getResources().getDisplayMetrics());
    private int clickRange = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
    private String frameColor = "#9e9e9e";

    private int currIndex = 0;
    private DBReaderNovel novel = null;

    public static final int CLICK_CASE = 1;
    public static final int CLICK_SEARCH = 2;

    protected int orgX;
    protected int orgY;


    public static class ReadPanel_Dict_Event{
        public ReadPanel_Dict_Event(DBReaderNovel novel,int index)
        {
            this.novel = novel;
            this.index = index;
        }
        public int index;
        public DBReaderNovel novel;
    }

    public static class ReadPanel_Cache_Event{

    }

    public static class  ReadPanel_ToMain_Event{
        public ReadPanel_ToMain_Event(int id){
            this.id = id;
        }
        public int id;
    }

    public ReaderPanel(Context context) {
        super(context);
    }

    public ReaderPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public void setOrg(int x, int y){
        orgX = x;
        orgY = y;
    }

    public ReaderPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCurrIndex(int index){
        this.currIndex = index;
    }

    public void setNovel(DBReaderNovel novel){
        this.novel = novel;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        drawFrame(canvas);
        drawTexts(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eve = event.getActionMasked();
        if(eve == MotionEvent.ACTION_DOWN ){
            int x = (int)event.getX();
            int y = (int)event.getY();
            if(checkDict(x,y)==true){
                EventBus.getDefault().post(new ReadPanel_Dict_Event(this.novel,this.currIndex));
            }
            else if(checkCache(x,y) == true){
                EventBus.getDefault().post(new ReadPanel_Cache_Event());
            }
            else if(checkCase(x,y) == true){
                EventBus.getDefault().post(new ReadPanel_ToMain_Event(CLICK_CASE));
            }
            else if(checkSearch(x,y) == true){
                EventBus.getDefault().post(new ReadPanel_ToMain_Event(CLICK_SEARCH));
            }
            setVisibility(View.GONE);
        }
        return true;
    }

    private void drawFrame(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#DBC49B"));
        canvas.drawCircle(orgX, orgY, outRadius+10, paint);
        canvas.save();
        canvas.translate(orgX, orgY);
        canvas.rotate(45);


        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setAlpha(255);
        paint.setColor(Color.parseColor(frameColor));
        canvas.drawCircle(0, 0, outRadius + 10, paint);


        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor(frameColor));
        canvas.drawCircle(0, 0, midRadius, paint);

        canvas.drawLine(0, midRadius, 0, outRadius+10, paint);
        canvas.drawLine(0, -midRadius, 0, -outRadius-10, paint);
        canvas.drawLine(midRadius, 0, outRadius+10, 0, paint);
        canvas.drawLine(-midRadius, 0, -outRadius-10, 0, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#FFA726"));
        canvas.drawCircle(0, 0, inRadius, paint);

        canvas.restore();
    }

    private void drawTexts(Canvas canvas) {
        Paint paint = new Paint();
        int ts = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics());
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(),"fonts/xinkai.ttf");
        paint.setTypeface(typeface);
        paint.setStrokeWidth(3);
        paint.setTextSize(ts);
        paint.setColor(Color.parseColor("#000000"));
        Rect rc = new Rect();
        int delta = midRadius+(outRadius-midRadius)/2;

        canvas.save();
        canvas.translate(orgX, orgY);

        String txt = getResources().getString(R.string.panel_dict);
        paint.getTextBounds(txt, 0, txt.length(), rc);
        int begin = 0 - delta;
        begin -= rc.width() / 2;
        canvas.drawText(txt, begin, rc.height() / 2, paint);

        txt = getResources().getString(R.string.panel_cache);
        paint.getTextBounds(txt, 0, txt.length(), rc);
        begin = delta;
        begin -= rc.width() / 2;
        canvas.drawText(txt, begin, rc.height() / 2, paint);

        txt = getResources().getString(R.string.panel_search);
        paint.getTextBounds(txt, 0, txt.length(), rc);
        begin = delta;
        begin += rc.height() / 2;
        canvas.drawText(txt, -rc.width() / 2, begin, paint);

        txt = getResources().getString(R.string.panel_case);
        paint.getTextBounds(txt, 0, txt.length(), rc);
        begin = 0 - delta;
        begin += rc.height() / 2;
        canvas.drawText(txt, -rc.width() / 2, begin, paint);

        canvas.restore();
    }

    private boolean checkDict(int x, int y) {
        int delta = midRadius+(outRadius-midRadius)/2;
        int orgX = this.orgX - delta;
        int orgY = this.orgY;

        Rect rc = new Rect(orgX-clickRange,orgY-clickRange,orgX+clickRange,orgY+clickRange);
        return rc.contains(x,y);
    }

    private boolean checkCache(int x, int y) {
        int delta = midRadius+(outRadius-midRadius)/2;
        int orgX = this.orgX + delta;
        int orgY = this.orgY;

        Rect rc = new Rect(orgX-clickRange,orgY-clickRange,orgX+clickRange,orgY+clickRange);
        return rc.contains(x,y);
    }

    private boolean checkCase(int x, int y) {
        int delta = midRadius+(outRadius-midRadius)/2;
        int orgX = this.orgX;
        int orgY = this.orgY - delta;

        Rect rc = new Rect(orgX-clickRange,orgY-clickRange,orgX+clickRange,orgY+clickRange);
        return rc.contains(x,y);
    }

    private boolean checkSearch(int x, int y){
        int delta = midRadius+(outRadius-midRadius)/2;
        int orgX = this.orgX;
        int orgY = this.orgY + delta;

        Rect rc = new Rect(orgX-clickRange,orgY-clickRange,orgX+clickRange,orgY+clickRange);
        return rc.contains(x,y);
    }
}
