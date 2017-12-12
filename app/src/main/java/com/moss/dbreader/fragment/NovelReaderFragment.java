package com.moss.dbreader.fragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moss.dbreader.Common;
import com.moss.dbreader.fragment.events.FetchEngineEvent;
import com.moss.dbreader.fragment.events.RefreshNovelsEvent;
import com.moss.dbreader.fragment.events.SwitchFragmentEvent;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.service.events.FetchChapterEvent;
import com.moss.dbreader.service.events.FetchDeltaChapterListEvent;
import com.moss.dbreader.ui.ReaderPageAdapter;
import com.moss.dbreader.ui.ReaderPanel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;

/**
 * Created by tangqif on 2017/10/9.
 */

public class NovelReaderFragment extends Fragment implements IBackPress {
    private NovelEngineService.NovelEngine engine = null;
    private int sessionID = -1;
    private DBReaderNovel novel;
    private ReaderPageAdapter adapter = null;
    private long beginTime = 0;

    @Subscribe
    public void onFetchText(ReaderPageAdapter.FetchTextEvent event){
        this.engine.fetchChapter(this.novel,
                this.novel.chapters.get(event.chapterIndex),
                this.sessionID);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFetchChapterEvent(FetchChapterEvent event){
        if (this.sessionID != event.sessionID) {
            return;
        }
        if (event.nRet != NO_ERROR) {
            NovelReaderFragment.this.adapter.error(event.chapterIndex);
            return;
        }
        NovelReaderFragment.this.adapter.addText(event.chapterIndex, event.cont);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFetchDeltaChapterListEvent(FetchDeltaChapterListEvent event){
        if(event.nRet != NO_ERROR || this.novel.name.compareTo(event.novelName) != 0){
            return;
        }
        updateChapters(event.chapters);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFetchEngine(FetchEngineEvent event){
        this.engine = event.engine;
        this.sessionID = engine.generateSessionID();
        initializeViewPager();
        initializeAdapter();

    }

    @Subscribe
    public void onReadPanel_ToMain_Event(ReaderPanel.ReadPanel_ToMain_Event event){
        int id = 0;
        if(event.id == ReaderPanel.CLICK_SEARCH){
            id = 1;
        }
        back(id);
    }

    @Override
    public void onBackPress() {
        back(-1);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Common.changeStatusBarColor(getActivity(), Color.parseColor("#E0E0E0"));
        EventBus.getDefault().register(this);

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

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().post(new RefreshNovelsEvent());
        EventBus.getDefault().unregister(this);
        if (this.engine != null) {
            this.engine.cancel(this.sessionID);
            this.engine = null;
        }
    }

//    private int getCurrentChapterIndex() {
//        ViewPager vp = (ViewPager) getActivity().findViewById(R.id.reader_viewpager);
//        int curr = vp.getCurrentItem();
//        ReaderPageAdapter adapter = (ReaderPageAdapter) vp.getAdapter();
//        ReaderPageAdapter.ReaderPage rp = adapter.getReaderPage(curr);
//        return rp.chapterIndex;
//    }

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
        this.adapter = new ReaderPageAdapter(this);
        vp.addOnPageChangeListener(adapter);
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

    private void transferToMain(int index){
        Common.changeStatusBarColor(getActivity(), Color.parseColor("#DBC49B"));
        getActivity().getSupportFragmentManager().popBackStack();
        if(index != -1){
            EventBus.getDefault().post(new SwitchFragmentEvent(index));
        }
    }

    private void popupConfirmDlg(final int index){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.prompt_title);
        builder.setMessage(R.string.prompt_msg);
        builder.setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                novel.isInCase = 1;
                transferToMain(index);
            }
        });
        builder.setNegativeButton(R.string.prompt_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                transferToMain(index);
            }
        });
        builder.create().show();
    }

    private void back(int index){
        if(novel.isInCase == 0){
            popupConfirmDlg(index);
        }
        else{
            transferToMain(index);
        }
    }

}
