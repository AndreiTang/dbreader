package com.moss.dbreader.service;

import java.util.ArrayList;

/**
 * Created by tangqif on 2017/10/3.
 */

public interface IFetchNovelEngineNotify {
    void OnSearchNovels(final  boolean bRet, final int engineID, final int sessionID, final ArrayList<DBReaderNovel> novels);
    void OnFetchNovel(final  boolean bRet, final int engineID, final int sessionID, final DBReaderNovel novel);
    void OnFetchChapter(final boolean bRet,final int sessionID,final int index, final String cont);
}
