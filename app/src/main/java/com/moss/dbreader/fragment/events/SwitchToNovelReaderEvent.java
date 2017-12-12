package com.moss.dbreader.fragment.events;

import com.moss.dbreader.service.DBReaderNovel;

/**
 * Created by andrei on 2017/12/12.
 */

public class SwitchToNovelReaderEvent {
    public SwitchToNovelReaderEvent(DBReaderNovel novel) {
        this.novel = novel;
    }
    public DBReaderNovel novel;
}
