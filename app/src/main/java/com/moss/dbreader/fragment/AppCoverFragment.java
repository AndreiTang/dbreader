package com.moss.dbreader.fragment;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moss.dbreader.BookCaseManager;
import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelEngineService;

import java.util.ArrayList;

import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;

/**
 * Created by andrei on 2017/11/21.
 */

public class AppCoverFragment extends Fragment {

    private IFetchNovelEngineNotify notify = new IFetchNovelEngineNotify() {
        @Override
        public void OnSearchNovels(int nRet, int engineID, int sessionID, ArrayList<DBReaderNovel> novels) {

        }

        @Override
        public void OnFetchNovel(int nRet, int sessionID, DBReaderNovel novel) {
            if(nRet != NO_ERROR || sessionID != AppCoverFragment.this.sessionID){
                return;
            }

            DBReaderNovel item = BookCaseManager.getNovel(novel.name);
            if(item != null && item.chapters.size() != novel.chapters.size()){
                item.chapters.clear();
                item.chapters = novel.chapters;
                BookCaseManager.saveDBReader(item);
            }
        }

        @Override
        public void OnFetchChapter(int nRet, int sessionID, int index, String cont) {

        }

        @Override
        public void OnCacheChapter(int nRet, String novelName, int index, String cont) {

        }

        @Override
        public void OnCacheChapterComplete(String novelName) {

        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            NovelEngineService.NovelEngineBinder binder = (NovelEngineService.NovelEngineBinder) iBinder;
            AppCoverFragment.this.engine  = binder.getNovelEngine();
            engine.addNotify(AppCoverFragment.this.notify);

            Thread thrd = new Thread(new Runnable() {
                @Override
                public void run() {
                    BookCaseManager.initialize(getContext().getFilesDir().getAbsolutePath());
                    ArrayList<DBReaderNovel> novels = BookCaseManager.fetchNovelsInBookCase();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    AppCoverFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity activity = (MainActivity) AppCoverFragment.this.getActivity();
                            activity.initializeBookCaseList();
                        }
                    });

                    AppCoverFragment.this.sessionID = engine.generateSessionID();

                    if (novels.size() > 0) {
                        for (int i = 0; i < novels.size(); i++) {
                            DBReaderNovel item = novels.get(i);
                            DBReaderNovel novel = new DBReaderNovel();
                            novel.url = item.url;
                            novel.name = item.name;
                            novel.engineID = item.engineID;
                            engine.fetchNovel(novel,item.engineID,AppCoverFragment.this.sessionID);
                        }
                    }

                }
            });
            thrd.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    ////////////////////////////////
    private int sessionID = -1;
    private NovelEngineService.NovelEngine engine = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_appcover, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/wawa.ttf");
        TextView tv = (TextView) getActivity().findViewById(R.id.cover_title);
        tv.setTypeface(typeface);

        if(savedInstanceState == null){
            Intent intent = new Intent(getActivity(), NovelEngineService.class);
            getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.engine.removeNotify(this.notify);
        getActivity().unbindService(this.serviceConnection);
    }
}
