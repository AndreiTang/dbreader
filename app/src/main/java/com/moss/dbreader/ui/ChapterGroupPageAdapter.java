package com.moss.dbreader.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.moss.dbreader.R;
import com.moss.dbreader.fragment.BookCoverFragment;

import java.util.ArrayList;

/**
 * Created by tangqif on 11/25/2017.
 */

public class ChapterGroupPageAdapter extends BaseAdapter {

    BookCoverFragment bookCoverFragment = null;
    ArrayList<Integer> groups = null;

    public ChapterGroupPageAdapter(BookCoverFragment bookCoverFragment, ArrayList<Integer> groups){
        this.bookCoverFragment = bookCoverFragment;
        this.groups = groups;
    }
    @Override
    public int getCount() {
        return groups.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = this.bookCoverFragment.getActivity().getLayoutInflater().inflate(R.layout.view_chapter,parent,false);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = (Integer) v.getTag(R.id.tag_chap_index);
                    ChapterGroupPageAdapter.this.bookCoverFragment.listChapters(index);
                }
            });
        }
        int begin = this.groups.get(position) + 1;

        int end = begin + 50;
        String msg = this.bookCoverFragment.getString(R.string.book_chapter);
        msg  = String.format(msg,begin,end);
        TextView tv = (TextView) convertView.findViewById(R.id.chapter_name);
        tv.setText(msg);
        convertView.setTag(R.id.tag_chap_index,begin-1);
        return convertView;
    }
}
