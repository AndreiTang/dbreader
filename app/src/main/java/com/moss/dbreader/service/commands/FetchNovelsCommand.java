package com.moss.dbreader.service.commands;

import android.os.Handler;
import android.os.Looper;

import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngine;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.NovelInfoManager;
import com.moss.dbreader.service.events.FetchNovelEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * Created by andrei on 2017/12/5.
 */

public class FetchNovelsCommand implements INovelServiceCommand {
    @Override
    public void process(Map<String, Object> args, List<IFetchNovelEngine> engines) {
        int nRet = IFetchNovelEngine.ERROR_NO_RESULT;
        ArrayList<DBReaderNovel> novels = (ArrayList<DBReaderNovel>) args.get(CommandCommon.TAG_NOVEL);
        final int sessionID = (Integer)args.get(CommandCommon.TAG_SESSION_ID);
        ArrayList<DBReaderNovel> items = new ArrayList<>();
        for(int i = 0 ; i < novels.size(); i++){
            DBReaderNovel novel = novels.get(i);
            int engineID = novel.engineID;
            if (engineID > 0 || engineID < engines.size()) {
                IFetchNovelEngine engine = engines.get(engineID);
                nRet = engine.fetchNovel(novel);
                if(nRet == IFetchNovelEngine.NO_ERROR){
                    items.add(novel);
                }
            }
        }
        nRet = IFetchNovelEngine.NO_ERROR;
        if(items.size() == 0){
            nRet = IFetchNovelEngine.ERROR_NO_RESULT;
        }

        EventBus.getDefault().post(new FetchNovelEvent(nRet,sessionID,items));
    }

}
