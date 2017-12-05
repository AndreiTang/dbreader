package com.moss.dbreader.fragment;

import android.content.ComponentName;
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

import java.util.ArrayList;


/**
 * Created by andrei on 2017/11/21.
 */

public class AppCoverFragment extends Fragment {

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            NovelEngineService.NovelEngineBinder binder = (NovelEngineService.NovelEngineBinder) iBinder;
            AppCoverFragment.this.engine = binder.getNovelEngine();
            Thread thrd = new Thread(new Runnable() {
                @Override
                public void run() {
                    NovelInfoManager.initialize(getContext().getFilesDir().getAbsolutePath());
                    ArrayList<DBReaderNovel> novels = NovelInfoManager.fetchNovelsInBookCase();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    AppCoverFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity activity = (MainActivity) AppCoverFragment.this.getActivity();
                            activity.switchMainUI();
                        }
                    });

                    int sessionID = engine.generateSessionID();
                    if (novels.size() > 0) {
                        for (int i = 0; i < novels.size(); i++) {
                            DBReaderNovel item = novels.get(i);
                            engine.fetchDeltaChapterList(item);
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
    private NovelEngineService.NovelEngine engine = null;

    public ServiceConnection getServiceConnection(){
        return this.serviceConnection;
    }

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

}
