package com.moss.dbreader.ui;

import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
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

public class ReaderPageAdapter extends PagerAdapter {
    static public class ReaderPage {
        public int chapterIndex;
        public int begin;
        public int end;
    }

    private ArrayList<ReaderPage> pages = new ArrayList<ReaderPage>();
    private HashMap<Integer, String> pageTexts = new HashMap<Integer, String>();
    private ArrayList<View> views;
    private int textViewId;
    private int maskViewId;
    private boolean needUpdated = false;
    private ArrayList<View> usingViews = new ArrayList<View>();


    public ReaderPageAdapter(ArrayList<View> views, int tvId, int maskId) {
        this.views = views;
        textViewId = tvId;
        maskViewId = maskId;
    }

    public void addPage(ReaderPage page) {
        pages.add(page);
    }

    public void addText(int index, String text) {
        pageTexts.put(index, text);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ReaderPage page = getReaderPage(position);
        if (page == null) {
            return super.instantiateItem(container, position);
        }
        View view = views.get(0);
        views.remove(0);
        view.setTag(R.id.tag_pos, position);
        view.setTag(R.id.tag_chap_index, page.chapterIndex);
        TextView tv = (TextView) view.findViewById(textViewId);
        if (pageTexts.containsKey(page.chapterIndex) && page.begin != -2) {
            refreshPage(page, tv);
        } else {
            //View v = view.findViewById(maskViewId);
            //v.setVisibility(View.VISIBLE);
            //tv.setVisibility(View.INVISIBLE);
        }
        usingViews.add(view);
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
        if (needUpdated) {
            needUpdated = false;
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

    private ReaderPage getReaderPage(int pos) {
        return pages.get(pos);
    }

    private void refreshPage(final ReaderPage page, final TextView tv) {
        if (page.begin == -1) {
            initPageText(page, tv);
        } else {
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
                if (item.begin == -2) {
                    item.begin = -1;
                }
                break;
            }
        }
    }

    private void initPageText(final ReaderPage page, final TextView tv) {
        tv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                tv.getViewTreeObserver().removeOnPreDrawListener(this);
                allocatePages(page, tv);
                refreshOtherPages();
                needUpdated = true;
                ReaderPageAdapter.this.notifyDataSetChanged();
                return false;
            }
        });
        String text = pageTexts.get(page.chapterIndex);
        SpannableString sp = new SpannableString(text);
        int end = text.indexOf('\n');
        sp.setSpan(new RelativeSizeSpan(1.2f), 0, end, 0);
        tv.setText(sp);
    }

    private void refreshOtherPages() {
        for (int i = 0; i < usingViews.size(); i++) {
            View v = usingViews.get(i);
            int pos = (Integer) v.getTag(R.id.tag_pos);
            int chap = (Integer) v.getTag(R.id.tag_chap_index);
            ReaderPage rp = getReaderPage(pos);
            if (rp.chapterIndex == chap || rp.begin < 0) {
                continue;
            }
            setPageText(rp, (TextView) v.findViewById(textViewId));
        }
    }

    private void allocatePages(final ReaderPage page, final TextView tv) {
        int count = tv.getLineCount();
        int h = 0;
        Rect rc = new Rect();
        int begin = 0;
        for (int i = 0; i < count; i++) {
            tv.getLineBounds(i, rc);
            h += rc.height();
            if (h > tv.getHeight() || i == count - 1) {
                if (i < count - 1) {
                    i--;
                }
                int end = tv.getLayout().getLineEnd(i);
                ReaderPage rp = new ReaderPage();
                rp.chapterIndex = page.chapterIndex;
                rp.begin = begin;
                rp.end = end;
                rp.chapterIndex = page.chapterIndex;
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
        text = text.substring(page.begin, page.end);
        if (page.begin == 0) {
            int end = text.indexOf('\n');
            SpannableString sp = new SpannableString(text);
            sp.setSpan(new RelativeSizeSpan(1.2f), 0, end, 0);
            tv.setText(sp);
        } else {
            tv.setText(text);
        }
    }
}
