package com.moss.dbreader.ui;

import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.moss.dbreader.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tangqif on 2017/9/25.
 */

public class ReaderPageAdapter extends PagerAdapter implements OnPageChangeListener {


    static public class ReaderPage {
        public int chapterIndex;
        public int begin;
        public int end;
        public String name;
    }


    private ArrayList<ReaderPage> pages = new ArrayList<ReaderPage>();
    private HashMap<Integer, String> pageTexts = new HashMap<Integer, String>();
    private ArrayList<View> views;
    private int textViewId;
    private int maskViewId;
    private int pageNoId;
    private int titleId;
    private ArrayList<View> usingViews = new ArrayList<View>();
    IReaderPageAdapterNotify readerPageAdapterNotify = null;
    private static final int TITLE_FONT_SIZE_SP = 22;
    public static final int FLAG_CURR_PAGE = -1;
    public static final int FLAG_PREVIOUS_PAGE = -2;

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
            }
            else if(pos != position && rp.begin == FLAG_CURR_PAGE){
                rp.begin = FLAG_PREVIOUS_PAGE;
            }
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    public ReaderPageAdapter(ArrayList<View> views, int tvId, int titleId, int pageNoId, int maskId, IReaderPageAdapterNotify readerPageAdapterNotify) {
        this.views = views;
        this.textViewId = tvId;
        this.maskViewId = maskId;
        this.titleId = titleId;
        this.pageNoId = pageNoId;
        this.readerPageAdapterNotify = readerPageAdapterNotify;
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

    public void addPage(ReaderPage page) {
        pages.add(page);
    }

    public void addText(int index, String text) {

        Log.i("Andrei", "addText index is " + index + " " + usingViews.size());
        pageTexts.put(index, text);
        for (int i = 0; i < usingViews.size(); i++) {
            View v = usingViews.get(i);
            int chapIndex = (Integer) v.getTag(R.id.tag_chap_index);
            ReaderPage page = getFirstPageOfChapter(chapIndex);
            if (chapIndex == index && page.begin == FLAG_CURR_PAGE) {
                initPageText(page, (TextView) v.findViewById(textViewId));
                break;
            }
        }
    }

    HashMap<Integer, String> getPageTexts() {
        return pageTexts;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ReaderPage page = getReaderPage(position);
        if (page == null) {
            return super.instantiateItem(container, position);
        }
        Log.i("Andrei", "views is "+ views.size() + " " + usingViews.size());
        View view = views.get(0);
        views.remove(0);
        usingViews.add(view);
        view.setTag(R.id.tag_pos, position);
        view.setTag(R.id.tag_chap_index, page.chapterIndex);
        view.setTag(R.id.tag_page_begin, page.begin);
        view.setTag(R.id.tag_need_update, 0);
        TextView tv = null;

        tv = (TextView) view.findViewById(this.titleId);
        tv.setText(page.name);
        tv = (TextView) view.findViewById(this.pageNoId);
        String pageNo = (position + 1) + "/" + pages.size();
        tv.setText(pageNo);

        tv = (TextView) view.findViewById(textViewId);
        View mask = view.findViewById(maskViewId);
        if (pageTexts.containsKey(page.chapterIndex) && page.begin != FLAG_PREVIOUS_PAGE) {
            refreshPage(page, tv);
            mask.setVisibility(View.GONE);
        } else {
            tv.setText("");
            if (page.begin == FLAG_CURR_PAGE) {
                readerPageAdapterNotify.update(page.chapterIndex);
            }
            mask.setVisibility(View.VISIBLE);
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

    public int getFirstPageOfChapterIndex(int chapIndex){
        for (int i = 0; i < pages.size(); i++) {
            ReaderPage page = pages.get(i);
            if (page.chapterIndex == chapIndex && page.begin <= 0) {
                return i;
            }
        }
        return -1;
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

    public ReaderPage getReaderPage(int pos) {
        return pages.get(pos);
    }

    private void refreshPage(final ReaderPage page, final TextView tv) {
        if (page.begin == -1) {
            initPageText(page, tv);
        } else if (page.begin >= 0) {
            setPageText(page, tv);
        } else {
            //tv.setText("");
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
                if (item.begin == -1 && page.begin == 0) {
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
        Log.i("Andrei", "initPage index " + page.chapterIndex);
        tv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                tv.getViewTreeObserver().removeOnPreDrawListener(this);
                int tvHigh = tv.getHeight();
                allocatePages(page, tv, tvHigh);
                ReaderPage rp = getFirstPageOfChapter(page.chapterIndex + 1);
                if (rp != null && rp.begin == FLAG_PREVIOUS_PAGE) {
                    rp.begin = FLAG_CURR_PAGE;
                }
                updatePages();
                ReaderPageAdapter.this.notifyDataSetChanged();
                return false;
            }
        });
        String text = pageTexts.get(page.chapterIndex);
        if (text.indexOf(page.name) == -1) {
            text = page.name + "\n" + pageTexts.get(page.chapterIndex);
        }
        int end = text.indexOf('\n');
        SpannableString sp = new SpannableString(text);
        int fs = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TITLE_FONT_SIZE_SP, tv.getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, tv.getResources().getDisplayMetrics());
        sp.setSpan(new DBReaderCenterSpan(fs, margin), 0, end, 0);
        tv.setText(sp);
    }

    private void updatePages() {
        for (int i = 0; i < usingViews.size(); i++) {
            View v = usingViews.get(i);
            int pos = (Integer) v.getTag(R.id.tag_pos);
            int chap = (Integer) v.getTag(R.id.tag_chap_index);
            int begin = (Integer) v.getTag(R.id.tag_page_begin);
            ReaderPage rp = getReaderPage(pos);
            if (rp.begin != begin || rp.chapterIndex != chap) {
                v.setTag(R.id.tag_need_update, 1);
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
            if (h > tvHigh || i == count - 1) {
                if (i < count - 1) {
                    if (begin == 0) {
                        i = i - 2;
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
        String text = pageTexts.get(page.chapterIndex);
        if (text.indexOf(page.name) == -1) {
            text = page.name + "\n" + pageTexts.get(page.chapterIndex);
        }
        text = text.substring(page.begin, page.end);
        if (page.begin == 0) {
            int end = text.indexOf('\n');
            SpannableString sp = new SpannableString(text);
            int fs = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TITLE_FONT_SIZE_SP, tv.getResources().getDisplayMetrics());
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, tv.getResources().getDisplayMetrics());
            sp.setSpan(new DBReaderCenterSpan(fs, margin), 0, end, 0);
            tv.setText(sp);
            tv.setGravity(Gravity.BOTTOM);
        } else {
            tv.setText(text);
            tv.setGravity(Gravity.TOP);
        }
    }
}
