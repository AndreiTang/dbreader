package com.moss.dbreader.service.commands;

import com.moss.dbreader.service.IFetchNovelEngine;
import com.moss.dbreader.service.IFetchNovelEngineNotify;

import java.util.List;
import java.util.Map;

/**
 * Created by andrei on 2017/12/5.
 */

public interface INovelServiceCommand {
    void process(Map<String,Object> args, List<IFetchNovelEngine> engines, List<IFetchNovelEngineNotify> notifies);
    void cancel();
}
