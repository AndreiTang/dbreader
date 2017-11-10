package com.moss.dbreader.service;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by tangqif on 2017/9/23.
 */
public class DBReaderNovel implements Serializable{


    private static final long serialVersionUID = 1177827488392242691L;

    public class Chapter implements Serializable{
        private static final long serialVersionUID = 1742127615231658991L;
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
    public int isInCase = 0;
    public int engineID = 0;
    public int currPage = 0;

    public void  Add(final String name, final String url){
        Chapter chapter = new Chapter();
        chapter.name = name;
        chapter.url = url;
        chapter.index = chapters.size() ;
        chapters.add(chapter);
    }
}
