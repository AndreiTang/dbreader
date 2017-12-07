package com.moss.dbreader.service.commands;

import android.os.Handler;
import android.os.Looper;

import com.moss.dbreader.service.DBReaderNovel;
import com.moss.dbreader.service.IFetchNovelEngine;
import com.moss.dbreader.service.IFetchNovelEngineNotify;
import com.moss.dbreader.service.events.SearchEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by andrei on 2017/12/5.
 */

public class SearchCommand implements INovelServiceCommand {
    @Override
    public void process(Map<String, Object> args, List<IFetchNovelEngine> engines) {
        int nRet = IFetchNovelEngine.ERROR_NO_RESULT;
        String name = (String)args.get(CommandCommon.TAG_NAME);
        final int sessionID = (Integer)args.get(CommandCommon.TAG_SESSION_ID);
        int engineID = 0;
        ArrayList<DBReaderNovel> novels = new ArrayList<DBReaderNovel>();
        for (int i = 0; i < engines.size(); i++) {
            IFetchNovelEngine engine = engines.get(i);
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

        EventBus.getDefault().post(new SearchEvent(nRet,engineID,sessionID,novels));
    }

}
