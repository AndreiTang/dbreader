package com.moss.dbreader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by tangqif on 2017/9/30.
 */

public class NovelEngineService extends Service {

    private class NovelEngineBinder extends Binder{
        NovelEngineService getNovelEngineService(){
            return NovelEngineService.this;
        }
    }

    private final Binder binder = new NovelEngineBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    boolean searchNovel(final String nsme){
        return true;
    }


}
