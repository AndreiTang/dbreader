package com.moss.dbreader.fragment.events;

import com.moss.dbreader.service.DBReaderNovel;

/**
 * Created by andrei on 2017/12/13.
 */

public class ChangeChapterEvent {
    public ChangeChapterEvent(DBReaderNovel.Chapter chapter) {
        this.chapter = chapter;
    }
    public DBReaderNovel.Chapter chapter;
}
