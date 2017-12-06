package com.moss.dbreader.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.ui.IReaderPageAdapterNotify;
import com.moss.dbreader.ui.ReaderPageAdapter;
import com.moss.dbreader.ui.ReaderPanel;

import java.util.ArrayList;

import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;

/**
 * Created by tangqif on 2017/10/9.
 */

public class NovelReaderFragment extends Fragment {


    private GestureDetector.OnDoubleTapListener doubleTapListener = new GestureDetector.OnDoubleTapListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            ReaderPanel rp = (ReaderPanel) getActivity().findViewById(R.id.reader_panel);
            rp.setVisibility(View.VISIBLE);
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    };

    private GestureDetector detector = new GestureDetector(NovelReaderFragment.this.getContext(), new GestureDetector.OnGestureListener() {
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

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    });


    IReaderPageAdapterNotify readerPageAdapterNotify = new IReaderPageAdapterNotify() {
        @Override
        public void update(final int index) {
            if (NovelReaderFragment.this.engine == null) {
                NovelReaderFragment.this.tmpIndex = index;
            } else {
                NovelReaderFragment.this.engine.fetchChapter(NovelReaderFragment.this.novel,
                        NovelReaderFragment.this.novel.chapters.get(index),
                        NovelReaderFragment.this.sessionID);
            }
        }
    };

    IFetchNovelEngineNotify fetchNovelEngineNotify = new IFetchNovelEngineNotify() {
        @Override
        public void OnSearchNovels(int nRet, int engineID, int sessionID, final ArrayList<DBReaderNovel> novels) {

        }

        @Override
        public void OnFetchNovel(int nRet, int sessionID, final DBReaderNovel novel) {


        }

        @Override
        public void OnFetchChapter(int nRet, int sessionID, final int index, final String cont) {
            if (NovelReaderFragment.this.sessionID != sessionID) {
                return;
            }
            if (nRet != NO_ERROR) {
                NovelReaderFragment.this.adapter.error(index);
                return;
            }
            NovelReaderFragment.this.adapter.addText(index, cont);
        }

        @Override
        public void OnCacheChapterComplete(final String novelName) {
            Log.i("Andrei",novelName + " cache finish");
            String msg = getActivity().getResources().getString(R.string.cache_complete);
            msg = novelName + " " + msg;
            Toast.makeText(NovelReaderFragment.this.getActivity(), msg, Toast.LENGTH_LONG).show();
        }

        @Override
        public void OnFetchDeltaChapterList(int nRet, String novelName, final ArrayList<DBReaderNovel.Chapter> chapters) {
            if(nRet != NO_ERROR || NovelReaderFragment.this.novel.name.compareTo(novel.name) != 0){
                return;
            }
            updateChapters(chapters);
        }

    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            NovelEngineService.NovelEngineBinder binder = (NovelEngineService.NovelEngineBinder) iBinder;
            NovelReaderFragment.this.engine = binder.getNovelEngine();
            NovelReaderFragment.this.engine.addNotify(fetchNovelEngineNotify);
            NovelReaderFragment.this.sessionID = engine.generateSessionID();
            if (tmpIndex != -1) {
                engine.fetchChapter(novel,
                        novel.chapters.get(tmpIndex),
                        sessionID);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            NovelReaderFragment.this.engine.removeNotify(fetchNovelEngineNotify);
        }
    };
    /////////////////////////////////////////////////////////////////////////
    private NovelEngineService.NovelEngine engine = null;
    //private int engineID = -1;
    private int sessionID = -1;
    private DBReaderNovel novel;
    private ReaderPageAdapter adapter = null;
    private int tmpIndex = -1;

    private final static String TAG_PAGES = "tag_pages";
    private final static String TAG_CURR_PAGE = "tag_cur_page";
    private final static String TAG_NOVEL = "tag_novel";
    private long beginTime = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeViewPager();
        initializeAdapter();

//        if (savedInstanceState != null) {
//            onRestoreInstanceState(savedInstanceState);
//        } else {
//            initializeAdapter();
//        }
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (this.adapter == null) {
//            return;
//        }
//
//        ArrayList<ReaderPageAdapter.ReaderPage> rps = new ArrayList<ReaderPageAdapter.ReaderPage>();
//        int count = this.adapter.getCount();
//        for (int i = 0; i < count; i++) {
//            ReaderPageAdapter.ReaderPage rp = this.adapter.getReaderPage(i);
//            rps.add(rp);
//        }
//        outState.putSerializable(NovelReaderFragment.TAG_PAGES, rps);
//
//        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.reader_viewpager);
//        int curr = vp.getCurrentItem();
//        outState.putInt(NovelReaderFragment.TAG_CURR_PAGE, curr);
//        outState.putSerializable(NovelReaderFragment.TAG_NOVEL, this.novel);
//    }


    public void setNovel(DBReaderNovel novel) {
        this.novel = novel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_novelreader, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), NovelEngineService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.engine != null) {
            this.engine.cancel(this.sessionID);
            getActivity().unbindService(this.serviceConnection);
            NovelReaderFragment.this.engine.removeNotify(fetchNovelEngineNotify);
            this.engine = null;
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        this.beginTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.novel.duration += (System.currentTimeMillis() - this.beginTime);
        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.reader_viewpager);
        this.novel.currPage = vp.getCurrentItem();
        this.novel.currChapter = adapter.getReaderPage(this.novel.currPage).chapterIndex;
        NovelInfoManager.saveReaderPages(this.novel.name, this.adapter.getPages());
        NovelInfoManager.add(novel, true);
        NovelInfoManager.saveDBReader(novel);
    }

    public void cacheChapters() {
        engine.cacheChapters(this.novel);
    }

    public int getCurrentChapterIndex() {
        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.reader_viewpager);
        int curr = vp.getCurrentItem();
        ReaderPageAdapter adapter = (ReaderPageAdapter) vp.getAdapter();
        ReaderPageAdapter.ReaderPage rp = adapter.getReaderPage(curr);
        return rp.chapterIndex;
    }

    public void changeChapter(DBReaderNovel.Chapter chapter) {
        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.reader_viewpager);
        ReaderPageAdapter adapter = (ReaderPageAdapter) vp.getAdapter();
        int curIndex = adapter.getFirstPageOfChapterIndex(chapter.index);
        if (curIndex != -1) {
            adapter.setCurrentItem(curIndex);
            vp.setCurrentItem(curIndex);
        }
    }

    private void updateChapters(ArrayList<DBReaderNovel.Chapter> chapters){
        for(int i = 0 ;i< chapters.size(); i++){
            DBReaderNovel.Chapter item = chapters.get(i);
            ReaderPageAdapter.ReaderPage rp = new ReaderPageAdapter.ReaderPage();
            rp.name = item.name;
            rp.chapterIndex = item.index;
            rp.begin = ReaderPageAdapter.FLAG_PREVIOUS_PAGE;
            this.adapter.addPage(rp);
        }
        this.adapter.notifyDataSetChanged();
    }

//    private void onRestoreInstanceState(Bundle outState) {
//        ArrayList<ReaderPageAdapter.ReaderPage> rpList = (ArrayList<ReaderPageAdapter.ReaderPage>) outState.getSerializable(NovelReaderFragment.TAG_PAGES);
//        for (int i = 0; i < rpList.size(); i++) {
//            this.adapter.addPage(rpList.get(i));
//        }
//
//        this.novel = (DBReaderNovel) outState.getSerializable(NovelReaderFragment.TAG_NOVEL);
//
//        int curPage = outState.getInt(NovelReaderFragment.TAG_CURR_PAGE);
//
//        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.reader_viewpager);
//        vp.setAdapter(adapter);
//        vp.setCurrentItem(curPage);
//    }


    private void initializeViewPager() {
        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.reader_viewpager);
        ArrayList<View> views = new ArrayList<View>();
        detector.setOnDoubleTapListener(doubleTapListener);
        for (int i = 0; i < 8; i++) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.view_reader, null);
            views.add(v);
            initializeProgressBar(v);
            v.setLongClickable(true);
            v.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return detector.onTouchEvent(event);
                }
            });
        }
        this.adapter = new ReaderPageAdapter(views, R.id.reader_text, R.id.reader_chapter_title, R.id.reader_chapter_page_no, R.id.reader_mask,R.id.reader_error, readerPageAdapterNotify);
        vp.addOnPageChangeListener(adapter);
    }

    private void initializeProgressBar(View v) {
        SimpleDraweeView pv = (SimpleDraweeView) v.findViewById(R.id.reader_progress);
        Uri uri = Uri.parse("res://" + getContext().getPackageName() + "/" + R.drawable.progress_big);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        pv.setController(controller);
    }

    private void initializeAdapter() {
        int i = 0;
        int curPage = this.novel.currPage;
        ArrayList<ReaderPageAdapter.ReaderPage> rps = NovelInfoManager.readReaderPages(this.novel.name);
        if (rps != null) {
            for (i = 0; i < rps.size(); i++) {
                this.adapter.addPage(rps.get(i));
            }
            ReaderPageAdapter.ReaderPage rp = rps.get(rps.size() - 1);
            i = rp.chapterIndex;
        }
        for (; i < this.novel.chapters.size(); i++) {
            ReaderPageAdapter.ReaderPage rp = new ReaderPageAdapter.ReaderPage();
            rp.begin = ReaderPageAdapter.FLAG_PREVIOUS_PAGE;
            rp.chapterIndex = i;
            rp.name = this.novel.chapters.get(i).name;
            this.adapter.addPage(rp);
        }
        this.adapter.setCurrentItem(curPage);
        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.reader_viewpager);
        vp.setAdapter(adapter);
        vp.setCurrentItem(curPage);

    }

}
