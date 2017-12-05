package com.moss.dbreader.service.commands;

import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngine;
import com.moss.dbreader.service.IFetchNovelEngineNotify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by andrei on 2017/12/5.
 */

public class SearchCommand implements INovelServiceCommand {
    IFetchNovelEngine engine = null;
    @Override
    public void process(Map<String, Object> args, List<IFetchNovelEngine> engines, List<IFetchNovelEngineNotify> notifies) {
        int nRet = IFetchNovelEngine.ERROR_NO_RESULT;
        String name = (String)args.get(CommandCommon.TAG_NAME);
        int sessionID = (Integer)args.get(CommandCommon.TAG_SESSION_ID);
        int engineID = 0;
        ArrayList<DBReaderNovel> novels = new ArrayList<DBReaderNovel>();
        for (int i = 0; i < engines.size(); i++) {
            IFetchNovelEngine engine = engines.get(i);
            this.engine = engine;
            nRet = engine.searchNovels(name, novels);
            if (nRet == IFetchNovelEngine.NO_ERROR) {
                engineID = i;
                break;
            }
        }

        for(int i = 0 ; i < novels.size(); i++){
            DBReaderNovel item = novels.get(i);
            item.engineID = engineID;
        }

        for (int i = 0; i < notifies.size(); i++) {
            notifies.get(i).OnSearchNovels(nRet, engineID,sessionID,novels);
        }
    }

    @Override
    public void cancel() {

    }

}
