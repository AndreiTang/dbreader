package com.moss.dbreader.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moss.dbreader.Common;
import com.moss.dbreader.R;
import com.moss.dbreader.fragment.events.SwitchFragmentEvent;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.ui.MainPageAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by tangqif on 12/7/2017.
 */

public class MainFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        Common.changeStatusBarColor(getActivity(), Color.parseColor("#DBC49B"));
        initializeAdapter();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe
    public void onSwitchFragmentEvent(SwitchFragmentEvent event){
        ViewPager vp = (ViewPager) getView().findViewById(R.id.main_viewpager);
       vp.setCurrentItem(event.index);
    }

    private void initializeAdapter(){
        MainPageAdapter adapter = new MainPageAdapter(this.getChildFragmentManager(), getContext());
        ViewPager vp = (ViewPager) getView().findViewById(R.id.main_viewpager);
        vp.setAdapter(adapter);

        int index = -1;
        Bundle args = getArguments();
        if(args != null){
            index = args.getInt(Common.TAG_MAIN_CATEGORY,-1);
        }
        int count = NovelInfoManager.fetchNovelsInBookCase().size();
        if(index == -1 && count == 0){
            index = 1;
        }
        vp.setCurrentItem(index);
    }
}
