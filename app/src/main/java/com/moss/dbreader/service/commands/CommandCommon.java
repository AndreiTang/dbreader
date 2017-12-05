package com.moss.dbreader.service.commands;

/**
 * Created by andrei on 2017/12/5.
 */

public final class CommandCommon {
    public static final String TAG_NAME = "name";
    public static final String TAG_SESSION_ID = "sessionID";
    public static final String TAG_NOVEL = "novel";
    public static final String TAG_CHAPTER = "chapter";
    public static final String TAG_ENGINE_ID = "engineID";

    public static final int CMD_SEARCH = 1;
    public static final int CMD_FETCHNOVEL = 2;
    public static final int CMD_FETCHCHAPTER = 3;
    public static final int CMD_CACHRCHAPTERS = 4;
    public static final int CMD_FETCHDELTACHAPTERLIST = 5;

}
