package com.moss.dbreader.ui;

import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.BatteryManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.moss.dbreader.Common;
import com.moss.dbreader.R;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.functions.Consumer;

import static android.content.Context.BATTERY_SERVICE;
import static android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY;
import static android.os.BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER;

/**
 * Created by tangqif on 2017/9/25.
 */

public class ReaderPageAdapter extends PagerAdapter implements OnPageChangeListener {


    static public class ReaderPage implements Serializable {
        private static final long serialVersionUID = -3964162394765715002L;
        public int chapterIndex;
        public int begin;
        public int end;
        public String name;
    }

    static public class FetchTextEvent {
        public FetchTextEvent(int index) {
            this.chapterIndex = index;
        }

        public int chapterIndex;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    private ArrayList<ReaderPage> pages = new ArrayList<ReaderPage>();
    private HashMap<Integer, String> pageTexts = new HashMap<Integer, String>();
    private ArrayList<View> views = new ArrayList<View>();
    ;
    private ArrayList<View> usingViews = new ArrayList<View>();
    private Fragment fragment;
    private int chapterCount = 0;
    private int power = 50;

    GestureDetector detector = null;

    private static final int TITLE_FONT_SIZE_SP = 18;
    public static final int FLAG_CURR_PAGE = -1;
    public static final int FLAG_PREVIOUS_PAGE = -2;

    public ReaderPageAdapter(Fragment fragment, int chapterCount) {
        this.fragment = fragment;
        this.chapterCount = chapterCount;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < usingViews.size(); i++) {
            View v = usingViews.get(i);
            int pos = (Integer) v.getTag(R.id.tag_pos);
            int chapIndex = (Integer) v.getTag(R.id.tag_chap_index);
            ReaderPage rp = getFirstPageOfChapter(chapIndex);
            if (pos == position) {
                if (rp.begin == FLAG_PREVIOUS_PAGE) {
                    rp.begin = FLAG_CURR_PAGE;
                    v.setTag(R.id.tag_need_update, 1);
                    ReaderPageAdapter.this.notifyDataSetChanged();
                }
            } else if (pos != position && rp.begin == FLAG_CURR_PAGE) {
                rp.begin = FLAG_PREVIOUS_PAGE;
            }
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    public void setCurrentItem(int pos) {
        for (int i = 0; i < pages.size(); i++) {
            ReaderPage item = pages.get(i);
            if (item.begin == FLAG_CURR_PAGE && pos != i) {
                item.begin = FLAG_PREVIOUS_PAGE;
            }
            if (pos == i && item.begin == FLAG_PREVIOUS_PAGE) {
                item.begin = FLAG_CURR_PAGE;
            }
        }
    }

    public void changeBattery(int per) {
        this.power = per;
        for (int i = 0; i < usingViews.size(); i++) {
            View v = usingViews.get(i);
            ImageView battery = (ImageView) v.findViewById(R.id.reader_battery);
            int id = getBatteryId(per);
            battery.setImageResource(id);
        }
    }

    public void changeChapters(int deltaChapterCount) {
        this.chapterCount += deltaChapterCount;
        for (int i = 0; i < usingViews.size(); i++) {
            View view = usingViews.get(i);
            TextView tv = (TextView) view.findViewById(R.id.reader_chapter_page_no);
            int chapterIndex = (Integer) view.getTag(R.id.tag_chap_index);
            String pageNo = (chapterIndex + 1) + "/" + chapterCount;
            tv.setText(pageNo);
        }
    }

    public void addPage(ReaderPage page) {
        pages.add(page);
    }

    public void addText(int index, String text) {

        ReaderPage rp = getFirstPageOfChapter(index);
        String cont = buildNovelText(rp.name, text);
        pageTexts.put(index, cont);
        for (int i = 0; i < usingViews.size(); i++) {
            View v = usingViews.get(i);
            int chapIndex = (Integer) v.getTag(R.id.tag_chap_index);
            ReaderPage page = getFirstPageOfChapter(chapIndex);
            if (chapIndex == index && page.begin == FLAG_CURR_PAGE) {
                initPageText(page, (TextView) v.findViewById(R.id.reader_text));
                break;
            }
            int pos = (Integer) v.getTag(R.id.tag_pos);
            page = this.getReaderPage(pos);
            if (chapIndex == index && page.begin >= 0) {
                View mask = v.findViewById(R.id.reader_mask);
                mask.setVisibility(View.GONE);
                View err = v.findViewById(R.id.reader_error);
                err.setVisibility(View.GONE);
                setPageText(page, (TextView) v.findViewById(R.id.reader_text));
            }
        }
    }

    public void error(int index) {
        for (int i = 0; i < usingViews.size(); i++) {
            View v = usingViews.get(i);
            int chapIndex = (Integer) v.getTag(R.id.tag_chap_index);
            if (chapIndex == index) {
                View err = v.findViewById(R.id.reader_error);
                err.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    public ArrayList<ReaderPage> getPages() {
        return pages;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ReaderPage page = getReaderPage(position);
        if (page == null) {
            return super.instantiateItem(container, position);
        }
        View view = null;
        if (views.size() > 0) {
            view = views.remove(0);
        } else {
            view = createView();
        }

        usingViews.add(view);

        view.setTag(R.id.tag_pos, position);
        view.setTag(R.id.tag_chap_index, page.chapterIndex);
        view.setTag(R.id.tag_page_begin, page.begin);
        view.setTag(R.id.tag_need_update, 0);

        ImageView battery = (ImageView) view.findViewById(R.id.reader_battery);
        int id = getBatteryId(power);
        battery.setImageResource(id);

        TextView tv = (TextView) view.findViewById(R.id.reader_chapter_title);
        tv.setText(page.name);
        tv = (TextView) view.findViewById(R.id.reader_chapter_page_no);
        String pageNo = (page.chapterIndex + 1) + "/" + chapterCount;
        tv.setText(pageNo);

        tv = (TextView) view.findViewById(R.id.reader_text);
        View mask = view.findViewById(R.id.reader_mask);
        View errView = view.findViewById(R.id.reader_error);
        if (pageTexts.containsKey(page.chapterIndex) && page.begin != FLAG_PREVIOUS_PAGE) {
            refreshPage(page, tv);
            mask.setVisibility(View.GONE);
            errView.setVisibility(View.GONE);
        } else {
            tv.setText("");
            mask.setVisibility(View.VISIBLE);
            if (page.begin == FLAG_CURR_PAGE || page.begin >= 0) {
                EventBus.getDefault().post(new FetchTextEvent(page.chapterIndex));
            }
        }
        ((ViewGroup) container).addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        View v = (View) object;
        int needUpdate = (Integer) v.getTag(R.id.tag_need_update);
        if (needUpdate == 1) {
            v.setTag(R.id.tag_need_update, 0);
            return POSITION_NONE;
        } else {
            return super.getItemPosition(object);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        usingViews.remove(object);
        ((ViewPager) container).removeView((View) object);
        views.add((View) object);
    }

    public int getFirstPageOfChapterIndex(int chapIndex) {
        for (int i = 0; i < pages.size(); i++) {
            ReaderPage page = pages.get(i);
            if (page.chapterIndex == chapIndex && page.begin <= 0) {
                return i;
            }
        }
        return -1;
    }

    public ReaderPage getReaderPage(int pos) {
        return pages.get(pos);
    }

    private View createView() {
        final View v = this.fragment.getActivity().getLayoutInflater().inflate(R.layout.view_reader, null);
        initializeProgressBar(v);

        if (detector == null) {
            detector = new GestureDetector(fragment.getContext(), new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    showReaderPanel(v, (int) e.getRawX(), (int) e.getRawY());
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    return false;
                }
            });
        }
        detector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                showReaderPanel(v, (int) e.getRawX(), (int) e.getRawY());
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });
        View txView = v.findViewById(R.id.reader_text);
        txView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View vw, MotionEvent event) {
                detector.onTouchEvent(event);
                return true;
            }
        });

        final View err = v.findViewById(R.id.reader_error);
        RxView.clicks(err).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {
                int chapIndex = (int) v.getTag(R.id.tag_chap_index);
                EventBus.getDefault().post(new FetchTextEvent(chapIndex));
                err.setVisibility(View.GONE);
            }
        });

        TextClock tmView = (TextClock) v.findViewById(R.id.reader_time);
        tmView.setFormat24Hour("hh:mm");
        tmView.setFormat12Hour("hh:mm");

        return v;
    }

    private void showReaderPanel(View v, int x, int y) {
        ReaderPanel rp = (ReaderPanel) ReaderPageAdapter.this.fragment.getView().findViewById(R.id.reader_panel);
        int chapIndex = (int) v.getTag(R.id.tag_chap_index);
        rp.setOrg(x, y);
        rp.setCurrIndex(chapIndex);
        rp.setVisibility(View.VISIBLE);
    }

    private int getBatteryId(int per) {
        if (per > 90)
            return R.drawable.battery_100_90;
        else if (per > 80)
            return R.drawable.battery_90_80;
        else if (per > 70)
            return R.drawable.battery_80_70;
        else if (per > 60)
            return R.drawable.battery_70_60;
        else if (per > 50)
            return R.drawable.battery_60_50;
        else if (per > 40)
            return R.drawable.battery_50_40;
        else if (per > 30)
            return R.drawable.battery_40_30;
        else if (per > 20)
            return R.drawable.battery_30_20;
        else if (per > 10)
            return R.drawable.battery_20_10;
        else
            return R.drawable.battery_10_0;
    }

    private String buildNovelText(String chapterName, String cont) {
        BufferedReader reader = new BufferedReader(new StringReader(cont));
        String line;
        String title = chapterName.replace(" ", "");
        int index = title.indexOf('(');
        if (index != -1) {
            title = title.substring(0, index);
        }
        index = title.indexOf('（');
        if (index != -1) {
            title = title.substring(0, index);
        }
        String text = "";
        boolean hasTitle = false;
        int i = 0;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                line = line.replace(" ", "");
                line = line.replace("\r", "");
                line = line.replace(" ", "");
                if (line.length() == 0) {
                    continue;
                }
                i++;
                if (i == 1) {
                    if (line.indexOf(title) != -1) {
                        text = chapterName + "\n\n";
                        hasTitle = true;
                    } else {
                        text = "  " + line + "\n";
                    }
                } else {
                    if (line.indexOf(title) != -1) {
                        continue;
                    }
                    line = "  " + line;
                    text = text + line + "\n";
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (hasTitle == false) {
            text = chapterName + "\n\n" + text;
        }
        if (text.length() > 1) {
            text = text.substring(0, text.length() - 1);
        }
        text = Common.half2full(text);
        return text;
    }

    private ReaderPage getFirstPageOfChapter(int chapIndex) {
        for (int i = 0; i < pages.size(); i++) {
            ReaderPage page = pages.get(i);
            if (page.chapterIndex == chapIndex && page.begin <= 0) {
                return page;
            }
        }
        return null;
    }

    private void initializeProgressBar(View v) {
        SimpleDraweeView pv = (SimpleDraweeView) v.findViewById(R.id.reader_progress);
        Uri uri = Uri.parse("res://" + this.fragment.getContext().getPackageName() + "/" + R.drawable.progress_big);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        pv.setController(controller);
    }

    private void refreshPage(final ReaderPage page, final TextView tv) {
        if (page.begin == -1) {
            initPageText(page, tv);
        } else if (page.begin >= 0) {
            setPageText(page, tv);
        }
    }

    private void insertReaderPage(final ReaderPage page) {
        int count = pages.size();
        for (int i = 0; i < count; i++) {
            ReaderPage item = pages.get(i);
            if (item.chapterIndex < page.chapterIndex) {
                continue;
            }
            if (page.chapterIndex == item.chapterIndex) {
                if ((item.begin == -1 ||  item.begin == -2) && page.begin == 0) {
                    item.begin = page.begin;
                    item.end = page.end;
                    break;
                } else if (page.begin > item.begin && i == count - 1) {
                    pages.add(page);
                    break;
                }
            } else if (item.chapterIndex > page.chapterIndex) {
                pages.add(i, page);
                break;
            }
        }
    }

    private void initPageText(final ReaderPage page, final TextView tv) {
        tv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                tv.getViewTreeObserver().removeOnPreDrawListener(this);
                int tvHigh = tv.getHeight();
                allocatePages(page, tv, tvHigh);
                updatePages(page.chapterIndex);
                ReaderPageAdapter.this.notifyDataSetChanged();
                ReaderPage rp = getFirstPageOfChapter(page.chapterIndex + 1);
                if (rp != null && rp.begin == FLAG_PREVIOUS_PAGE) {
                    rp.begin = FLAG_CURR_PAGE;
                    EventBus.getDefault().post(new FetchTextEvent(rp.chapterIndex));
                }
                return false;
            }
        });
        String text = pageTexts.get(page.chapterIndex);
        int end = text.indexOf('\n');
        SpannableString sp = new SpannableString(text);
        int fs = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TITLE_FONT_SIZE_SP, tv.getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, tv.getResources().getDisplayMetrics());
        sp.setSpan(new DBReaderCenterSpan(fs, margin), 0, end, 0);
        tv.setText(sp);
    }

    private void updatePages(int chapIndex) {
        for (int i = 0; i < usingViews.size(); i++) {
            View v = usingViews.get(i);
            int pos = (Integer) v.getTag(R.id.tag_pos);
            ReaderPage rp = getReaderPage(pos);
            if (rp.chapterIndex != chapIndex) {
                continue;
            }
            int chap = (Integer) v.getTag(R.id.tag_chap_index);
            int begin = (Integer) v.getTag(R.id.tag_page_begin);
            if (rp.begin != begin || rp.chapterIndex != chap) {
                v.setTag(R.id.tag_need_update, 1);
                v.setTag(R.id.tag_chap_index, rp.chapterIndex);
            }
        }
    }

    private void allocatePages(final ReaderPage page, final TextView tv, int tvHigh) {
        int count = tv.getLineCount();
        int h = 0;
        Rect rc = new Rect();
        int begin = 0;
        for (int i = 0; i < count; i++) {
            tv.getLineBounds(i, rc);
            h += rc.height();
            if (h >= tvHigh || i == count - 1) {
                if (begin == 0) {
                    i = i - 1;
                } else if (h > tvHigh) {
                    int margin = (int) tv.getLineSpacingExtra();
                    if ((h - margin) > tvHigh) {
                        i--;
                    }
                }
                int end = tv.getLayout().getLineEnd(i);
                ReaderPage rp = new ReaderPage();
                rp.chapterIndex = page.chapterIndex;
                rp.begin = begin;
                rp.end = end;
                rp.name = page.name;
                insertReaderPage(rp);
                if (i != count - 1) {
                    begin = tv.getLayout().getLineStart(i + 1);
                    h = 0;
                }
            }
        }
    }

    private void setPageText(final ReaderPage page, TextView tv) {
        String text = this.pageTexts.get(page.chapterIndex);
        int len = text.length();
        if (page.begin >= len) {
            text = "";
        } else if (page.end > len) {
            text = text.substring(page.begin, len);
        } else {
            text = text.substring(page.begin, page.end);
        }

        if (text.charAt(text.length() - 1) == '\n') {
            text = text.substring(0, text.length() - 1);
        }

        if (page.begin == 0) {
            int end = text.indexOf('\n');
            SpannableString sp = new SpannableString(text);
            int fs = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TITLE_FONT_SIZE_SP, tv.getResources().getDisplayMetrics());
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, tv.getResources().getDisplayMetrics());
            sp.setSpan(new DBReaderCenterSpan(fs, margin), 0, end, 0);
            tv.setGravity(Gravity.BOTTOM);
            tv.setText(sp);
        } else {
            tv.setGravity(Gravity.CENTER_VERTICAL);
            if (len == page.end) {
                tv.setGravity(Gravity.TOP);
            }
            tv.setText(text);
        }
    }
}
