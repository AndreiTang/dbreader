package com.moss.dbreader.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookSearchFragment extends Fragment implements IFetchNovelEngineNotify {


    public BookSearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       return inflater.inflate(R.layout.fragment_booksearch, container, false);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        customizeSearchView();

        SearchView sv = (SearchView) getActivity().findViewById(R.id.book_search);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                MainActivity activity = (MainActivity) getActivity();
                activity.getNovelEngine().searchNovel(s,0);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        MainActivity activity = (MainActivity) getActivity();
        activity.getNovelEngine().addNotify(this);

    }

    private void customizeSearchView(){
        SearchView sv = (SearchView) getActivity().findViewById(R.id.book_search);
        Class<?> classSV = sv.getClass();
        try {
            Field fd = classSV.getDeclaredField("mSearchPlate");
            fd.setAccessible(true);
            View tv = (View)fd.get(sv);
            tv.setBackgroundColor(Color.TRANSPARENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnSearchNovels(boolean bRet, int engineID, int sessionID, ArrayList<DBReaderNovel> novels) {

    }

    @Override
    public void OnFetchNovel(boolean bRet, int engineID, int sessionID, DBReaderNovel novel) {

    }

    @Override
    public void OnFetchChapter(boolean bRet, int sessionID, int index, String cont) {

    }
}
