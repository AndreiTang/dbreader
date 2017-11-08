package com.moss.dbreader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import static com.moss.dbreader.service.IFetchNovelEngine.ERROR_NO_RESULT;
import static com.moss.dbreader.service.IFetchNovelEngine.NO_ERROR;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.fetchChapter;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.fetchNovel;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.search;

/**
 * Created by tangqif on 2017/9/30.
 */

public class NovelEngineService extends Service {

    private static int sessionID = 0;

    private int currSessionID = -1;

    public class NovelEngineBinder extends Binder {
        public NovelEngine getNovelEngine() {
            return novelEngine;
        }
    }

    public class NovelEngine {

        public int generateSessionID(){
            return sessionID++;
        }

        public void searchNovel(final String name, int sessionID) {
            NovelEngineCommand cmd = new NovelEngineCommand();
            cmd.type = search;
            cmd.sessionID = sessionID;
            cmd.pars.add(name);
            commands.add(cmd);
        }

        public void fetchNovel(final DBReaderNovel novel,int engineID, int sessionID ){
            NovelEngineCommand cmd = new NovelEngineCommand();
            cmd.type = fetchNovel;
            cmd.engineCode = engineID;
            cmd.sessionID = sessionID;
            cmd.pars.add(novel);
            commands.add(cmd);
        }


        public void fetchChapter(DBReaderNovel.Chapter chapter, int engineID, int sessionID) {
            NovelEngineCommand cmd = new NovelEngineCommand();
            cmd.type = fetchChapter;
            cmd.engineCode = engineID;
            cmd.sessionID = sessionID;
            cmd.pars.add(chapter);
            commands.add(cmd);
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
    }

    private NovelEngine novelEngine = new NovelEngine();
    private Binder binder = new NovelEngineBinder();
    private ArrayList<IFetchNovelEngine> engines = new ArrayList<IFetchNovelEngine>();
    private ArrayList<NovelEngineCommand> commands = new ArrayList<NovelEngineCommand>();
    private ArrayList<IFetchNovelEngineNotify> notifies = new ArrayList<IFetchNovelEngineNotify>();
    private Thread thrd;

    @Override
    public void onCreate() {
        super.onCreate();
        Initialize();
    }

    @Override
    public void onDestroy() {
        thrd.interrupt();
        super.onDestroy();
    }


    public NovelEngineService() {
        engines.add(new PiaoTianNovel());
    }


    private void Initialize() {
        thrd = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        proc();
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    String err = e.getMessage();
                }
            }
        });
        thrd.start();
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
            if(sessionID != this.currSessionID && this.currSessionID !=-1){
                for(i = 0 ; i < engines.size(); i++){
                    IFetchNovelEngine engine = engines.get(i);
                    engine.cancel();
                }
            }
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
