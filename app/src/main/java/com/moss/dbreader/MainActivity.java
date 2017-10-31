package com.moss.dbreader;


import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.moss.dbreader.ui.MainPageAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);

        MainPageAdapter adapter = new MainPageAdapter(getSupportFragmentManager(),this.getApplicationContext());
        ViewPager vp = (ViewPager) findViewById(R.id.main_viewpager);
        vp.setAdapter(adapter);

    }

}
