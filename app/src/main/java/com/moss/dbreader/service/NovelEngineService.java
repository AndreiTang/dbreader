package com.moss.dbreader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import com.moss.dbreader.service.commands.CacheChaptersCommand;
import com.moss.dbreader.service.commands.FetchChapterCommand;
import com.moss.dbreader.service.commands.FetchDeltaChapterListCommand;
import com.moss.dbreader.service.commands.FetchNovelCommand;
import com.moss.dbreader.service.commands.INovelServiceCommand;
import com.moss.dbreader.service.commands.CommandCommon;
import com.moss.dbreader.service.commands.SearchCommand;
import com.moss.dbreader.service.events.InitializedEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by tangqif on 2017/9/30.
 */

public class NovelEngineService extends Service {

    private static int sessionID = 0;

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
            Thread thrd = new Thread(new Runnable() {
                @Override
                public void run() {
                    NovelInfoManager.initialize(NovelEngineService.this.getApplicationContext().getFilesDir().getAbsolutePath());
                    EventBus.getDefault().post(new InitializedEvent());
                    ArrayList<DBReaderNovel> novels = NovelInfoManager.getNovels();
                    for(int i = 0;i < novels.size(); i++){
                        DBReaderNovel item = novels.get(i);
                        if(item.isInCase == 1){
                            fetchDeltaChapterList(item);
                        }
                    }
                }
            });
            thrd.start();
        }

        public void searchNovel(final String name, int sessionID) {
            HashMap<String,Object> args = new HashMap<>();
            args.put(CommandCommon.TAG_NAME,name);
            args.put(CommandCommon.TAG_SESSION_ID,sessionID);
            sendMsg(CommandCommon.CMD_SEARCH,args);
        }

        public void fetchNovel(final DBReaderNovel novel, int sessionID) {
            HashMap<String,Object> args = new HashMap<>();
            args.put(CommandCommon.TAG_NOVEL,novel);
            args.put(CommandCommon.TAG_SESSION_ID,sessionID);
            sendMsg(CommandCommon.CMD_FETCHNOVEL,args);
        }


        public void fetchChapter(DBReaderNovel novel, DBReaderNovel.Chapter chapter, int sessionID) {
            HashMap<String,Object> args = new HashMap<>();
            args.put(CommandCommon.TAG_NOVEL,novel);
            args.put(CommandCommon.TAG_CHAPTER,chapter);
            args.put(CommandCommon.TAG_SESSION_ID,sessionID);
            sendMsg(CommandCommon.CMD_FETCHCHAPTER,args);
        }

        public void cacheChapters(DBReaderNovel novel) {
            HashMap<String,Object> args = new HashMap<>();
            args.put(CommandCommon.TAG_NOVEL,novel);
            sendCacheMsg(CommandCommon.CMD_CACHRCHAPTERS,args);
        }

        public void fetchDeltaChapterList(final DBReaderNovel novel) {
            HashMap<String,Object> args = new HashMap<>();
            args.put(CommandCommon.TAG_NOVEL,novel);
            sendCacheMsg(CommandCommon.CMD_FETCHDELTACHAPTERLIST,args);
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

    class ServiceHandler extends Handler {

        int lastMsgID = 0;

        public ServiceHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){
            NovelEngineService.this.procMessage(msg);
            if(lastMsgID - 1 == msg.arg1){
                getLooper().quit();
            }
        }

        void sendMsg(Message msg){
            msg.arg1 = lastMsgID;
            sendMessage(msg);
            lastMsgID++;
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////


    private NovelEngine novelEngine = new NovelEngine();
    private Binder binder = new NovelEngineBinder();
    private ArrayList<IFetchNovelEngine> engines = new ArrayList<IFetchNovelEngine>();
    private ArrayList<IFetchNovelEngineNotify> notifies = new ArrayList<IFetchNovelEngineNotify>();
    private HandlerThread thrd = null;
    private HandlerThread thrdCache = null;
    private ServiceHandler handler = null;
    private ServiceHandler handlerCache = null;
    private ArrayList<Integer> cancelIDs = new ArrayList<Integer>();
    private HashMap<Integer,INovelServiceCommand> cmds = new HashMap<Integer, INovelServiceCommand>();


    public NovelEngineService() {
        engines.add(new PiaoTianNovel());
        cmds.put(CommandCommon.CMD_SEARCH,new SearchCommand());
        cmds.put(CommandCommon.CMD_FETCHNOVEL,new FetchNovelCommand());
        cmds.put(CommandCommon.CMD_FETCHCHAPTER,new FetchChapterCommand());
        cmds.put(CommandCommon.CMD_CACHRCHAPTERS,new CacheChaptersCommand());
        cmds.put(CommandCommon.CMD_FETCHDELTACHAPTERLIST,new FetchDeltaChapterListCommand());
    }

    public void cancel(int sessionID){
        this.cancelIDs.add(sessionID);
    }

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
        this.cmds.clear();
        this.notifies.clear();
        this.engines.clear();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    void sendMsg(int what, Map<String,Object> args){
        if(this.thrd == null || !this.thrd.isAlive()){
            this.thrd = new HandlerThread("");
            this.thrd.start();
            this.handler = new ServiceHandler(this.thrd.getLooper());
        }
        Message msg = this.handler.obtainMessage(what,args);
        handler.sendMsg(msg);
    }

    void sendCacheMsg(int what, Map<String,Object> args){
        if(this.thrdCache == null || !this.thrdCache.isAlive()){
            this.thrdCache = new HandlerThread("");
            this.thrdCache.start();
            this.handlerCache = new ServiceHandler(this.thrdCache.getLooper());
        }
        Message msg = this.handlerCache.obtainMessage(what,args);
        handlerCache.sendMsg(msg);
    }

    void procMessage(Message msg){
        Map<String,Object> args =  ( Map<String,Object>)msg.obj;
        Integer sid = (Integer) args.get(CommandCommon.TAG_SESSION_ID);
        if(sid != null && this.cancelIDs.contains(sid) ){
            return;
        }
        INovelServiceCommand cmd = cmds.get(msg.what);
        if(cmd != null){
            cmd.process(args,this.engines);
        }
    }

}
