package com.moss.dbreader.service.events;

import com.moss.dbreader.service.DBReaderNovel;

import java.util.ArrayList;

/**
 * Created by andrei on 2017/12/7.
 */

public class FetchNovelEvent {
    public int nRet;
    public int sessionID;
    public ArrayList<DBReaderNovel> novels;

    public FetchNovelEvent(int nRet, int sessionID, ArrayList<DBReaderNovel> novels) {
        this.nRet = nRet;
        this.sessionID = sessionID;
        this.novels = novels;
    }
}
