package com.moss.dbreader.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.moss.dbreader.fragment.BookCaseFragment;
import com.moss.dbreader.fragment.BookSearchFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tangqif on 10/31/2017.
 */

public class MainPageAdapter extends FragmentPagerAdapter {
    private Context context;
    private final static String[] fragments = { BookCaseFragment.class.getName(),BookSearchFragment.class.getName()};//
    private HashMap<Integer,Fragment> fragmentMap = new HashMap<Integer, Fragment>();

    public MainPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if(this.fragmentMap.containsKey(position)){
            return fragmentMap.get(position);
        }
        else{
            Fragment fragment = Fragment.instantiate(this.context,fragments[position]);
            fragmentMap.put(position,fragment);
            return fragment;
        }

    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}
