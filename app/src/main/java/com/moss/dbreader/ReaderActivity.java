package com.moss.dbreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.moss.dbreader.service.DBReaderNovel;

public class ReaderActivity extends AppCompatActivity {


    private DBReaderNovel novel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        this.novel = (DBReaderNovel)getIntent().getSerializableExtra("novel");


    }
}
