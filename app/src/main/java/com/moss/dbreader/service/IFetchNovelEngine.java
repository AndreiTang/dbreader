package com.moss.dbreader.service;

import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Created by tangqif on 2017/9/23.
 */
public interface IFetchNovelEngine {
    int searchNovels(final String name,ArrayList<DBReaderNovel> novels);
    int fetchNovel(DBReaderNovel novel);
    int fetchDeltaChapterList(DBReaderNovel novel, ArrayList<DBReaderNovel.Chapter> deltaList);
    int fetchChapter(final DBReaderNovel.Chapter chapter, StringWriter cont);

    final int NO_ERROR = 0;
    final int ERROR_NETWORK = 1;
    final int ERROR_NO_RESULT = 2;
    final int ERROR_TOO_MANY = 3;
}
