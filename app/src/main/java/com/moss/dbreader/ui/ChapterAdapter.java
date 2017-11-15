package com.moss.dbreader.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.moss.dbreader.R;
import com.moss.dbreader.ReaderActivity;
import com.moss.dbreader.service.DBReaderNovel;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by tangqif on 11/14/2017.
 */

public class ChapterAdapter extends BaseAdapter {

    private ArrayList<DBReaderNovel.Chapter> chapters = null;
    private Context context;
    private Fragment fragment;

    public ChapterAdapter(Context context, Fragment fragment, ArrayList<DBReaderNovel.Chapter> chapters){
        this.chapters = chapters;
        this.context = context;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return this.chapters.size();
    }

    @Override
    public Object getItem(int position) {
        return this.chapters.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DBReaderNovel.Chapter chapter = this.chapters.get(position);
        if(convertView == null){
            convertView = LayoutInflater.from(this.context).inflate(R.layout.view_chapter,parent,false);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DBReaderNovel.Chapter cp = (DBReaderNovel.Chapter)v.getTag(R.id.tag_chapter);
                    Log.i("Andrei", "cahp is " + cp.index + " " + cp.name);
                    ((ReaderActivity)fragment.getActivity()).changeChapter(cp);
                    fragment.getView().setVisibility(View.GONE);
                }
            });
        }
        TextView tv = (TextView) convertView.findViewById(R.id.chapter_name);
        tv.setText(chapter.name);
        convertView.setTag(R.id.tag_chapter,chapter);
        return convertView;
    }
}
