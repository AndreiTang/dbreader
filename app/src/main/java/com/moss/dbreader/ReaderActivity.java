package com.moss.dbreader;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.moss.dbreader.fragment.BookCoverFragment;
import com.moss.dbreader.fragment.NovelReaderFragment;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.ui.ReaderPageAdapter;
import com.moss.dbreader.ui.ReaderPanel;

public class ReaderActivity extends AppCompatActivity {

    ReaderPanel.IReadPanelNotify readPanelNotify = new ReaderPanel.IReadPanelNotify() {

        @Override
        public void onClickDict() {
            ReaderPanel rp = (ReaderPanel) findViewById(R.id.reader_panel);
            rp.setVisibility(View.GONE);

            NovelReaderFragment novelReaderfragment = (NovelReaderFragment) getSupportFragmentManager().findFragmentById(R.id.reader_fragment);
            int currIndex = novelReaderfragment.getCurrentChapterIndex();

            BookCoverFragment bookCoverfragment = (BookCoverFragment) ReaderActivity.this.getSupportFragmentManager().findFragmentById(R.id.book_cover_fragment);
            bookCoverfragment.getView().setVisibility(View.VISIBLE);
            bookCoverfragment.setSelection(currIndex);
        }

        @Override
        public void onClickCache() {
            View rp = findViewById(R.id.reader_panel);
            rp.setVisibility(View.GONE);
            NovelReaderFragment novelReaderfragment = (NovelReaderFragment) getSupportFragmentManager().findFragmentById(R.id.reader_fragment);
            novelReaderfragment.cacheChapters();
        }

        @Override
        public void onClickCase() {
            exitReader(-0);
        }

        @Override
        public void onClickSearch() {
            exitReader(1);
        }

        @Override
        public void onClickDefault() {
            View rp = findViewById(R.id.reader_panel);
            rp.setVisibility(View.GONE);
        }
    };
/////////////////////////////////////////////////////
    private DBReaderNovel novel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        this.novel = (DBReaderNovel) getIntent().getSerializableExtra("novel");

        ReaderPanel rp = (ReaderPanel) findViewById(R.id.reader_panel);
        rp.setNotify(readPanelNotify);

        Fragment fragment = this.getSupportFragmentManager().findFragmentById(R.id.reader_fragment);
        ((NovelReaderFragment) fragment).setNovel(this.novel);
        fragment = this.getSupportFragmentManager().findFragmentById(R.id.book_cover_fragment);
        ((BookCoverFragment) fragment).setNovel(this.novel);

    }

    @Override
    public void onBackPressed(){
        exitReader(-1);
    }

    public void changeChapter(DBReaderNovel.Chapter chapter) {
        NovelReaderFragment novelReaderfragment = (NovelReaderFragment) getSupportFragmentManager().findFragmentById(R.id.reader_fragment);
        novelReaderfragment.changeChapter(chapter);
    }

    private void transferToMain(int index) {
        finish();
    }

    private void popupDlg(final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.prompt_title);
        builder.setMessage(R.string.prompt_msg);
        builder.setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                novel.isInCase = 1;
                transferToMain(index);
            }
        });
        builder.setNegativeButton(R.string.prompt_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                transferToMain(index);
            }
        });

        builder.create().show();

    }


    private void exitReader(int index){
        if(ReaderActivity.this.novel.isInCase == 0){
            popupDlg(index);
        }
        else{
            transferToMain(index);
        }
    }
}
