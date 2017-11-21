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
import java.util.LinkedList;
import java.util.Queue;

import static com.moss.dbreader.service.IFetchNovelEngine.ERROR_NO_RESULT;
import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.cacheChapter;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.fetchChapter;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.fetchNovel;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.search;

/**
 * Created by tangqif on 2017/9/30.
 */

public class NovelEngineService extends Service {

    private static int sessionID = 0;

    private int currSessionID = -1;
    private HashMap<Integer,Integer> cacheIDs = new HashMap<Integer, Integer>();

    public class NovelEngineBinder extends Binder {
        public NovelEngine getNovelEngine() {
            return novelEngine;
        }
    }

    public class NovelEngine {

        public int generateSessionID(){
            sessionID++;
            return sessionID;
        }

        public void searchNovel(final String name, int sessionID) {
            NovelEngineCommand cmd = new NovelEngineCommand();
            cmd.type = search;
            cmd.sessionID = sessionID;
            cmd.pars.add(name);
            commands.add(cmd);
            initialize();
        }

        public void fetchNovel(final DBReaderNovel novel,int engineID, int sessionID ){
            NovelEngineCommand cmd = new NovelEngineCommand();
            cmd.type = fetchNovel;
            cmd.engineCode = engineID;
            cmd.sessionID = sessionID;
            cmd.pars.add(novel);
            commands.add(cmd);
            initialize();
        }


        public void fetchChapter(DBReaderNovel.Chapter chapter, int engineID, int sessionID) {
            NovelEngineCommand cmd = new NovelEngineCommand();
            cmd.type = fetchChapter;
            cmd.engineCode = engineID;
            cmd.sessionID = sessionID;
            cmd.pars.add(chapter);
            commands.add(cmd);
            initialize();
        }

        public void cacheChapters(String novelName, ArrayList<DBReaderNovel.Chapter> chapters, int engineID){
            if(chapters.size() == 0){
                return;
            }
            int sid = generateSessionID();
            NovelEngineService.this.cacheIDs.put(sid,chapters.size());
            Log.i("Andrei","Service cache " + novelName + " " + chapters.size());
            for(int i = 0 ; i < chapters.size(); i++){
                DBReaderNovel.Chapter chapter = chapters.get(i);
                NovelEngineCommand cmd = new NovelEngineCommand();
                cmd.type = cacheChapter;
                cmd.engineCode = engineID;
                cmd.sessionID = sid;
                cmd.pars.add(chapter);
                cmd.pars.add(novelName);
                NovelEngineService.this.cacheCommands.add(cmd);
            }
            initializeCache();
        }

        public void cancel(int sessionID){
            NovelEngineService.this.cancel(sessionID);
        }

        public void addNotify(IFetchNovelEngineNotify notify) {
            notifies.add(notify);
        }

        public void removeNotify(IFetchNovelEngineNotify notify){
            notifies.remove(notify);
        }

        public int fetchChapterDirectly(DBReaderNovel novel,int engineID){
            int nRet = ERROR_NO_RESULT;
            if (engineID > 0 || engineID < engines.size()) {
                IFetchNovelEngine engine = engines.get(engineID);
                nRet = engine.fetchNovel(novel);
            }
            return nRet;
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
        if(this.thrd != null){
            thrd.interrupt();
            this.thrd = null;
        }

        if(this.thrdCache != null){
            this.thrdCache.interrupt();
            this.thrdCache = null;
        }
        super.onDestroy();
    }


    public NovelEngineService() {
        engines.add(new PiaoTianNovel());
    }


    private void initialize() {
        if(this.thrd != null){
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
                        if(commands.size() == 0){
                            i--;
                        }
                        else{
                            i = 1000;
                        }
                        if(i <= 0){
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

    private void initializeCache(){
        if(this.thrdCache != null){
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
                        if(cacheCommands.size() == 0){
                            i--;
                        }
                        else{
                            i = 100;
                        }
                        if(i <= 0){
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

    private void cancel(int sessionID){
        synchronized (this){
            int i = commands.size() - 1;
            for(; i >= 0 ; i--){
                NovelEngineCommand item = commands.get(i);
                if(item.sessionID == sessionID){
                    commands.remove(i);
                }
            }
            if(sessionID == this.currSessionID && this.currSessionID !=-1){
                for(i = 0 ; i < engines.size(); i++){
                    IFetchNovelEngine engine = engines.get(i);
                    engine.cancel();
                }
            }
        }
    }

    private void procCache(){
        NovelEngineCommand cmd = null;
        synchronized (this){
            if (cacheCommands.size() == 0) {
                return;
            }
            cmd = cacheCommands.remove(0);
            procCacheChapter((String)cmd.pars.get(1),(DBReaderNovel.Chapter) cmd.pars.get(0), cmd.engineCode, cmd.sessionID);
        }
    }

    private void proc() {
        NovelEngineCommand cmd = null;
        NovelEngineCommand.CommandType type = search;
        synchronized (this){
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
                procFetchNovel((DBReaderNovel)cmd.pars.get(0),cmd.engineCode,cmd.sessionID);
                break;
            case fetchChapter:
                procFetchChapter((DBReaderNovel.Chapter) cmd.pars.get(0), cmd.engineCode, cmd.sessionID);
                break;
            case cacheChapter:
                procCacheChapter((String)cmd.pars.get(1),(DBReaderNovel.Chapter) cmd.pars.get(0), cmd.engineCode, cmd.sessionID);
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

    private void procFetchNovel(DBReaderNovel novel, int engineID, int sessionID){
        int nRet = ERROR_NO_RESULT;
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            nRet = engine.fetchNovel(novel);
        }
        for (int i = 0; i < notifies.size(); i++) {
            notifies.get(i).OnFetchNovel(nRet, sessionID,novel);
        }
    }

    private void procFetchChapter(final DBReaderNovel.Chapter chapter, int engineID, int sessionID) {
        int nRet = ERROR_NO_RESULT;
        String cont = "";
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            StringWriter buf = new StringWriter();
            nRet = engine.fetchChapter(chapter, buf);
            cont = buf.toString();
        }

        for (int i = 0; i < notifies.size(); i++) {
            notifies.get(i).OnFetchChapter(nRet, sessionID, chapter.index,cont);
        }
    }

    private void procCacheChapter(final String novelName, final DBReaderNovel.Chapter chapter, int engineID, int sessionID){
        int nRet = ERROR_NO_RESULT;
        String cont = "";
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            StringWriter buf = new StringWriter();
            nRet = engine.fetchChapter(chapter, buf);
            cont = buf.toString();
            for (int i = 0; i < notifies.size(); i++) {
                notifies.get(i).OnCacheChapter(nRet,novelName, chapter.index,cont);
            }
        }
        int count = this.cacheIDs.get(sessionID);
        count--;
        if(count == 0){
            this.cacheIDs.remove(sessionID);
            for (int i = 0; i < notifies.size(); i++) {
                notifies.get(i).OnCacheChapterComplete(novelName);
            }
        }
        else{
            this.cacheIDs.put(sessionID,count);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
