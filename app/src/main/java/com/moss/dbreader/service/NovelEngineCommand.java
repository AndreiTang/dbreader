package com.moss.dbreader.service;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by tangqif on 2017/10/1.
 */

public class NovelEngineCommand {
    public enum CommandType {
        search, fetchNovel,fetchChapter;
    };
    public int sessionID;
    public CommandType type;
    public int engineCode;
    public ArrayList<Object> pars = new ArrayList<Object>();
    public boolean isCancel = false;
}
