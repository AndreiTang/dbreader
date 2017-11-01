package com.moss.dbreader;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.moss.dbreader.fragment.NovelReaderFragment;
import com.moss.dbreader.service.DBReaderNovel;

public class ReaderActivity extends AppCompatActivity {


    private DBReaderNovel novel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        this.novel = (DBReaderNovel)getIntent().getSerializableExtra("novel");
        Fragment fragment = this.getSupportFragmentManager().findFragmentById(R.id.reader_fragment);
        if(fragment instanceof NovelReaderFragment){
            int engineID = getIntent().getIntExtra(Common.TAG_ENGINE_ID,0);
            int curPage = getIntent().getIntExtra(Common.TAG_CUR_PAGE,0);
            String str = engineID + " " + curPage;
            ((NovelReaderFragment)fragment).setNovelInfo(this.novel,engineID,curPage);
        }
    }
}
