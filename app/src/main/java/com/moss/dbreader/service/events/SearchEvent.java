package com.moss.dbreader.service.events;

import com.moss.dbreader.service.DBReaderNovel;

import java.util.ArrayList;

/**
 * Created by andrei on 2017/12/7.
 */

public class SearchEvent {
    public int nRet;
    public int engineID;
    public int sessionID;
    public ArrayList<DBReaderNovel> novels;

    public SearchEvent(int nRet, int engineID, int sessionID, ArrayList<DBReaderNovel> novels) {
        this.nRet = nRet;
        this.engineID = engineID;
        this.sessionID = sessionID;
        this.novels = novels;
    }
}
