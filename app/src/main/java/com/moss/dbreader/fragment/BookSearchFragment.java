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

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.ui.SearchPageAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookSearchFragment extends Fragment {

    private NovelEngineService.NovelEngine engine = null;
    private int engineID = -1;
    private static int sessionID = 0;
    private int searchCount = 0;
    ArrayList<DBReaderNovel> tmpNovels = new ArrayList<DBReaderNovel>();
    private final int AllowCount = 5;
    private ArrayList<DBReaderNovel> novels;
    private View footView;
    private boolean isRunning = false;
    private SearchPageAdapter searchPageAdapter = null;
    private int currIndex = 0;


    IFetchNovelEngineNotify notify = new IFetchNovelEngineNotify() {
        @Override
        public void OnSearchNovels(boolean bRet, int engineID, int sessionID, final ArrayList<DBReaderNovel> novels) {
            if (!bRet || BookSearchFragment.sessionID != sessionID) {
                return;
            }
            BookSearchFragment.this.novels = novels;
            BookSearchFragment.this.engineID = engineID;
            fetchNovelDetails();
        }

        @Override
        public void OnFetchNovel(boolean bRet, int sessionID, DBReaderNovel novel) {
            if (BookSearchFragment.sessionID != sessionID) {
                return;
            }
            searchCount--;
            if (!bRet) {
                return;
            }
            tmpNovels.add(novel);
            if (searchCount == 0) {
                BookSearchFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final ListView lv = (ListView) getActivity().findViewById(R.id.search_list);
                        lv.removeFooterView(footView);
                        isRunning = false;
                        showSearchResult();
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
        footView = inflater.inflate(R.layout.view_search_progress, null);
        return inflater.inflate(R.layout.fragment_booksearch, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeSearchView();
        initializeListView();
        initializeFootView();

        Intent intent = new Intent(getActivity(), NovelEngineService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (engine != null) {
            getActivity().unbindService(serviceConnection);
            engine = null;
        }
    }

    private void initializeFootView() {
        SimpleDraweeView pv = (SimpleDraweeView) footView.findViewById(R.id.search_progress);
        Uri uri = Uri.parse("res://" + getContext().getPackageName() + "/" + R.drawable.progress_small);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        pv.setController(controller);
    }

    private void showSearchResult() {
        ListView lv = (ListView) getActivity().findViewById(R.id.search_list);
        boolean bNew = false;
        if (searchPageAdapter == null) {
            searchPageAdapter = new SearchPageAdapter(getActivity().getApplicationContext(), this);
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
            if (v.getBottom() <= lv.getHeight()) {
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
                sessionID++;
                engine.searchNovel(s, sessionID);
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
