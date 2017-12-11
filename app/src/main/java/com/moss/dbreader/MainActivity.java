package com.moss.dbreader;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.moss.dbreader.fragment.MainFragment;
import com.moss.dbreader.fragment.events.FetchEngineEvent;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.service.events.CacheChaptersEvent;
import com.moss.dbreader.service.events.InitializedEvent;
import com.moss.dbreader.ui.MainPageAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;

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

//    @Override
//    public void onBackPressed() {
//       // System.exit(0);
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    protected void createBookCaseFragment() {
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, new MainFragment());
        ft.addToBackStack(null);
        ft.commit();
        Common.changeStatusBarColor(this, Color.parseColor("#DBC49B"));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCacheChaptersEvent(CacheChaptersEvent event) {
        String msg = MainActivity.this.getResources().getString(R.string.cache_complete);
        msg = event.novelName + " " + msg;
        Toast.makeText(this, msg, Toast.LENGTH_LONG);
    }

    private void initializeEngine(NovelEngineService.NovelEngine engine){
        this.engine = engine;
        EventBus.getDefault().postSticky(new FetchEngineEvent(engine));

        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
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
                        createBookCaseFragment();
                    }
                });
    }
}
