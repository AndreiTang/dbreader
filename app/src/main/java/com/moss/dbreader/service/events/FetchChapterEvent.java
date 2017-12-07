package com.moss.dbreader.service.events;

/**
 * Created by andrei on 2017/12/7.
 */

public class FetchChapterEvent {
    public FetchChapterEvent(int nRet,int sessionID , int chapterIndex, String cont){
        this.chapterIndex = chapterIndex;
        this.nRet = nRet;
        this.sessionID = sessionID;
        this.cont = cont;
    }
    public int chapterIndex;
    public String cont;
    public int nRet;
    int sessionID;
}
