package com.moss.dbreader.fragment.events;

import com.moss.dbreader.service.NovelEngineService;

/**
 * Created by andrei on 2017/12/11.
 */

public class FetchEngineEvent {
    public FetchEngineEvent(NovelEngineService.NovelEngine engine) {
        this.engine = engine;
    }
    public NovelEngineService.NovelEngine engine;
}
