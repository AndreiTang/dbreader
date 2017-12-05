package com.moss.dbreader.service.commands;

import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngine;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.service.commands.INovelServiceCommand;
import com.moss.dbreader.service.commands.CommandCommon;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;



/**
 * Created by andrei on 2017/12/5.
 */

public class FetchChapterCommand implements INovelServiceCommand {
    @Override
    public void process(Map<String, Object> args, List<IFetchNovelEngine> engines, List<IFetchNovelEngineNotify> notifies) {
        int nRet = IFetchNovelEngine.ERROR_NO_RESULT;
        String cont = "";
        int engineID = (Integer)args.get(CommandCommon.TAG_ENGINE_ID);
        DBReaderNovel.Chapter chapter = (DBReaderNovel.Chapter)args.get(CommandCommon.TAG_CHAPTER);
        String novelName = (String)args.get(CommandCommon.TAG_NAME);
        int sessionID = (Integer)args.get(CommandCommon.TAG_SESSION_ID);
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            StringWriter buf = new StringWriter();
            nRet = engine.fetchChapter(chapter, buf);
            cont = buf.toString();
        }

        if (nRet == IFetchNovelEngine.NO_ERROR) {
            NovelInfoManager.saveChapterText(novelName, chapter.index, cont);
        }

        for (int i = 0; i < notifies.size(); i++) {
            notifies.get(i).OnFetchChapter(nRet, sessionID, chapter.index, cont);
        }
    }

    @Override
    public void cancel() {

    }
}
