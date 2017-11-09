package com.moss.dbreader;


import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.ui.MainPageAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BookCaseManager.initialize(getApplicationContext().getFilesDir().getAbsolutePath());
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);

        MainPageAdapter adapter = new MainPageAdapter(getSupportFragmentManager(),this.getApplicationContext());
        ViewPager vp = (ViewPager) findViewById(R.id.main_viewpager);
        vp.setAdapter(adapter);

        int count = 0;
        ArrayList<DBReaderNovel> novels = BookCaseManager.getNovels();
        for(int i = 0 ; i < novels.size(); i++){
            DBReaderNovel novel = novels.get(i);
            if(novel.isInCase == 1){
                count++;
            }
        }

        int index = getIntent().getIntExtra(Common.TAG_MAIN_CATEGORY,-1);
        if(index == -1){
            if(count == 0){
                index = 1;
            }
            else{
                index = 0;
            }
        }
        vp.setCurrentItem(index);
    }



}
