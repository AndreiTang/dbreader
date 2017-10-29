package com.moss.dbreader.service;

import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Created by tangqif on 2017/9/23.
 */
public interface IFetchNovelEngine {
    boolean searchNovels(final String name,ArrayList<DBReaderNovel> novels);
    boolean fetchNovel(DBReaderNovel novel);
    boolean fetchChapter(final DBReaderNovel.Chapter chapter, StringWriter cont);
    void cancel();
}
