package com.moss.dbreader.ui;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding2.view.RxView;
import com.moss.dbreader.fragment.events.SwitchToNovelReaderEvent;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;

import org.greenrobot.eventbus.EventBus;

import static com.bumptech.glide.request.RequestOptions.fitCenterTransform;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

/**
 * Created by tangqif on 10/23/2017.
 */

public class SearchPageAdapter extends BaseAdapter {

    private ArrayList<DBReaderNovel> novels = new ArrayList<DBReaderNovel>();
    private Fragment fragment;
   // private int engineID;
    private ArrayList<View> views = new ArrayList<View>();

    public SearchPageAdapter(Fragment fragment){
        this.fragment = fragment;
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
            view = fragment.getActivity().getLayoutInflater().inflate(R.layout.view_search,viewGroup,false);
            views.add(view);
            final View v = view;
            RxView.clicks(v).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Consumer() {
                @Override
                public void accept(Object o) throws Exception {
                    int pos = (Integer) v.getTag(R.id.tag_pos);
                    DBReaderNovel novel = novels.get(pos);
                    novel.currPage = 0;
                    DBReaderNovel item = NovelInfoManager.getNovel(novel.name);
                    if(item != null){
                        EventBus.getDefault().post(new SwitchToNovelReaderEvent(item));
                    }
                    else{
                        EventBus.getDefault().post(new SwitchToNovelReaderEvent(novel));
                    }
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
