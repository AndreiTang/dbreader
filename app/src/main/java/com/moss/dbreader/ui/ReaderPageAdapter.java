package com.moss.dbreader.ui;

import android.graphics.Rect;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
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
    private ArrayList<View> views  = new ArrayList<View>();;
    private ArrayList<View> usingViews = new ArrayList<View>();
    private Fragment fragment;

    private static final int TITLE_FONT_SIZE_SP = 20;
    public static final int FLAG_CURR_PAGE = -1;
    public static final int FLAG_PREVIOUS_PAGE = -2;

    public ReaderPageAdapter(Fragment fragment) {
        this.fragment = fragment;
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
        TextView tv = (TextView) view.findViewById(R.id.reader_chapter_title);
        tv.setText(page.name);
        tv = (TextView) view.findViewById(R.id.reader_chapter_page_no);
        String pageNo = (position + 1) + "/" + pages.size();
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
        v.setLongClickable(true);
        RxView.longClicks(v).subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {
                ReaderPanel rp = (ReaderPanel) ReaderPageAdapter.this.fragment.getView().findViewById(R.id.reader_panel);
                int chapIndex = (int) v.getTag(R.id.tag_chap_index);
                rp.setCurrIndex(chapIndex);
                rp.setVisibility(View.VISIBLE);
            }
        });

        final View err = v.findViewById(R.id.reader_error);
        RxView.clicks(err).throttleFirst(1,TimeUnit.SECONDS).subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {
                int chapIndex = (int) v.getTag(R.id.tag_chap_index);
                EventBus.getDefault().post(new FetchTextEvent(chapIndex));
                err.setVisibility(View.GONE);
            }
        });
        return v;
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
                i++;
                line = line.replace(" ", "");
                if (i == 1) {
                    if (line.indexOf(title) != -1) {
                        text = chapterName + "\n\n";
                        hasTitle = true;
                    } else {
                        text = "    " + line + "\n";
                    }
                } else {
                    if (line.indexOf(title) != -1) {
                        continue;
                    }
                    line = "    " + line;
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
        tv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                tv.getViewTreeObserver().removeOnPreDrawListener(this);
                int tvHigh = tv.getHeight();
                allocatePages(page, tv, tvHigh);
                updatePages();
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

    private void updatePages() {
        for (int i = 0; i < usingViews.size(); i++) {
            View v = usingViews.get(i);
            int pos = (Integer) v.getTag(R.id.tag_pos);
            int chap = (Integer) v.getTag(R.id.tag_chap_index);
            int begin = (Integer) v.getTag(R.id.tag_page_begin);
            ReaderPage rp = getReaderPage(pos);
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
            if (h > tvHigh || i == count - 1) {
                if (h > tvHigh) {
                    if (begin == 0) {
                        i = i - 2;
                    } else {
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
            tv.setGravity(Gravity.CENTER_VERTICAL);
            if (len == page.end) {
                tv.setGravity(Gravity.TOP);
            }
        }
    }
}
