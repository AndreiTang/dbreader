package com.moss.dbreader.ui;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.MainActivity;
import com.moss.dbreader.R;
import com.moss.dbreader.service.DBReaderNovel;

import java.util.ArrayList;

import static com.bumptech.glide.request.RequestOptions.fitCenterTransform;

/**
 * Created by tangqif on 11/10/2017.
 */

public class CasePageAdapter extends BaseAdapter {

    private Fragment fragment;
    private ArrayList<DBReaderNovel> novels;
    public static final int MODE_NORMAL = 1;
    public static final int MODE_REMOVE = 2;
    private int mode = MODE_NORMAL;
    private ArrayList<Integer> ids = new ArrayList<Integer>();


    public CasePageAdapter(Fragment fragment){
        this.fragment = fragment;
        this.novels = NovelInfoManager.fetchNovelsInBookCase();
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
            view = this.fragment.getActivity().getLayoutInflater().inflate(R.layout.view_case,parent,false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag(R.id.tag_pos);
                    DBReaderNovel novel = novels.get(pos);
                    if(CasePageAdapter.this.mode == MODE_NORMAL){
                        novel.isUpdated = 0;
                        ((MainActivity)fragment.getActivity()).switchToNovelReader(novel);
                    }
                    else{
                        View rm = v.findViewById(R.id.case_novel_remove);
                        if(rm.getVisibility() == View.GONE){
                            rm.setVisibility(View.VISIBLE);
                            CasePageAdapter.this.ids.add(pos);
                        }
                        else{
                            rm.setVisibility(View.GONE);
                            Integer item = pos;
                            CasePageAdapter.this.ids.remove(item);
                        }
                    }

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

        View flag = view.findViewById(R.id.case_novel_update);
        if(novel.isUpdated == 1){
            flag.setVisibility(View.VISIBLE);
        }
        else{
            flag.setVisibility(View.GONE);
        }

        if(this.ids.contains(position) == true){
            view.findViewById(R.id.case_novel_remove).setVisibility(View.VISIBLE);
        }
        else{
            view.findViewById(R.id.case_novel_remove).setVisibility(View.GONE);
        }

        return view;
    }

    public int getMode(){
        return this.mode;
    }

    public void setMode(int mode){
        this.mode = mode;
        if(this.mode == MODE_NORMAL){
            removeSelectNovels();
        }
    }

    private void removeSelectNovels(){
        if(this.ids.size() == 0){
            return;
        }
        for(int i = 0; i < this.ids.size(); i++){
            int pos = this.ids.get(i);
            DBReaderNovel novel = this.novels.get(pos);
            novel.isInCase = 0;
            novel.currChapter = 0;
            novel.currPage = 0;
            NovelInfoManager.saveDBReader(novel);
            NovelInfoManager.removeReaderPages(novel.name);
            this.novels.remove(pos);
        }
        this.ids.clear();
        notifyDataSetChanged();
    }
}
