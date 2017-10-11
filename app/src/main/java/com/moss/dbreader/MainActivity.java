package com.moss.dbreader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.moss.dbreader.service.NovelEngineService;


public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private NovelEngineService.NovelEngine engine = null;
    private int engineID;
    private ServiceConnection sc = null;
    private boolean bOK = false;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        NovelEngineService.NovelEngineBinder binder = (NovelEngineService.NovelEngineBinder) iBinder;
        engine = binder.getNovelEngine();

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.printf("this is a test");

        //Intent intent = new Intent(this, NovelEngineService.class);
       // bindService(intent, this, Context.BIND_AUTO_CREATE);



    }


}
