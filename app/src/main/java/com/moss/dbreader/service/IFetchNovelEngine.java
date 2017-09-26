package com.moss.dbreader.service;

import java.util.ArrayList;

/**
 * Created by tangqif on 2017/9/23.
 */
public interface IFetchNovelEngine {
    ArrayList<DBReaderRemoteNovel> searchNovels(final String name);
    boolean fetchNovel(final DBReaderRemoteNovel remote,DBReaderLocalNovel local);
    String fetchChapter(DBReaderLocalNovel.Chapter chapter);
}
