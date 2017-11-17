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

import org.w3c.dom.Text;

import static com.bumptech.glide.request.RequestOptions.fitCenterTransform;

/**
 * Created by tangqif on 11/13/2017.
 */

public class BookCoverFragment extends Fragment {

    private DBReaderNovel novel;

   public void setNovel(DBReaderNovel novel){
       this.novel = novel;

       ImageView img = (ImageView) getActivity().findViewById(R.id.book_cover_img);
       Glide.with(this).clear(img);
       Glide.with(this).load(novel.img).apply(fitCenterTransform()).into(img);

       TextView tv = (TextView)getActivity().findViewById(R.id.book_cover_name);
       tv.setText(novel.name);

       tv = (TextView)getActivity().findViewById(R.id.book_cover_type);
       tv.setText(novel.type);


       ChapterAdapter adapter = new ChapterAdapter(getContext(),this,this.novel.chapters);
       ListView lv = (ListView)getActivity().findViewById(R.id.book_cover_list);
       lv.setVerticalScrollBarEnabled(false);
       lv.setAdapter(adapter);
   }

   public void setSelection(int curIndex){
       ListView lv = (ListView)getActivity().findViewById(R.id.book_cover_list);
       lv.setSelection(curIndex);
   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_bookcover,container,false);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        View mask = getActivity().findViewById(R.id.book_cover_mask);
        mask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookCoverFragment.this.getView().setVisibility(View.GONE);
            }
        });
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
    }
}
