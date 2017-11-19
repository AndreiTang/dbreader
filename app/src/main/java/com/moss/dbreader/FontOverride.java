package com.moss.dbreader;

import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangqif on 11/19/2017.
 */

final public class FontOverride {
    public static  void setDefaultFont(Context context,String typefaceFontName, String fontPath){
        Typeface typeface = Typeface.createFromAsset(context.getAssets(),fontPath);
        replaceFont(typefaceFontName,typeface);
    }

    protected static void replaceFont(String typefaceFontName,Typeface typeface){
        try {
            Field field = Typeface.class.getDeclaredField(typefaceFontName);
            field.setAccessible(true);
            field.set(null,typeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
