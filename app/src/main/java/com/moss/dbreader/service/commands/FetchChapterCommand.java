package com.moss.dbreader.service.commands;

import android.os.Handler;
import android.os.Looper;

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
        DBReaderNovel novel = (DBReaderNovel) args.get(CommandCommon.TAG_NOVEL);
        final int engineID = novel.engineID;
        DBReaderNovel.Chapter chapter = (DBReaderNovel.Chapter)args.get(CommandCommon.TAG_CHAPTER);
        String novelName = novel.name;
        final int sessionID = (Integer)args.get(CommandCommon.TAG_SESSION_ID);

        cont = NovelInfoManager.getChapterText(novelName,chapter.index);
        if(cont.isEmpty()){
            if (engineID > 0 || engineID < engines.size()) {
                IFetchNovelEngine engine = engines.get(engineID);
                StringWriter buf = new StringWriter();
                nRet = engine.fetchChapter(chapter, buf);
                cont = buf.toString();
                if (nRet == IFetchNovelEngine.NO_ERROR) {
                    NovelInfoManager.saveChapterText(novelName, chapter.index, cont);
                }
            }
        }
        else{
            nRet = IFetchNovelEngine.NO_ERROR;
        }

        if(Looper.getMainLooper() != null && notifies.size() > 0){
            final int fnRet = nRet;
            final int fIndex = chapter.index;
            final String fCont = cont;
            final List<IFetchNovelEngineNotify> fNotifies = notifies;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < fNotifies.size(); i++) {
                        fNotifies.get(i).OnFetchChapter(fnRet, sessionID, fIndex, fCont);
                    }
                }
            });
        }


    }

}
