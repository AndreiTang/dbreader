package com.moss.dbreader.service.commands;

import android.os.Handler;
import android.os.Looper;

import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngine;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelInfoManager;

import java.util.ArrayList;
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
        final int sessionID = (Integer)args.get(CommandCommon.TAG_SESSION_ID);
        int engineID = novel.engineID;
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            nRet = engine.fetchNovel(novel);
        }

        if(Looper.getMainLooper() != null && notifies.size() > 0){
            final int fnRet = nRet;
            final DBReaderNovel fNovel = novel;
            final List<IFetchNovelEngineNotify> fNotifies = notifies;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < fNotifies.size(); i++) {
                        fNotifies.get(i).OnFetchNovel(fnRet,sessionID, fNovel);
                    }
                }
            });
        }
    }

}
