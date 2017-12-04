package com.moss.dbreader.service;

import java.util.ArrayList;

/**
 * Created by tangqif on 2017/10/3.
 */

public interface IFetchNovelEngineNotify {
    void OnSearchNovels(final  int nRet, final int engineID, final int sessionID, final ArrayList<DBReaderNovel> novels);
    void OnFetchNovel(final  int nRet, final int sessionID, final DBReaderNovel novel);
    void OnFetchChapter(final int nRet,final int sessionID,final int index, final String cont);
    void OnCacheChapter(final int nRet,final String novelName,final int index, final String cont);
    void OnCacheChapterComplete(final String novelName);
    void OnFetchDeltaChapterList(final  int nRet,final String novelName, ArrayList<DBReaderNovel.Chapter> chapters);
}
