package com.moss.dbreader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import static com.moss.dbreader.service.IFetchNovelEngine.ERROR_NO_RESULT;
import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.cacheChapter;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.fetchChapter;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.fetchDeltaChapterList;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.fetchNovel;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.search;

/**
 * Created by tangqif on 2017/9/30.
 */

public class NovelEngineService extends Service {

    private static int sessionID = 0;

    private int currSessionID = -1;
    private HashMap<Integer, Integer> cacheIDs = new HashMap<Integer, Integer>();

    public class NovelEngineBinder extends Binder {
        public NovelEngine getNovelEngine() {
            return novelEngine;
        }
    }

    public class NovelEngine {

        public int generateSessionID() {
            sessionID++;
            return sessionID;
        }

        public void loadNovels(){
                    
        }

        public void searchNovel(final String name, int sessionID) {
            NovelEngineCommand cmd = new NovelEngineCommand();
            cmd.type = search;
            cmd.sessionID = sessionID;
            cmd.pars.add(name);
            commands.add(cmd);
            initialize();
        }

        public void fetchNovel(final DBReaderNovel novel, int engineID, int sessionID) {
            NovelEngineCommand cmd = new NovelEngineCommand();
            cmd.type = fetchNovel;
            cmd.engineCode = engineID;
            cmd.sessionID = sessionID;
            cmd.pars.add(novel);
            commands.add(cmd);
            initialize();
        }


        public void fetchChapter(String novelName,DBReaderNovel.Chapter chapter, int engineID, int sessionID) {
            NovelEngineCommand cmd = new NovelEngineCommand();
            cmd.type = fetchChapter;
            cmd.engineCode = engineID;
            cmd.sessionID = sessionID;
            cmd.pars.add(novelName);
            cmd.pars.add(chapter);
            commands.add(cmd);
            initialize();
        }

        public void cacheChapters(DBReaderNovel novel, int engineID) {
            NovelEngineCommand cmd = new NovelEngineCommand();
            cmd.type = cacheChapter;
            cmd.engineCode = engineID;
            cmd.pars.add(novel);
            NovelEngineService.this.cacheCommands.add(cmd);
            initializeCache();
        }

        public void fetchDeltaChapterList(final DBReaderNovel novel, int engineID) {
            NovelEngineCommand cmd = new NovelEngineCommand();
            cmd.type = fetchDeltaChapterList;
            cmd.engineCode = engineID;
            cmd.pars.add(novel);
            NovelEngineService.this.cacheCommands.add(cmd);
            initializeCache();
        }

        public void cancel(int sessionID) {
            NovelEngineService.this.cancel(sessionID);
        }

        public void addNotify(IFetchNovelEngineNotify notify) {
            if (notifies.contains(notify) == true) {
                return;
            }
            notifies.add(notify);
        }

        public void removeNotify(IFetchNovelEngineNotify notify) {
            notifies.remove(notify);
        }

    }

    private NovelEngine novelEngine = new NovelEngine();
    private Binder binder = new NovelEngineBinder();
    private ArrayList<IFetchNovelEngine> engines = new ArrayList<IFetchNovelEngine>();
    private ArrayList<NovelEngineCommand> commands = new ArrayList<NovelEngineCommand>();
    private ArrayList<NovelEngineCommand> cacheCommands = new ArrayList<NovelEngineCommand>();
    private ArrayList<IFetchNovelEngineNotify> notifies = new ArrayList<IFetchNovelEngineNotify>();
    private Thread thrd = null;
    private Thread thrdCache = null;

    @Override
    public void onDestroy() {
        if (this.thrd != null) {
            thrd.interrupt();
            this.thrd = null;
        }

        if (this.thrdCache != null) {
            this.thrdCache.interrupt();
            this.thrdCache = null;
        }
        super.onDestroy();
    }


    public NovelEngineService() {
        engines.add(new PiaoTianNovel());
    }


    private void initialize() {
        if (this.thrd != null) {
            return;
        }
        this.thrd = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 1000;
                    while (!Thread.currentThread().isInterrupted()) {
                        proc();
                        Thread.sleep(100);
                        if (commands.size() == 0) {
                            i--;
                        } else {
                            i = 1000;
                        }
                        if (i <= 0) {
                            break;
                        }
                    }

                } catch (Exception e) {
                    String err = e.getMessage();
                }
                NovelEngineService.this.thrd = null;
                Log.i("Andrei", "Service thread exit");
            }
        });
        this.thrd.start();
    }

    private void initializeCache() {
        if (this.thrdCache != null) {
            return;
        }
        this.thrdCache = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 100;
                    while (!Thread.currentThread().isInterrupted()) {
                        procCache();
                        Thread.sleep(1000);
                        if (cacheCommands.size() == 0) {
                            i--;
                        } else {
                            i = 100;
                        }
                        if (i <= 0) {
                            break;
                        }
                    }

                } catch (Exception e) {
                    String err = e.getMessage();
                }
                NovelEngineService.this.thrdCache = null;
                Log.i("Andrei", "Cache Service thread exit");
            }
        });
        this.thrdCache.start();
    }

    private void cancel(int sessionID) {
        synchronized (this) {
            int i = commands.size() - 1;
            for (; i >= 0; i--) {
                NovelEngineCommand item = commands.get(i);
                if (item.sessionID == sessionID) {
                    commands.remove(i);
                }
            }
            if (sessionID == this.currSessionID && this.currSessionID != -1) {
                for (i = 0; i < engines.size(); i++) {
                    IFetchNovelEngine engine = engines.get(i);
                    engine.cancel();
                }
            }
        }
    }

    private void procCache() {
        NovelEngineCommand cmd = null;
        synchronized (this) {
            if (cacheCommands.size() == 0) {
                return;
            }
            cmd = cacheCommands.remove(0);
            NovelEngineCommand.CommandType type = cmd.type;
            switch (type) {
                case cacheChapter:
                    procCacheChapter((DBReaderNovel) cmd.pars.get(0), cmd.engineCode);
                    break;
                case fetchDeltaChapterList:
                    procFetchDeltaChapterList((DBReaderNovel) cmd.pars.get(0), cmd.engineCode);
                    break;
                default:
                    break;
            }

        }
    }

    private void proc() {
        NovelEngineCommand cmd = null;
        NovelEngineCommand.CommandType type = search;
        synchronized (this) {
            if (commands.size() == 0) {
                return;
            }
            cmd = commands.remove(0);
            type = cmd.type;
        }
        this.currSessionID = cmd.sessionID;
        switch (type) {
            case search:
                procSearch((String) cmd.pars.get(0), cmd.sessionID);
                break;
            case fetchNovel:
                procFetchNovel((DBReaderNovel) cmd.pars.get(0), cmd.engineCode, cmd.sessionID);
                break;
            case fetchChapter:
                procFetchChapter((String)cmd.pars.get(0),(DBReaderNovel.Chapter) cmd.pars.get(1), cmd.engineCode, cmd.sessionID);
                break;
            default:
                break;
        }
        this.currSessionID = -1;
    }

    private void procSearch(final String name, int sessionID) {
        Integer engineID = 0;
        ArrayList<DBReaderNovel> novels = new ArrayList<DBReaderNovel>();
        int nRet = searchNovels(name, engineID, novels);
        for (int i = 0; i < notifies.size(); i++) {
            notifies.get(i).OnSearchNovels(nRet, engineID, sessionID, novels);
        }
    }

    private int searchNovels(final String name, Integer engineID, ArrayList<DBReaderNovel> novels) {
        for (int i = 0; i < engines.size(); i++) {
            IFetchNovelEngine engine = engines.get(i);
            int nRet = engine.searchNovels(name, novels);
            if (nRet == NO_ERROR) {
                engineID = i;
                return NO_ERROR;
            }
        }
        return ERROR_NO_RESULT;
    }

    private void procFetchNovel(DBReaderNovel novel, int engineID, int sessionID) {
        int nRet = ERROR_NO_RESULT;
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            nRet = engine.fetchNovel(novel);
        }
        if(nRet == NO_ERROR){
            NovelInfoManager.saveDBReader(novel);
        }
        for (int i = 0; i < notifies.size(); i++) {
            notifies.get(i).OnFetchNovel(nRet, sessionID, novel);
        }
    }

    private void procFetchChapter(final String novelName,final DBReaderNovel.Chapter chapter, int engineID, int sessionID) {
        int nRet = ERROR_NO_RESULT;
        String cont = "";
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            StringWriter buf = new StringWriter();
            nRet = engine.fetchChapter(chapter, buf);
            cont = buf.toString();
        }

        if(nRet == NO_ERROR){
            NovelInfoManager.saveChapterText(novelName,chapter.index,cont);
        }

        for (int i = 0; i < notifies.size(); i++) {
            notifies.get(i).OnFetchChapter(nRet, sessionID, chapter.index, cont);
        }
    }

    private void procCacheChapter(DBReaderNovel novel, int engineID) {
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            for(int i = 0 ; i < novel.chapters.size(); i++){
                DBReaderNovel.Chapter chapter = novel.chapters.get(i);
                boolean bRet = NovelInfoManager.isChapterExist(novel.name,chapter.index);
                if(bRet == true){
                    continue;
                }
                StringWriter buf = new StringWriter();
                int nRet = engine.fetchChapter(chapter, buf);
                String cont = buf.toString();
                if(nRet == NO_ERROR){
                    NovelInfoManager.saveChapterText(novel.name,chapter.index,cont);
                }
            }
        }
        for (int i = 0; i < notifies.size(); i++) {
            notifies.get(i).OnCacheChapterComplete(novel.name);
        }
    }

    private void procFetchDeltaChapterList(DBReaderNovel novel, int engineID) {
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            ArrayList<DBReaderNovel.Chapter> chapters = new ArrayList<DBReaderNovel.Chapter>();
            int nRet = engine.fetchDeltaChapterList(novel, chapters);
            if(nRet == NO_ERROR){
                DBReaderNovel item = NovelInfoManager.getNovel(novel.name);
                item.chapters.addAll(chapters);
                item.isUpdated = 1;
                NovelInfoManager.saveDBReader(item);
            }
            for (int i = 0; i < notifies.size(); i++) {
                notifies.get(i).OnFetchDeltaChapterList(nRet, novel.name, chapters);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
