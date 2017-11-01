package com.moss.dbreader.service;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by tangqif on 2017/9/23.
 */
public class DBReaderNovel implements Serializable{

    public class Chapter {
        public String name;
        public String url;
        public int index;
    }

    public ArrayList<Chapter> chapters = new ArrayList<Chapter>();
    public String name;
    public String url;
    public String author;
    public String type;
    public String updateDate;
    public String img;
    public String decs;

    public void  Add(final String name, final String url){
        Chapter chapter = new Chapter();
        chapter.name = name;
        chapter.url = url;
        chapter.index = chapters.size() ;
        chapters.add(chapter);
    }
}
