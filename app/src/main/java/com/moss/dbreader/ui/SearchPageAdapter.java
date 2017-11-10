package com.moss.dbreader.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.moss.dbreader.Common;
import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.ReaderActivity;
import com.moss.dbreader.service.DBReaderNovel;

import static com.bumptech.glide.request.RequestOptions.fitCenterTransform;
import java.util.ArrayList;

/**
 * Created by tangqif on 10/23/2017.
 */

public class SearchPageAdapter extends BaseAdapter {

    private ArrayList<DBReaderNovel> novels = new ArrayList<DBReaderNovel>();
    private Context context;
    private Fragment fragment;
    private int engineID;
    private ArrayList<View> views = new ArrayList<View>();

    public SearchPageAdapter(Context context, Fragment fragment, int engineID){
        this.context = context;
        this.fragment = fragment;
        this.engineID = engineID;
    }

    public void addNovel(DBReaderNovel novel){
        novels.add(novel);
    }

    @Override
    public int getCount() {
        return novels.size();
    }

    @Override
    public Object getItem(int i)
    {
        if(i >= novels.size()){
            return null;
        }
        return novels.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.view_search,viewGroup,false);
            views.add(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag(R.id.tag_pos);
                    DBReaderNovel novel = novels.get(pos);
                    novel.engineID = SearchPageAdapter.this.engineID;
                    novel.currPage = 0;
                    ((MainActivity)fragment.getActivity()).switchToNovelReader(novel);
                }
            });
        }

        view.setTag(R.id.tag_pos,i);

        DBReaderNovel item = novels.get(i);
        TextView tv = (TextView) view.findViewById(R.id.search_novel_name);
        tv.setText(item.name);
        tv = (TextView) view.findViewById(R.id.search_novel_author);
        tv.setText(item.author);
        tv = (TextView) view.findViewById(R.id.search_novel_decs);
        tv.setText(item.decs);
        tv = (TextView) view.findViewById(R.id.search_novel_type);
        tv.setText(item.type);

        ImageView img = (ImageView) view.findViewById(R.id.search_novel_cover);
        Glide.with(fragment).clear(img);
        Glide.with(fragment).load(item.img).apply(fitCenterTransform()).into(img);
        return view;
    }

}
