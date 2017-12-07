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

import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;

public class MainActivity extends AppCompatActivity {

    private static boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        FontOverride.setDefaultFont(getApplicationContext(), "MONOSPACE", "fonts/xinkai.ttf");
        EventBus.getDefault().register(this);
        // setContentView(R.layout.activity_main);

        if (isFirst) {
            createAppCoverFragment();
            isFirst = false;
        } else {
            createBookCaseFragment();
        }
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
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


    protected void createBookCaseFragment() {
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, new MainFragment());
        ft.commit();
    }

    protected void createAppCoverFragment() {
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, new AppCoverFragment());
        ft.commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    void onInitializedEvent(InitializedEvent event) {
        createBookCaseFragment();
        Common.changeStatusBarColor(this, Color.parseColor("#DBC49B"));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    void onCacheChaptersEvent(CacheChaptersEvent event) {
        String msg = MainActivity.this.getResources().getString(R.string.cache_complete);
        msg = event.novelName + " " + msg;
        Toast.makeText(this, msg, Toast.LENGTH_LONG);
    }
}
