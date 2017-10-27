package com.moss.dbreader.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.widget.ListView;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.ui.SearchPageAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookSearchFragment extends Fragment  {

    private NovelEngineService.NovelEngine engine = null;
    private int engineID = -1;
    private int searchCount = 0;
    ArrayList<DBReaderNovel> tmpNovels = new ArrayList<DBReaderNovel>();
    private static final int AllowCount = 5;


    IFetchNovelEngineNotify notify = new IFetchNovelEngineNotify() {
        @Override
        public void OnSearchNovels(boolean bRet, int engineID, int sessionID, final ArrayList<DBReaderNovel> novels) {
            if(!bRet){
                return;
            }
            BookSearchFragment.this.engineID = engineID;
            searchCount = novels.size();
            if(searchCount > AllowCount){
                searchCount = AllowCount;
            }
            for(int i = 0 ; i < searchCount ; i++){
                engine.fetchNovel(novels.get(i),engineID,sessionID);
            }
        }

        @Override
        public void OnFetchNovel(boolean bRet, int sessionID, DBReaderNovel novel) {
            searchCount--;
            if(!bRet){
                return;
            }
            tmpNovels.add(novel);
            if(searchCount == 0 || tmpNovels.size() == AllowCount){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView lv = (ListView) getActivity().findViewById(R.id.search_list);
                        SearchPageAdapter adapter = new SearchPageAdapter(getActivity().getApplicationContext());
                        for(int i = 0 ; i < tmpNovels.size(); i++){
                            adapter.addNovel(tmpNovels.get(i));
                        }
                        lv.setAdapter(adapter);
                    }
                });
            }
        }

        @Override
        public void OnFetchChapter(boolean bRet, int sessionID, int index, String cont) {

        }
    };

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            NovelEngineService.NovelEngineBinder binder = (NovelEngineService.NovelEngineBinder) iBinder;
            engine = binder.getNovelEngine();
            engine.addNotify(notify);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            engine.removeNotify(notify);
        }
    };


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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeSearchView();
        initializeListView();



//        ArrayList<DBReaderNovel> novels = new ArrayList<DBReaderNovel>();
//        DBReaderNovel nv = new DBReaderNovel();
//        nv.name = "官居一品";
//        nv.author="三戒大师";
//        nv.decs="数风流，论成败，百年一梦多慷慨。有心要励精图治挽天倾，哪怕身后骂名滚滚,论功名还看今朝";
//        nv.type="连载";
//        novels.add(nv);
//        nv = new DBReaderNovel();
//        nv.name = "官居一品";
//        nv.author="三戒大师";
//        nv.decs="数风流，论成败，百年一梦多慷慨。有心要励精图治挽天倾，哪怕身后骂名滚滚";
//        nv.type="连载";
//        novels.add(nv);
//        SearchPageAdapter adapter = new SearchPageAdapter(getActivity().getApplicationContext(), novels);
//        ListView lv = (ListView) getActivity().findViewById(R.id.search_list);
//        lv.setAdapter(adapter);

        //Intent intent = new Intent(getActivity(), NovelEngineService.class);
        //getActivity().bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(engine != null){
            getActivity().unbindService(serviceConnection);
            engine = null;
        }
    }

    private void initializeListView(){
        ListView lv = (ListView) getActivity().findViewById(R.id.search_list);
        lv.setFooterDividersEnabled(false);
        float dividerHigh = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,getResources().getDisplayMetrics());
        lv.setDividerHeight((int)dividerHigh);
        Drawable dw = lv.getDivider();
        dw.setAlpha(0);
    }

    private void initializeSearchView() {
        SearchView sv = (SearchView) getActivity().findViewById(R.id.book_search);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                engine.searchNovel(s, 0);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        customizeSearchView(sv);
    }

    private void customizeSearchView(SearchView sv) {
        Class<?> classSV = sv.getClass();
        try {
            Field fd = classSV.getDeclaredField("mSearchPlate");
            fd.setAccessible(true);
            View tv = (View) fd.get(sv);
            tv.setBackgroundColor(Color.TRANSPARENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
