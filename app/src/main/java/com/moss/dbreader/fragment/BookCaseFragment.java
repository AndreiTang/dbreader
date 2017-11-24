package com.moss.dbreader.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.moss.dbreader.BookCaseManager;
import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.ui.CasePageAdapter;

import java.util.ArrayList;

import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;

/**
 * Created by andrei on 2017/10/11.
 */

public class BookCaseFragment extends Fragment {

    private IFetchNovelEngineNotify notify = new IFetchNovelEngineNotify() {
        @Override
        public void OnSearchNovels(int nRet, int engineID, int sessionID, ArrayList<DBReaderNovel> novels) {

        }

        @Override
        public void OnFetchNovel(int nRet, int sessionID, DBReaderNovel novel) {

        }

        @Override
        public void OnFetchChapter(int nRet, int sessionID, int index, String cont) {

        }

        @Override
        public void OnCacheChapter(int nRet, String novelName, int index, String cont) {

        }

        @Override
        public void OnCacheChapterComplete(String novelName) {

        }

        @Override
        public void OnFetchDeltaChapterList(int nRet, int sessionID, DBReaderNovel novel, ArrayList<DBReaderNovel.Chapter> chapters) {
            if(nRet != NO_ERROR){
                return;
            }

            DBReaderNovel item = BookCaseManager.getNovel(novel.name);
            if(item == null){
                return;
            }
            item.isUpdated = 1;
            BookCaseFragment.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    GridView gv = (GridView)getActivity().findViewById(R.id.case_grid);
                    CasePageAdapter cp  = (CasePageAdapter)gv.getAdapter();
                    cp.notifyDataSetChanged();

                }
            });
        }
    };

    public IFetchNovelEngineNotify getFetchNovelEngineNotify(){
        return notify;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookcase,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        View v = getActivity().findViewById(R.id.book_case_search);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).switchFragment(1);
            }
        });
    }

    @Override
    public void  onResume(){
        super.onResume();
        CasePageAdapter cp = new CasePageAdapter(this);
        GridView gv = (GridView)getActivity().findViewById(R.id.case_grid);
        gv.setAdapter(cp);
    }

}
