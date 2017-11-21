package com.moss.dbreader;


import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
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

        finish();
    }

    @Override
    public void onBackPressed(){
        System.exit(0);
    }

    public void initializeBookCaseList(){
        MainPageAdapter adapter = new MainPageAdapter(getSupportFragmentManager(),this.getApplicationContext());
        ViewPager vp = (ViewPager) findViewById(R.id.main_viewpager);
        vp.setAdapter(adapter);

        int count  = BookCaseManager.fetchNovelsInBookCase().size();
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
        vp.setVisibility(View.VISIBLE);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.app_cover_fragment);
        fragment.getView().setVisibility(View.GONE);

        Log.i("Andrei", "init finish");
    }

}
