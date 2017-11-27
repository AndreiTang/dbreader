package com.moss.dbreader;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by andrei on 2017/11/1.
 */

public final class Common {
    public static final String TAG_NOVEL = "novel";
    public static final String TAG_ENGINE_ID = "engineID";
    public static final String TAG_CUR_PAGE = "curPage";
    public static final String TAG_MAIN_CATEGORY = "mainCategory";

    public static void changeStatusBarColor(Activity activy, int color){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = activy.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }
}
