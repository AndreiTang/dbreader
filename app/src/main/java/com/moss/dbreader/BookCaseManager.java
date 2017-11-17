package com.moss.dbreader;

import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.moss.dbreader.service.DBReaderNovel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by tangqif on 10/15/2017.
 */

public class BookCaseManager {

    static ArrayList<DBReaderNovel> novels = new ArrayList<DBReaderNovel>();
    static String appPath;

    static public void initialize(String appPath) {
        if (novels.size() > 0) {
            return;
        }
        BookCaseManager.appPath = appPath + "/";
        Log.i("Andrei","path is "+appPath);
        File dir = new File(appPath);
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }

        File files[] = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                DBReaderNovel novel = readDBReader(file.getName());
                novels.add(novel);
            }
        }
    }

    static public void saveChapterText(String name, int index, String text) {
        String path = getNovelFolder(name) + index + ".txt";
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            OutputStreamWriter fw = new OutputStreamWriter(fo);
            fw.write(text);
            fw.close();
            fo.flush();
            fo.close();
        } catch (FileNotFoundException e) {
            Log.i("Andrei", "create file err");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("Andrei", "Write file err" + e.toString());
            e.printStackTrace();
        }
    }

    static public String getChapterText(String name, int index) {
        String path = getNovelFolder(name) + index + ".txt";
        String chapText = "";
        try {
            File file = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            chapText = line;
            while ((line = reader.readLine())!=null) {
                chapText += "\n";
                chapText += line;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chapText;
    }

    static public void add(DBReaderNovel novel, boolean isFirst) {
        for (int i = 0; i < novels.size(); i++) {
            DBReaderNovel item = novels.get(i);
            if(item == null){
                novels.remove(i);
                i--;
                continue;
            }
            if (item.name.compareTo(novel.name) == 0) {
                novels.remove(i);
                break;
            }
        }
        novel.updateTime = System.currentTimeMillis();
        if (isFirst) {
            novels.add(0, novel);
        } else {
            novels.add(novel);

        }
    }

    static public ArrayList<DBReaderNovel> getNovels() {
        return novels;
    }

    static public String getNovelFolder(String novelName) {
        String path = appPath + novelName;
        File dir = new File(path);
        if (!dir.exists()) {
            boolean ret = dir.mkdirs();
            Log.i("Andrei", "the create floder is " + ret);
        }
        return path + "/";
    }

    static public ArrayList<DBReaderNovel> fetchNovelsInBookCase(){
        ArrayList<DBReaderNovel> bookCase = new ArrayList<DBReaderNovel>();
        for(int i = 0 ;i < BookCaseManager.novels.size(); i++){
            DBReaderNovel nv = BookCaseManager.novels.get(i);
            if(nv != null && nv.isInCase == 1){
                bookCase.add(nv);
            }
        }
        Collections.sort(bookCase);
        return bookCase;
    }

    static public DBReaderNovel readDBReader(String name) {
        String path = getNovelFolder(name) + "novel.json";
        String novelTxt = "";
        try {
            File file = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = reader.readLine()) != null) {
                novelTxt += line + "\n";
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (novelTxt.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(novelTxt, DBReaderNovel.class);
    }

    static public void saveDBReader(DBReaderNovel novel) {
        String path = getNovelFolder(novel.name) + "novel.json";
        Gson gson = new Gson();
        String strNovel = gson.toJson(novel, DBReaderNovel.class);
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            OutputStreamWriter fw = new OutputStreamWriter(fo);
            fw.write(strNovel);
            fw.close();
            fo.flush();
            fo.close();
        } catch (FileNotFoundException e) {
            Log.i("Andrei", "create file err");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("Andrei", "write file err" + e.toString());
            e.printStackTrace();
        }
    }


}
