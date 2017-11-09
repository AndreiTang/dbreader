package com.moss.dbreader.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.moss.dbreader.BookCaseManager;
import com.moss.dbreader.Common;
import com.moss.dbreader.R;
import com.moss.dbreader.ReaderActivity;
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
        this.novels = BookCaseManager.getNovels();
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
//            views.add(view);
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int pos = (Integer) v.getTag(R.id.tag_pos);
//                    DBReaderNovel novel = novels.get(pos);
//                    Intent intent = new Intent(fragment.getActivity(), ReaderActivity.class);
//                    intent.putExtra(Common.TAG_NOVEL,novel);
//                    intent.putExtra(Common.TAG_ENGINE_ID,engineID);
//                    intent.putExtra(Common.TAG_CUR_PAGE,0);
//                    fragment.getActivity().startActivity(intent);
//                    fragment.getActivity().finish();
//                }
//            });
        }

        DBReaderNovel novel = this.novels.get(position);
        TextView tv = (TextView)view.findViewById(R.id.case_novel_title);
        tv.setText(novel.name);

        ImageView img = (ImageView) view.findViewById(R.id.case_novel_cover);
        Glide.with(fragment).clear(img);
        Glide.with(fragment).load(novel.img).apply(fitCenterTransform()).into(img);

        return view;
    }
}
