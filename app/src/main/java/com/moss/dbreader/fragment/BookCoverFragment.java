package com.moss.dbreader.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.ui.ChapterAdapter;
import com.moss.dbreader.ui.ChapterGroupAdapter;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static com.bumptech.glide.request.RequestOptions.fitCenterTransform;

/**
 * Created by tangqif on 11/13/2017.
 */

public class BookCoverFragment extends Fragment {

    private DBReaderNovel novel;
    private static final int MODE_CHAPTER = 1;
    private static final int MODE_CHAPTER_GROUP = 2;
    private int mode = MODE_CHAPTER;
    private int curIndex = -1;

   public void setNovel(DBReaderNovel novel){
       this.novel = novel;
   }

   public void setSelection(int curIndex){
       ListView lv = (ListView)getActivity().findViewById(R.id.book_cover_list);
       lv.setSelection(curIndex);
       this.curIndex = curIndex;
   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_bookcover,container,false);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        initializeMask();
        initializeBottomPanel();
        initializeChapters();
    }

    private void initializeChapters(){
        ImageView img = (ImageView) getActivity().findViewById(R.id.book_cover_img);
        Glide.with(this).clear(img);
        Glide.with(this).load(novel.img).apply(fitCenterTransform()).into(img);

        TextView tv = (TextView)getActivity().findViewById(R.id.book_cover_name);
        tv.setText(novel.name);

        tv = (TextView)getActivity().findViewById(R.id.book_cover_type);
        tv.setText(novel.type);
        listChapters(-1);
    }

    public void listChapters(int curIndex){
        this.curIndex = curIndex;
        this.mode = MODE_CHAPTER;
        ChapterAdapter adapter = new ChapterAdapter(this,this.novel.chapters);
        ListView lv = (ListView)getActivity().findViewById(R.id.book_cover_list);
        lv.setVerticalScrollBarEnabled(false);
        lv.setAdapter(adapter);
        if(this.curIndex != -1){
            lv.setSelection(this.curIndex);
        }
    }

    public void listChapterGroups(){
        this.mode = MODE_CHAPTER_GROUP;
        int count = novel.chapters.size();
        int groups = count/50;
        if(groups%50 !=0){
            groups++;
        }
        int index = 0;
        ArrayList<Integer> gs = new ArrayList<Integer>();
        for(int i = 0 ; i < groups; i++){
            int number = i * 50;
            gs.add(number);
            if(this.curIndex >= number && this.curIndex < number + 50){
                index = i;
            }
        }
        ChapterGroupAdapter adapter = new ChapterGroupAdapter(this,gs);
        ListView lv = (ListView)getActivity().findViewById(R.id.book_cover_list);
        lv.setVerticalScrollBarEnabled(false);
        lv.setAdapter(adapter);
        lv.setSelection(index);
    }

    private void initializeMask(){
        View mask = getActivity().findViewById(R.id.book_cover_mask);
        mask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookCoverFragment.this.getView().setVisibility(View.GONE);
            }
        });
    }

    private void initializeBottomPanel(){
        View v = getActivity().findViewById(R.id.book_cover_prev);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView lv = (ListView)getActivity().findViewById(R.id.book_cover_list);
                int begin = lv.getFirstVisiblePosition();
                int end = lv.getLastVisiblePosition();
                if(begin == 0){
                    return;
                }
                int countInPage = end - begin + 1;
                int cur = begin - countInPage;
                if(cur < 0){
                    cur = 0;
                }
                lv.setSelection(cur);
            }
        });

        v = getActivity().findViewById(R.id.book_cover_next);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView lv = (ListView)getActivity().findViewById(R.id.book_cover_list);
                int end = lv.getLastVisiblePosition();
                if(end == novel.chapters.size()-1){
                    return;
                }
                lv.setSelection(end+1);
            }
        });

        v = getActivity().findViewById(R.id.book_cover_chapter_group);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(BookCoverFragment.this.mode == MODE_CHAPTER){
                   listChapterGroups();
               }
               else{
                   listChapters(BookCoverFragment.this.curIndex);
               }
            }
        });
    }
}
