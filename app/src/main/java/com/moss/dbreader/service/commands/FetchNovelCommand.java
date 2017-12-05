package com.moss.dbreader.service.commands;

import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngine;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelInfoManager;

import java.util.List;
import java.util.Map;



/**
 * Created by andrei on 2017/12/5.
 */

public class FetchNovelCommand implements INovelServiceCommand {
    @Override
    public void process(Map<String, Object> args, List<IFetchNovelEngine> engines, List<IFetchNovelEngineNotify> notifies) {
        int nRet = IFetchNovelEngine.ERROR_NO_RESULT;
        DBReaderNovel novel = (DBReaderNovel) args.get(CommandCommon.TAG_NOVEL);
        int sessionID = (Integer)args.get(CommandCommon.TAG_SESSION_ID);
        int engineID = novel.engineID;
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            nRet = engine.fetchNovel(novel);
        }
        if (nRet == IFetchNovelEngine.NO_ERROR) {
            NovelInfoManager.add(novel,false);
            NovelInfoManager.saveDBReader(novel);
        }
        for (int i = 0; i < notifies.size(); i++) {
            notifies.get(i).OnFetchNovel(nRet, sessionID, novel);
        }
    }

    @Override
    public void cancel() {

    }
}
