package com.moss.dbreader.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.Common;
import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.service.events.InitializedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;


/**
 * Created by andrei on 2017/11/21.
 */

public class AppCoverFragment extends Fragment {

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            NovelEngineService.NovelEngineBinder binder = (NovelEngineService.NovelEngineBinder) iBinder;
            NovelEngineService.NovelEngine engine = binder.getNovelEngine();
            engine.loadNovels();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    ////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_appcover, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Common.changeStatusBarColor(getActivity(),Color.argb(255,206,175,121));

        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/wawa.ttf");
        TextView tv = (TextView) getActivity().findViewById(R.id.cover_title);
        tv.setTypeface(typeface);


    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().unbindService(this.serviceConnection);
    }

}
