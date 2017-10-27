package com.moss.dbreader.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by tangqif on 10/23/2017.
 */

public class SearchPageAdapter extends BaseAdapter {

    private ArrayList<DBReaderNovel> novels = new ArrayList<DBReaderNovel>();
    private Context context;
    private ArrayList<View> views = new ArrayList<View>();

    public SearchPageAdapter(Context context){
        this.novels = novels;
        this.context = context;
    }

    public void addNovel(DBReaderNovel novel){
        novels.add(novel);
    }

    public void refresh(){
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return novels.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.view_search,viewGroup,false);
            views.add(view);
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
        return view;
    }
}
