package com.moss.dbreader.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.moss.dbreader.BookCaseManager;
import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.ui.SearchPageAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.moss.dbreader.service.IFetchNovelEngine.ERROR_NETWORK;
import static com.moss.dbreader.service.IFetchNovelEngine.ERROR_NO_RESULT;
import static com.moss.dbreader.service.IFetchNovelEngine.ERROR_TOO_MANY;
import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookSearchFragment extends Fragment {

    private NovelEngineService.NovelEngine engine = null;
    private int engineID = -1;
    private int sessionID = 0;
    private int searchCount = 0;
    ArrayList<DBReaderNovel> tmpNovels = new ArrayList<DBReaderNovel>();
    private final int AllowCount = 5;
    private ArrayList<DBReaderNovel> novels;
    private View footView;
    private boolean isRunning = false;
    private SearchPageAdapter searchPageAdapter = null;


    IFetchNovelEngineNotify notify = new IFetchNovelEngineNotify() {
        @Override
        public void OnSearchNovels(int nRet, int engineID, int sessionID, final ArrayList<DBReaderNovel> novels) {
            if(BookSearchFragment.this.sessionID != sessionID){
                return;
            }
            if (nRet != NO_ERROR) {
                BookSearchFragment.this.showErrorInfo(nRet);
                return;
            }
            BookSearchFragment.this.novels = novels;
            BookSearchFragment.this.engineID = engineID;
            BookSearchFragment.this.sessionID = sessionID;
            if(novels.size() == 1){
                tmpNovels.clear();
                DBReaderNovel novel = BookSearchFragment.this.novels.get(0);
                BookSearchFragment.this.tmpNovels.add(novel);
                BookSearchFragment.this.novels.remove(0);
                showSearchResult();
            }
            else{
                BookSearchFragment.this.fetchNovelDetails();
            }

        }

        @Override
        public void OnFetchNovel(int nRet, int sessionID, DBReaderNovel novel) {
            if (BookSearchFragment.this.sessionID != sessionID) {
                return;
            }
            searchCount--;
            if (nRet != NO_ERROR) {
                return;
            }
            tmpNovels.add(novel);
            if (searchCount == 0) {
                showSearchResult();
            }
        }

        @Override
        public void OnFetchChapter(int nRet, int sessionID, int index, String cont) {

        }

        @Override
        public void OnCacheChapter(int nRet, String novelName, int index, String cont) {
            if (nRet != NO_ERROR) {
                return;
            }
            BookCaseManager.saveChapterText(novelName,index,cont);
        }

        @Override
        public void OnCacheChapterComplete(String novelName) {
            String msg = getActivity().getResources().getString(R.string.cache_complete);
            msg = novelName + " " + msg;
            Toast.makeText(BookSearchFragment.this.getActivity(),msg,Toast.LENGTH_LONG);
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
        footView = inflater.inflate(R.layout.view_search_progress, null);
        return inflater.inflate(R.layout.fragment_booksearch, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeSearchView();
        initializeListView();
        initializeProgressViews();
    }

    @Override
    public void onStart(){
        super.onStart();

        Intent intent = new Intent(getActivity(), NovelEngineService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onStop() {
        super.onStop();

        if (engine != null) {
            engine.cancel(sessionID);
            getActivity().unbindService(serviceConnection);
            engine = null;
        }

        ListView lv = (ListView) getActivity().findViewById(R.id.search_list);
        lv.setAdapter(null);
        lv.addFooterView(null);
        this.searchPageAdapter = null;
    }

    private void initializeProgressViews() {
        SimpleDraweeView pv = (SimpleDraweeView) footView.findViewById(R.id.search_progress);
        Uri uri = Uri.parse("res://" + getContext().getPackageName() + "/" + R.drawable.progress_small);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        pv.setController(controller);

        pv = (SimpleDraweeView) getActivity().findViewById(R.id.searching_progress);
        uri = Uri.parse("res://" + getContext().getPackageName() + "/" + R.drawable.progress_big);
        controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        pv.setController(controller);

    }

    private void showSearchResult() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View mask = getActivity().findViewById(R.id.search_mask);
                mask.setVisibility(View.GONE);
                final ListView lv = (ListView) getActivity().findViewById(R.id.search_list);
                lv.removeFooterView(footView);
                isRunning = false;

                boolean bNew = false;
                if (searchPageAdapter == null) {
                    searchPageAdapter = new SearchPageAdapter(BookSearchFragment.this,BookSearchFragment.this.engineID);
                    bNew = true;
                }
                for (int i = 0; i < tmpNovels.size(); i++) {
                    searchPageAdapter.addNovel(tmpNovels.get(i));
                }
                if (bNew == true) {
                    lv.setAdapter(searchPageAdapter);
                } else {
                    searchPageAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void initializeListView() {
        ListView lv = (ListView) getActivity().findViewById(R.id.search_list);
        lv.setFooterDividersEnabled(false);
        float dividerHigh = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
        lv.setDividerHeight((int) dividerHigh);
        Drawable dw = lv.getDivider();
        dw.setAlpha(0);
        lv.setSelected(true);
        lv.setVerticalScrollBarEnabled(false);

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                ListView lv = (ListView) absListView;
                int pos = i + i1 - 1;
                scrollToEnd(lv, pos);
            }
        });
    }

    private void scrollToEnd(ListView lv, int pos) {
        if (lv.getAdapter() == null || novels.size() == 0 || lv.getFooterViewsCount() == 1 || isRunning == true) {
            return;
        }

        if (lv.getAdapter().getCount() - 1 == pos) {
            int index = lv.getLastVisiblePosition();
            View v = null;
            for (int j = 0; j < lv.getChildCount(); j++) {
                v = lv.getChildAt(j);
                int itemPos = (Integer) v.getTag(R.id.tag_pos);
                if (itemPos == pos) {
                    break;
                }
            }
            if (v.getBottom()  <= lv.getHeight()) {
                Log.i("Andrei","Scroll End");
                lv.addFooterView(footView);
                isRunning = true;
                fetchNovelDetails();
            }
        }
    }


    private void fetchNovelDetails() {
        if (novels.size() == 0) {
            return;
        }
        tmpNovels.clear();
        searchCount = novels.size();
        if (searchCount > AllowCount) {
            searchCount = AllowCount;
        }
        for (int i = 0; i < searchCount; i++) {
            engine.fetchNovel(novels.get(0), engineID, sessionID);
            novels.remove(0);
        }
    }

    private void initializeSearchView() {
        SearchView sv = (SearchView) getActivity().findViewById(R.id.book_search);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                startSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        customizeSearchView(sv);
    }

    private void startSearch(String s) {
        engine.cancel(sessionID);
        sessionID = engine.generateSessionID();
        ListView lv = (ListView) getActivity().findViewById(R.id.search_list);
        if(lv.getFooterViewsCount() == 1){
            lv.removeFooterView(footView);
        }
        lv.setAdapter(null);
        searchPageAdapter = null;
        View mask = getActivity().findViewById(R.id.search_mask);
        mask.setVisibility(View.VISIBLE);
        mask.findViewById(R.id.searching_progress).setVisibility(View.VISIBLE);
        mask.findViewById(R.id.searching_text).setVisibility(View.VISIBLE);
        mask.findViewById(R.id.searching_err).setVisibility(View.GONE);
        engine.searchNovel(s, sessionID);
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

    private void showErrorInfo(final int err){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View mask = getActivity().findViewById(R.id.search_mask);
                mask.setVisibility(View.VISIBLE);
                mask.findViewById(R.id.searching_progress).setVisibility(View.GONE);
                mask.findViewById(R.id.searching_text).setVisibility(View.GONE);
                mask.findViewById(R.id.searching_err).setVisibility(View.VISIBLE);
                String errInfo;
                if(err == ERROR_NETWORK){
                    errInfo = getActivity().getResources().getString(R.string.book_search_err_network);
                }
                else if(err==ERROR_TOO_MANY){
                    errInfo = getActivity().getResources().getString(R.string.book_search_err_too_many);
                }
                else{
                    errInfo = getActivity().getResources().getString(R.string.book_search_err_no_result);
                }
                TextView tx = (TextView)mask.findViewById(R.id.searching_err);
                tx.setText(errInfo);
            }
        });
    }


}
