package com.moss.dbreader.service.commands;

import android.os.Handler;
import android.os.Looper;

import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngine;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.service.events.FetchChapterEvent;
import com.moss.dbreader.service.events.FetchDeltaChapterListEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by andrei on 2017/12/5.
 */

public class FetchDeltaChapterListCommand implements INovelServiceCommand {
    @Override
    public void process(Map<String, Object> args, List<IFetchNovelEngine> engines) {
        DBReaderNovel novel = (DBReaderNovel)args.get(CommandCommon.TAG_NOVEL);
        int engineID = novel.engineID;
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            ArrayList<DBReaderNovel.Chapter> chapters = new ArrayList<DBReaderNovel.Chapter>();
            int nRet = engine.fetchDeltaChapterList(novel, chapters);
            if (nRet == IFetchNovelEngine.NO_ERROR) {
                DBReaderNovel item = NovelInfoManager.getNovel(novel.name);
                item.chapters.addAll(chapters);
                NovelInfoManager.saveDBReader(item);
            }

            EventBus.getDefault().post(new FetchDeltaChapterListEvent(novel.name,nRet,chapters));
        }
    }

}
