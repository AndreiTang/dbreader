package com.moss.dbreader;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.moss.dbreader.fragment.NovelReaderFragment;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.ui.ReaderPanel;

public class ReaderActivity extends AppCompatActivity {


    private DBReaderNovel novel;

    ReaderPanel.IReadPanelNotify readPanelNotify = new ReaderPanel.IReadPanelNotify() {

        @Override
        public void onClickDict() {

        }

        @Override
        public void onClickCache() {

        }

        @Override
        public void onClickCase() {
            popupDlg(0);
        }

        @Override
        public void onClickSearch() {
            popupDlg(1);
        }

        @Override
        public void onClickDefault() {
            View rp = findViewById(R.id.reader_panel);
            rp.setVisibility(View.GONE);
        }
    };

    private void transferToMain(int index){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Common.TAG_MAIN_CATEGORY,index);
        startActivity(intent);
    }

    private void popupDlg(final int index){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.prompt_title);
        builder.setMessage(R.string.prompt_msg);
        builder.setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                novel.isInCase = 1;
                BookCaseManager.add(novel);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        this.novel = (DBReaderNovel)getIntent().getSerializableExtra("novel");

        BookCaseManager.saveDBReader(novel);
        BookCaseManager.add(novel);

        Fragment fragment = this.getSupportFragmentManager().findFragmentById(R.id.reader_fragment);
        if(fragment instanceof NovelReaderFragment){
            int engineID = getIntent().getIntExtra(Common.TAG_ENGINE_ID,0);
            int curPage = getIntent().getIntExtra(Common.TAG_CUR_PAGE,0);
            ((NovelReaderFragment)fragment).setNovelInfo(this.novel,engineID,curPage);
        }

        ReaderPanel rp = (ReaderPanel)findViewById(R.id.reader_panel);
        rp.setNotify(readPanelNotify);
    }
}
