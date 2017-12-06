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

public class FetchDeltaChapterListCommand implements INovelServiceCommand {
    @Override
    public void process(Map<String, Object> args, List<IFetchNovelEngine> engines, List<IFetchNovelEngineNotify> notifies) {
        DBReaderNovel novel = (DBReaderNovel)args.get(CommandCommon.TAG_NOVEL);
        int engineID = novel.engineID;
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            ArrayList<DBReaderNovel.Chapter> chapters = new ArrayList<DBReaderNovel.Chapter>();
            int nRet = engine.fetchDeltaChapterList(novel, chapters);
            if (nRet == IFetchNovelEngine.NO_ERROR) {
                DBReaderNovel item = NovelInfoManager.getNovel(novel.name);
                item.chapters.addAll(chapters);
                item.isUpdated = 1;
                NovelInfoManager.saveDBReader(item);
            }

            if(Looper.getMainLooper() != null && notifies.size() > 0){
                final int fnRet = nRet;
                final String name = novel.name;
                final ArrayList<DBReaderNovel.Chapter> fChapters = chapters;
                final List<IFetchNovelEngineNotify> fNotifies = notifies;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < fNotifies.size(); i++) {
                            fNotifies.get(i).OnFetchDeltaChapterList(fnRet,name, fChapters);
                        }
                    }
                });
            }

        }
    }

}
