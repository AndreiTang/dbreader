package com.moss.dbreader.service.commands;

import android.os.Handler;
import android.os.Looper;

import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngine;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelInfoManager;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by andrei on 2017/12/5.
 */

public class CacheChaptersCommand implements INovelServiceCommand {
    @Override
    public void process(Map<String, Object> args, List<IFetchNovelEngine> engines, List<IFetchNovelEngineNotify> notifies) {
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

        if (Looper.getMainLooper() != null && notifies.size() > 0) {
            final String name =novel.name;
            final List<IFetchNovelEngineNotify> fNotifies = notifies;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < fNotifies.size(); i++) {
                        fNotifies.get(i).OnCacheChapterComplete(name);;
                    }
                }
            });
        }

    }

}
