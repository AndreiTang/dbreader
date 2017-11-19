package com.moss.dbreader.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.moss.dbreader.R;
import com.moss.dbreader.ui.CasePageAdapter;

/**
 * Created by andrei on 2017/10/11.
 */

public class BookCaseFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookcase,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        CasePageAdapter cp = new CasePageAdapter(this);
        GridView gv = (GridView)getActivity().findViewById(R.id.case_grid);
        gv.setAdapter(cp);
    }
}
