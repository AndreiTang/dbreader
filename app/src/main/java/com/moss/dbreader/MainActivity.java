package com.moss.dbreader;


import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.moss.dbreader.fragment.AppCoverFragment;
import com.moss.dbreader.fragment.BookCaseFragment;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.ui.MainPageAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        FontOverride.setDefaultFont(getApplicationContext(), "MONOSPACE","fonts/xinkai.ttf");
        setContentView(R.layout.activity_main);
    }


    public void switchToNovelReader(final DBReaderNovel novel){
        Intent intent = new Intent(this, ReaderActivity.class);
        intent.putExtra(Common.TAG_NOVEL,novel);
        startActivity(intent);
        //finish();
    }

    @Override
    public void onBackPressed(){
        //unbindService(AppCoverFragment.sc);
        //System.exit(0);
    }

    @Override
    public void onResume(){
        super.onResume();
        ViewPager vp = (ViewPager) findViewById(R.id.main_viewpager);
        MainPageAdapter adapter = (MainPageAdapter)vp.getAdapter();
        if(adapter != null){
            BookCaseFragment bookCaseFragment = (BookCaseFragment)adapter.getItem(0);
            //bookCaseFragment.intializeBookCase();
            int count  = BookCaseManager.fetchNovelsInBookCase().size();
            int index = 0;
            if(count == 0){
                index = 1;
            }
            vp.setCurrentItem(index);
        }

    }

    public void initializeBookCaseList(){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.app_cover_fragment);
        fragment.getView().setVisibility(View.GONE);

        MainPageAdapter adapter = new MainPageAdapter(getSupportFragmentManager(),this.getApplicationContext());
        ViewPager vp = (ViewPager) findViewById(R.id.main_viewpager);
        vp.setVisibility(View.VISIBLE);

        vp.setAdapter(adapter);

        int count  = BookCaseManager.fetchNovelsInBookCase().size();
        int index = 0;
        if(count == 0){
            index = 1;
        }
        vp.setCurrentItem(index);
        Log.i("Andrei", "init finish");
    }

}
