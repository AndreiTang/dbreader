package com.moss.dbreader.service.events;

import com.moss.dbreader.service.DBReaderNovel;

import java.util.ArrayList;

/**
 * Created by andrei on 2017/12/7.
 */

public class FetchDeltaChapterListEvent
{
    public FetchDeltaChapterListEvent(String novelName, int nRet, ArrayList<DBReaderNovel.Chapter> chapters) {
        this.novelName = novelName;
        this.nRet = nRet;
        this.chapters = chapters;
    }

    public String novelName;
    public int nRet;
    public ArrayList<DBReaderNovel.Chapter> chapters;
}
