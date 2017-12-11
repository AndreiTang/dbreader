package com.moss.dbreader.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.moss.dbreader.fragment.events.SwitchFragmentEvent;
import com.moss.dbreader.service.IFetchNovelEngine;

import com.moss.dbreader.R;
import com.moss.dbreader.service.events.FetchDeltaChapterListEvent;
import com.moss.dbreader.ui.CasePageAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by andrei on 2017/10/11.
 */

public class BookCaseFragment extends Fragment {

    CasePageAdapter cp = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookcase, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeAdater();
        intializeSearchBtn();
        initializeEditBtn();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initializeAdater() {
        this.cp = new CasePageAdapter(this);
        GridView gv = (GridView) getActivity().findViewById(R.id.case_grid);
        gv.setAdapter(cp);
    }

    private void initializeEditBtn() {
        View v = getView().findViewById(R.id.book_case_edit);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cp.getMode() == CasePageAdapter.MODE_NORMAL) {
                    cp.setMode(CasePageAdapter.MODE_REMOVE);
                    TextView tv = (TextView) v;
                    tv.setText(R.string.book_case_remove);
                } else {
                    cp.setMode(CasePageAdapter.MODE_NORMAL);
                    TextView tv = (TextView) v;
                    tv.setText(R.string.book_case_edit);
                }
            }
        });
    }

    private void intializeSearchBtn() {
        View v = getActivity().findViewById(R.id.book_case_search);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new SwitchFragmentEvent(1));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFetchDeltaChapterListEvent(FetchDeltaChapterListEvent event) {
        if (event.nRet != IFetchNovelEngine.NO_ERROR || cp == null) {
            return;
        }
        cp.notifyDataSetChanged();
    }
}
