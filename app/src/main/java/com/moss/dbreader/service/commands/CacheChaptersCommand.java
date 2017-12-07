package com.moss.dbreader.service.commands;

import android.os.Handler;
import android.os.Looper;

import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngine;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.service.events.CacheChaptersEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by andrei on 2017/12/5.
 */

public class CacheChaptersCommand implements INovelServiceCommand {
    @Override
    public void process(Map<String, Object> args, List<IFetchNovelEngine> engines) {
        DBReaderNovel novel = (DBReaderNovel) args.get(CommandCommon.TAG_NOVEL);
        int engineID = novel.engineID;
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            for (int i = 0; i < novel.chapters.size(); i++) {
                DBReaderNovel.Chapter chapter = novel.chapters.get(i);
                boolean bRet = NovelInfoManager.isChapterExist(novel.name, chapter.index);
                if (bRet == true) {
                    continue;
                }
                StringWriter buf = new StringWriter();
                int nRet = engine.fetchChapter(chapter, buf);
                String cont = buf.toString();
                if (nRet == IFetchNovelEngine.NO_ERROR) {
                    NovelInfoManager.saveChapterText(novel.name, chapter.index, cont);
                }
            }
        }
        EventBus.getDefault().post(new CacheChaptersEvent(novel.name));
    }
}
