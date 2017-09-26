package com.moss.dbreader.service;

import java.util.ArrayList;

/**
 * Created by tangqif on 2017/9/23.
 */
public class DBReaderLocalNovel extends DBReaderRemoteNovel{
    public class Chapter {
        public String name;
        public String url;
    }
    public String img;
    public ArrayList<Chapter> chapters = new ArrayList<Chapter>();
    public void  Add(final String name, final String url){
        Chapter chapter = new Chapter();
        chapter.name = name;
        chapter.url = url;
        chapters.add(chapter);
    }
}
