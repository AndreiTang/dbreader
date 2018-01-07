package com.moss.dbreader;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.moss.dbreader.fragment.BookCoverFragment;
import com.moss.dbreader.fragment.IBackPress;
import com.moss.dbreader.fragment.MainFragment;
import com.moss.dbreader.fragment.NovelReaderFragment;
import com.moss.dbreader.fragment.events.FetchEngineEvent;
import com.moss.dbreader.fragment.events.StatusBarVisibleEvent;
import com.moss.dbreader.fragment.events.SwitchToMainEvent;
import com.moss.dbreader.fragment.events.SwitchToNovelReaderEvent;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.service.events.CacheChaptersEvent;
import com.moss.dbreader.service.events.InitializedEvent;
import com.moss.dbreader.ui.ReaderPanel;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            NovelEngineService.NovelEngineBinder binder = (NovelEngineService.NovelEngineBinder) iBinder;
            initializeEngine(binder.getNovelEngine());
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    ///////////////////////////////////////////////////////////////////////////

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCacheChaptersEvent(CacheChaptersEvent event) {
        String msg = MainActivity.this.getResources().getString(R.string.cache_complete);
        msg = event.novelName + " " + msg;
        Toast.makeText(this, msg, Toast.LENGTH_LONG);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSwitchToNovelReaderEvent(SwitchToNovelReaderEvent event){
        NovelReaderFragment fragment = new NovelReaderFragment();
        fragment.setNovel(event.novel);
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, fragment);
        ft.commit();
    }

    @Subscribe
    public void onStatusBarVisibleEvent(StatusBarVisibleEvent event){
        if(event.isVisible){
            findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        else{
           findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Subscribe
    public void onSwitchToMainEvent(SwitchToMainEvent event){
        createMainFragment(event.index);
    }

    @Subscribe
    public void onReadPanel_Dict_Event(ReaderPanel.ReadPanel_Dict_Event event){
        BookCoverFragment fragment = new BookCoverFragment();
        fragment.setNovel(event.novel);
        Bundle arg = new Bundle();
        arg.putInt(Common.TAG_CUR_PAGE,event.index);
        fragment.setArguments(arg);
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.add(android.R.id.content, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    protected NovelEngineService.NovelEngine engine = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fresco.initialize(this);
        FontOverride.setDefaultFont(getApplicationContext(), "MONOSPACE", "fonts/xinkai.ttf");

        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        Intent intent = new Intent(this, NovelEngineService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fs = getSupportFragmentManager().getFragments();
        Fragment fragment = fs.get(fs.size()-1);
        if(fragment instanceof IBackPress){
            ((IBackPress)fragment).onBackPress();
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    protected void createMainFragment(int index) {
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        Fragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(Common.TAG_MAIN_CATEGORY,index);
        fragment.setArguments(args);
        ft.replace(android.R.id.content,fragment);
        ft.commit();
    }



    private void initializeEngine(NovelEngineService.NovelEngine engine){
        this.engine = engine;
        EventBus.getDefault().postSticky(new FetchEngineEvent(engine));

        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                NovelInfoManager.initialize(getApplicationContext().getFilesDir().getAbsolutePath());
                EventBus.getDefault().post(new InitializedEvent());
                ArrayList<DBReaderNovel> novels = NovelInfoManager.getNovels();
                for(int i = 0;i < novels.size(); i++){
                    DBReaderNovel item = novels.get(i);
                    if(item.isInCase == 1){
                        MainActivity.this.engine.fetchDeltaChapterList(item);
                    }
                }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        createMainFragment(-1);
                    }
                });
    }
}
