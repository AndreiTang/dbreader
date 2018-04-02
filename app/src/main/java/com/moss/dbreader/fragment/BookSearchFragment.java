package com.moss.dbreader.fragment;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.fragment.events.FetchEngineEvent;
import com.moss.dbreader.fragment.events.SwitchFragmentEvent;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelEngineService;
import com.moss.dbreader.service.events.FetchNovelEvent;
import com.moss.dbreader.service.events.SearchEvent;
import com.moss.dbreader.ui.SearchPageAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

import static com.moss.dbreader.service.IFetchNovelEngine.ERROR_NETWORK;
import static com.moss.dbreader.service.IFetchNovelEngine.ERROR_TOO_MANY;
import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookSearchFragment extends Fragment {

    private NovelEngineService.NovelEngine engine = null;
    //private int engineID = -1;
    private int sessionID = -1;
    private final int AllowCount = 5;
    private ArrayList<DBReaderNovel> novels = null;
    private View footView = null;
    private boolean isRunning = false;
    private SearchPageAdapter searchPageAdapter = null;


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearchEvent(SearchEvent event){
        if(this.sessionID != event.sessionID){
            return;
        }
        if (event.nRet != NO_ERROR) {
            showErrorInfo(event.nRet);
            return;
        }

        //this.engineID = event.engineID;
        this.sessionID = event.sessionID;

        ArrayList<DBReaderNovel> items = null;
        for(int i = event.novels.size() - 1 ; i >=0 ; i--){
            DBReaderNovel item = event.novels.get(i);
            if(item.chapters != null && item.chapters.size() > 0){
                if(items == null){
                    items = new ArrayList<DBReaderNovel>();
                }
                items.add(item);
                event.novels.remove(i);
            }
        }

        if(event.novels.size() > 0){
            this.novels = event.novels;
            fetchNovelDetails();
        }
        else{
            this.novels = null;
        }

        if(items != null){
            showSearchResult(items);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFetchNovelEvent(FetchNovelEvent event){
        if (this.sessionID != event.sessionID) {
            return;
        }
        if (event.nRet != NO_ERROR) {
            return;
        }
        showSearchResult(event.novels);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFetchEngine(FetchEngineEvent event){
        this.engine = event.engine;
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
        EventBus.getDefault().register(this);
        initializeSearchView();
        initializeListView();
        initializeProgressViews();
        View v = getActivity().findViewById(R.id.book_search_case);
        RxView.clicks(v).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {
                EventBus.getDefault().post(new SwitchFragmentEvent(0));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
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

    private void showSearchResult(ArrayList<DBReaderNovel> items) {

        View mask = getActivity().findViewById(R.id.search_mask);
        mask.setVisibility(View.GONE);
        final ListView lv = (ListView) getActivity().findViewById(R.id.search_list);
        lv.removeFooterView(footView);
        isRunning = false;

        boolean bNew = false;
        if (searchPageAdapter == null) {
            searchPageAdapter = new SearchPageAdapter(BookSearchFragment.this);
            bNew = true;
        }
        for (int i = 0; i < items.size(); i++) {
            searchPageAdapter.addNovel(items.get(i));
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
        if (lv.getAdapter() == null || novels == null ||novels.size() == 0 || lv.getFooterViewsCount() == 1 || isRunning == true) {
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

        int count = novels.size();
        if (count > AllowCount) {
            count = AllowCount;
        }
        ArrayList<DBReaderNovel> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DBReaderNovel item = novels.remove(0);
            items.add(item);
        }
        engine.fetchNovels(items,this.sessionID);
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

            int id = sv.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView textView = (TextView) sv.findViewById(id);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);


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
