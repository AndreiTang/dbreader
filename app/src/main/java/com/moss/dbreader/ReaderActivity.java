package com.moss.dbreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
            NovelReaderFragment novelReaderfragment = (NovelReaderFragment) getSupportFragmentManager().findFragmentById(R.id.reader_fragment);
            int currIndex = novelReaderfragment.getCurrentChapterIndex();
            BookCoverFragment bookCoverfragment = (BookCoverFragment) ReaderActivity.this.getSupportFragmentManager().findFragmentById(R.id.book_cover_fragment);
            bookCoverfragment.getView().setVisibility(View.VISIBLE);
            bookCoverfragment.setSelection(currIndex);
        }

        @Override
        public void onClickCache() {
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

    private BroadcastReceiver homeKeyEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra("reason");
                if (TextUtils.equals(reason, "homekey")) {
                    homeClick();
                } else if (TextUtils.equals(reason, "recentapps")) {
                    homeClick();
                }
            }
        }
    };

    /////////////////////////////////////////////////////
    private DBReaderNovel novel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        if (savedInstanceState != null) {
            this.novel = (DBReaderNovel) savedInstanceState.getSerializable("novel");
        } else {
            this.novel = (DBReaderNovel) getIntent().getSerializableExtra("novel");
        }

        ReaderPanel rp = (ReaderPanel) findViewById(R.id.reader_panel);
        rp.setNotify(readPanelNotify);

        Fragment fragment = this.getSupportFragmentManager().findFragmentById(R.id.reader_fragment);
        ((NovelReaderFragment) fragment).setNovel(this.novel);
        fragment = this.getSupportFragmentManager().findFragmentById(R.id.book_cover_fragment);
        ((BookCoverFragment) fragment).setNovel(this.novel);

        registerReceiver(this.homeKeyEventReceiver,new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        Common.changeStatusBarColor(this, Color.parseColor("#E0E0E0"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("novel", this.novel);
    }

    @Override
    public void onBackPressed() {
        exitReader(-1);
    }

    public void changeChapter(DBReaderNovel.Chapter chapter) {
        NovelReaderFragment novelReaderfragment = (NovelReaderFragment) getSupportFragmentManager().findFragmentById(R.id.reader_fragment);
        novelReaderfragment.changeChapter(chapter);
    }

    private void transferToMain(int index) {
        Log.i("Andrei", "Destroy reader");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Common.TAG_MAIN_CATEGORY, index);
        startActivity(intent);
        finish();
    }

    private void homeClick() {
        unregisterReceiver(this.homeKeyEventReceiver);
        this.homeKeyEventReceiver = null;
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Common.TAG_NOVEL, this.novel.name);
        startActivity(intent);
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


    private void exitReader(int index) {
        if (ReaderActivity.this.novel.isInCase == 0) {
            popupDlg(index);
        } else {
            transferToMain(index);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(this.homeKeyEventReceiver != null){
            unregisterReceiver(this.homeKeyEventReceiver);
        }
    }
}
