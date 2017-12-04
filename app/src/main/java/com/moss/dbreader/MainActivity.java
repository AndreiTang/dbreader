package com.moss.dbreader;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.moss.dbreader.fragment.AppCoverFragment;
import com.moss.dbreader.fragment.BookCaseFragment;
import com.moss.dbreader.fragment.BookSearchFragment;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.ui.MainPageAdapter;

import java.util.ArrayList;

import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;

public class MainActivity extends AppCompatActivity {

    private IFetchNovelEngineNotify notify = new IFetchNovelEngineNotify() {
        @Override
        public void OnSearchNovels(int nRet, int engineID, int sessionID, ArrayList<DBReaderNovel> novels) {
            for(int i = 0 ; i < MainActivity.this.notifies.size(); i++){
                IFetchNovelEngineNotify notify = MainActivity.this.notifies.get(i);
                if(notify != null){
                    notify.OnSearchNovels(nRet,engineID,sessionID,novels);
                }
            }
        }

        @Override
        public void OnFetchNovel(int nRet, int sessionID, DBReaderNovel novel) {
            for(int i = 0 ; i < MainActivity.this.notifies.size(); i++){
                IFetchNovelEngineNotify notify = MainActivity.this.notifies.get(i);
                if(notify != null){
                    notify.OnFetchNovel(nRet,sessionID,novel);
                }
            }
        }

        @Override
        public void OnFetchChapter(int nRet, int sessionID, int index, String cont) {

        }

        @Override
        public void OnCacheChapter(int nRet, String novelName, int index, String cont) {
            if (nRet != NO_ERROR) {
                return;
            }
            NovelInfoManager.saveChapterText(novelName,index,cont);
        }

        @Override
        public void OnCacheChapterComplete(final String novelName) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String msg = MainActivity.this.getResources().getString(R.string.cache_complete);
                    msg = novelName + " " + msg;
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG);
                }
            });
        }

        @Override
        public void OnFetchDeltaChapterList(int nRet, int sessionID, DBReaderNovel novel, ArrayList<DBReaderNovel.Chapter> chapters) {
            if (nRet != NO_ERROR ) {
                return;
            }
            DBReaderNovel item = NovelInfoManager.getNovel(novel.name);
            if (item != null ) {
                item.chapters.addAll(chapters);
                NovelInfoManager.saveDBReader(item);
            }

            for(int i = 0 ; i < MainActivity.this.notifies.size(); i++){
                IFetchNovelEngineNotify notify = MainActivity.this.notifies.get(i);
                if(notify != null){
                    notify.OnFetchDeltaChapterList(nRet,sessionID,novel,chapters);
                }
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NovelEngineService.NovelEngineBinder binder = (NovelEngineService.NovelEngineBinder) service;
            MainActivity.this.engine = binder.getNovelEngine();
            MainActivity.this.engine.addNotify(MainActivity.this.notify);
            for(int i = 0 ; i < MainActivity.this.serviceConnections.size(); i++){
                ServiceConnection sc = MainActivity.this.serviceConnections.get(i);
                if(sc != null){
                    sc.onServiceConnected(name,service);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };




    ////////////////////////////////////////////////
    private NovelEngineService.NovelEngine engine = null;
    private ArrayList<IFetchNovelEngineNotify> notifies = new ArrayList<IFetchNovelEngineNotify>();
    private ArrayList<ServiceConnection> serviceConnections = new ArrayList<ServiceConnection>();
    private String currNovelName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        FontOverride.setDefaultFont(getApplicationContext(), "MONOSPACE","fonts/xinkai.ttf");
        setContentView(R.layout.activity_main);


        AppCoverFragment fragment = (AppCoverFragment) getSupportFragmentManager().findFragmentById(R.id.app_cover_fragment);
        if(savedInstanceState != null){
            fragment.getView().setVisibility(View.GONE);
            findViewById(R.id.main_viewpager).setVisibility(View.VISIBLE);
        }
        else{
            this.serviceConnections.add(fragment.getServiceConnection());
        }

        MainPageAdapter adapter = new MainPageAdapter(getSupportFragmentManager(),this.getApplicationContext());
        ViewPager vp = (ViewPager) findViewById(R.id.main_viewpager);
        vp.setAdapter(adapter);

        BookCaseFragment caseFragment = (BookCaseFragment)adapter.getItem(0);
        this.notifies.add(caseFragment.getFetchNovelEngineNotify());

        BookSearchFragment searchFragment = (BookSearchFragment)adapter.getItem(1);
        this.serviceConnections.add(searchFragment.getServiceConnection());
        this.notifies.add(searchFragment.getFetchNovelEngineNotify());


        Intent intent = new Intent(this, NovelEngineService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        if(savedInstanceState != null){
            this.currNovelName = savedInstanceState.getString(Common.TAG_NOVEL);
            if(this.currNovelName != null && this.currNovelName.length() > 0){
                DBReaderNovel novel = NovelInfoManager.getNovel(this.currNovelName);
                switchToNovelReader(novel);
            }
        }
    }


    public void switchToNovelReader(final DBReaderNovel novel){
        this.currNovelName = novel.name;
        Intent intent = new Intent(this, ReaderActivity.class);
        intent.putExtra(Common.TAG_NOVEL,novel);
        startActivity(intent);
        if(this.engine != null){
            this.engine.removeNotify(this.notify);
            Log.i("Andrei", "engine not null");
        }
    }

    @Override
    public void onBackPressed(){
        //unbindService(AppCoverFragment.sc);
        System.exit(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.engine.removeNotify(this.notify);
        this.unbindService(this.serviceConnection);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(this.engine != null){
            this.engine.addNotify(notify);
        }
        this.currNovelName = null;
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        int index = intent.getIntExtra(Common.TAG_MAIN_CATEGORY,-1);
        if(index != -1){
            ViewPager vp = (ViewPager) findViewById(R.id.main_viewpager);
            vp.setCurrentItem(index);
            getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        String name = intent.getStringExtra(Common.TAG_NOVEL);
        if(name != null && name.length() > 0){
            DBReaderNovel novel = NovelInfoManager.getNovel(name);
            switchToNovelReader(novel);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if(this.currNovelName != null){
            outState.putString(Common.TAG_NOVEL,this.currNovelName);
        }
    }


    public void switchFragment(int index)
    {
        ViewPager vp = (ViewPager) findViewById(R.id.main_viewpager);
        vp.setCurrentItem(index);
    }

    public void switchMainUI(){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.app_cover_fragment);
        fragment.getView().setVisibility(View.GONE);

        ViewPager vp = (ViewPager) findViewById(R.id.main_viewpager);
        vp.setVisibility(View.VISIBLE);

        int count  = NovelInfoManager.fetchNovelsInBookCase().size();
        int index = 0;
        if(count == 0){
            index = 1;
        }
        vp.setCurrentItem(index);

        Common.changeStatusBarColor(this, Color.parseColor("#DBC49B"));
    }

}
