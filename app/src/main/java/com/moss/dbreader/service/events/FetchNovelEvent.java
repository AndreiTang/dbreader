package com.moss.dbreader.service.events;

import com.moss.dbreader.service.DBReaderNovel;

/**
 * Created by andrei on 2017/12/7.
 */

public class FetchNovelEvent {
    public int nRet;
    public int sessionID;
    public DBReaderNovel novel;

    public FetchNovelEvent(int nRet, int sessionID, DBReaderNovel novel) {
        this.nRet = nRet;
        this.sessionID = sessionID;
        this.novel = novel;
    }
}
