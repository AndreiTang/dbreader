package com.moss.dbreader.service.events;

/**
 * Created by andrei on 2017/12/7.
 */

public class CacheChaptersEvent {
    public CacheChaptersEvent(String novelName){
        this.novelName = novelName;
    }
    public String novelName;
}
