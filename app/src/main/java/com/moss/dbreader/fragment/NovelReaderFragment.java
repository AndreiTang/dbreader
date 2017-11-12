package com.moss.dbreader.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.moss.dbreader.BookCaseManager;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.ui.IReaderPageAdapterNotify;
import com.moss.dbreader.ui.ReaderPageAdapter;
import com.moss.dbreader.ui.ReaderPanel;
import com.moss.dbreader.ui.ReaderPanel.IReadPanelNotify;

import java.net.URI;
import java.util.ArrayList;

import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;

/**
 * Created by tangqif on 2017/10/9.
 */

public class NovelReaderFragment extends Fragment {

    private NovelEngineService.NovelEngine engine = null;
    //private int engineID = -1;
    private int sessionID = 0;
    private DBReaderNovel novel;
    private ReaderPageAdapter adapter = null;
    private int tmpIndex = -1;

    private GestureDetector.OnDoubleTapListener doubleTapListener = new GestureDetector.OnDoubleTapListener(){

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

            final String chap = BookCaseManager.getChapterText(novel.name,index);
            if(chap.length() > 0){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NovelReaderFragment.this.adapter.addText(index, chap);
                    }
                });
                return;
            }

            if (NovelReaderFragment.this.engine == null) {
                NovelReaderFragment.this.tmpIndex = index;
            } else {
                NovelReaderFragment.this.engine.fetchChapter(NovelReaderFragment.this.novel.chapters.get(index), NovelReaderFragment.this.novel.engineID, NovelReaderFragment.this.sessionID);
            }
        }
    };

    IFetchNovelEngineNotify fetchNovelEngineNotify = new IFetchNovelEngineNotify() {
        @Override
        public void OnSearchNovels(int nRet, int engineID, int sessionID, final ArrayList<DBReaderNovel> novels) {

        }

        @Override
        public void OnFetchNovel(int nRet, int sessionID, DBReaderNovel novel) {

        }

        @Override
        public void OnFetchChapter(int nRet, int sessionID, final int index, final String cont) {
            Log.i("Andrei", "index is " + index + " text arrived" + " session is " + sessionID + " ret is " + nRet);
            if (NovelReaderFragment.this.sessionID != sessionID) {
                return;
            }
            if (nRet != NO_ERROR) {
                return;
            }
            BookCaseManager.saveChapterText(novel.name,index,cont);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    NovelReaderFragment.this.adapter.addText(index, cont);
                }
            });

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
                engine.fetchChapter(novel.chapters.get(tmpIndex), NovelReaderFragment.this.novel.engineID, sessionID);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            NovelReaderFragment.this.engine.removeNotify(fetchNovelEngineNotify);
        }
    };


    @Override
    public void  onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        initializeViewPager();
    }


    public void setNovelInfo(DBReaderNovel novel) {
        this.novel = novel;
        initializeAdapter(novel.currPage);
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
            this.engine = null;
        }
    }

    private void initializeViewPager() {
        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.reader_viewpager);
        ArrayList<View> views = new ArrayList<View>();
        detector.setOnDoubleTapListener(doubleTapListener);
        for (int i = 0; i < 4; i++) {
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
        this.adapter = new ReaderPageAdapter(views, R.id.reader_text, R.id.reader_chapter_title, R.id.reader_chapter_page_no, R.id.reader_mask, readerPageAdapterNotify);
        vp.addOnPageChangeListener(adapter);
    }

    private void initializeProgressBar(View v){
        SimpleDraweeView pv = (SimpleDraweeView) v.findViewById(R.id.reader_progress);
        Uri uri = Uri.parse("res://" + getContext().getPackageName() + "/" + R.drawable.progress_big);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        pv.setController(controller);
    }

    private void initializeAdapter(int curPage){
        for (int i = 0; i < this.novel.chapters.size(); i++) {
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
