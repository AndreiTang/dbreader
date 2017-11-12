package com.moss.dbreader.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.moss.dbreader.BookCaseManager;
import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;

import java.util.ArrayList;

import static com.bumptech.glide.request.RequestOptions.fitCenterTransform;

/**
 * Created by tangqif on 11/10/2017.
 */

public class CasePageAdapter extends BaseAdapter {

    private Context context;
    private Fragment fragment;
    private ArrayList<DBReaderNovel> novels;

    public CasePageAdapter(Context context, Fragment fragment){
        this.context = context;
        this.fragment = fragment;
        this.novels = BookCaseManager.fetchNovelsInBookCase();
    }

    @Override
    public int getCount() {
        return novels.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.view_case,parent,false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag(R.id.tag_pos);
                    DBReaderNovel novel = novels.get(pos);
                    ((MainActivity)fragment.getActivity()).switchToNovelReader(novel);
                }
            });
        }

        DBReaderNovel novel = this.novels.get(position);
        TextView tv = (TextView)view.findViewById(R.id.case_novel_title);
        tv.setText(novel.name);

        view.setTag(R.id.tag_pos,position);

        ImageView img = (ImageView) view.findViewById(R.id.case_novel_cover);
        Glide.with(fragment).clear(img);
        Glide.with(fragment).load(novel.img).apply(fitCenterTransform()).into(img);

        return view;
    }
}
