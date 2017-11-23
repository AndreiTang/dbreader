package com.moss.dbreader.service;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by tangqif on 2017/9/23.
 */
public class DBReaderNovel implements Serializable, Comparable{


    private static final long serialVersionUID = 1177827488392242691L;

    @Override
    public int compareTo(@NonNull Object o) {
        DBReaderNovel obj = (DBReaderNovel)o;
        if(this.updateTime > obj.updateTime){
            return -1;
        }
        return 1;
    }

    public class Chapter implements Serializable{
        private static final long serialVersionUID = 1742127615231658991L;
        public String name;
        public String url;
        public int index;
    }

    @Expose
    public ArrayList<Chapter> chapters = new ArrayList<Chapter>();
    @Expose
    public String name;
    @Expose
    public String url;
    @Expose
    public String author;
    @Expose
    public String type;
    @Expose
    public String updateDate;
    @Expose
    public String img;
    @Expose
    public String decs;
    @Expose
    public int isInCase = 0;
    @Expose
    public int engineID = 0;
    @Expose
    public int currPage = 0;
    @Expose
    public long updateTime = 0;
    public int isUpdated = 0;

    public void  Add(final String name, final String url){
        Chapter chapter = new Chapter();
        chapter.name = name;
        chapter.url = url;
        chapter.index = chapters.size() ;
        chapters.add(chapter);
    }
}
