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

import static com.moss.dbreader.service.NovelEngineCommand.CommandType.fetchChapter;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.fetchNovel;
import static com.moss.dbreader.service.NovelEngineCommand.CommandType.search;

/**
 * Created by tangqif on 2017/9/30.
 */

public class NovelEngineService extends Service {


    public class NovelEngineBinder extends Binder {
        public NovelEngine getNovelEngine() {
            return novelEngine;
        }
    }

    public class NovelEngine {
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

        public void addNotify(IFetchNovelEngineNotify notify) {
            notifies.add(notify);
        }
    }

    private NovelEngine novelEngine = new NovelEngine();
    private Binder binder = new NovelEngineBinder();
    private ArrayList<IFetchNovelEngine> engines = new ArrayList<IFetchNovelEngine>();
    private Queue<NovelEngineCommand> commands = new LinkedList<NovelEngineCommand>();
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

    private void proc() {
        if (commands.size() == 0) {
            return;
        }
        NovelEngineCommand cmd = commands.remove();
        NovelEngineCommand.CommandType type = cmd.type;
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
    }

    private void procSearch(final String name, int sessionID) {
        Integer engineID = 0;
        ArrayList<DBReaderNovel> novels = new ArrayList<DBReaderNovel>();
        boolean bRet = searchNovels(name, engineID, novels);
        DBReaderNovel novel = novels.get(0);
        if(novels.size() == 1 && novel.chapters.size() > 0){
            for (int i = 0; i < notifies.size(); i++) {
                notifies.get(i).OnFetchNovel(bRet, sessionID, engineID, novel);
            }
        }
        else{
            for (int i = 0; i < notifies.size(); i++) {
                notifies.get(i).OnSearchNovels(bRet, sessionID, engineID, novels);
            }
        }
    }

    private boolean searchNovels(final String name, Integer engineID, ArrayList<DBReaderNovel> novels) {
        for (int i = 0; i < engines.size(); i++) {
            IFetchNovelEngine engine = engines.get(i);
            boolean bRet = engine.searchNovels(name, novels);
            if (bRet) {
                engineID = i;
                return true;
            }
        }
        return false;
    }

    private void procFetchNovel(DBReaderNovel novel, int engineID, int sessionID){
        boolean bRet = false;
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            bRet = engine.fetchNovel(novel);
        }
        for (int i = 0; i < notifies.size(); i++) {
            notifies.get(i).OnFetchNovel(bRet, sessionID, engineID, novel);
        }
    }

    private void procFetchChapter(final DBReaderNovel.Chapter chapter, int engineID, int sessionID) {
        boolean bRet = false;
        String cont = "";
        if (engineID > 0 || engineID < engines.size()) {
            IFetchNovelEngine engine = engines.get(engineID);
            StringWriter buf = new StringWriter();
            bRet = engine.fetchChapter(chapter, buf);
            cont = buf.toString();
        }

        for (int i = 0; i < notifies.size(); i++) {
            notifies.get(i).OnFetchChapter(bRet, sessionID, chapter.index,cont);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
