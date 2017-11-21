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
import com.moss.dbreader.service.IFetchNovelEngine;
import com.moss.dbreader.service.NovelEngineService;

import java.util.ArrayList;

/**
 * Created by andrei on 2017/11/21.
 */

public class AppCoverFragment extends Fragment {
    boolean isInitialized = false;
    private  ServiceConnection serviceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            NovelEngineService.NovelEngineBinder binder = (NovelEngineService.NovelEngineBinder) iBinder;
            final NovelEngineService.NovelEngine engine = binder.getNovelEngine();

            Thread thrd = new Thread(new Runnable() {
                @Override
                public void run() {
                    BookCaseManager.initialize(getContext().getFilesDir().getAbsolutePath());
                    ArrayList<DBReaderNovel> novels = BookCaseManager.fetchNovelsInBookCase();

                    if(novels.size() == 0 ){
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    AppCoverFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity activity = (MainActivity) AppCoverFragment.this.getActivity();
                            activity.initializeBookCaseList();
                        }
                    });

                    if(novels.size() > 0){
                        for(int i = 0; i < novels.size(); i++){
                            DBReaderNovel item = novels.get(i);
                            DBReaderNovel novel = new DBReaderNovel();
                            novel.url = item.url;
                            if(engine.fetchChapterDirectly(novel,item.engineID) == IFetchNovelEngine.NO_ERROR){
                                item.chapters.clear();
                                item.chapters = novel.chapters;
                                BookCaseManager.saveDBReader(item);
                            }
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_appcover,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(),"fonts/wawa.ttf");
        TextView tv = (TextView)getActivity().findViewById(R.id.cover_title);
        tv.setTypeface(typeface);

        if (isInitialized == false) {
            Intent intent = new Intent(getActivity(), NovelEngineService.class);
            getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            isInitialized = true;
        }
        else{
            getView().setVisibility(View.GONE);
        }
    }

}
