package com.moss.dbreader;

import android.os.Environment;
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
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by tangqif on 10/15/2017.
 */

public class BookCaseManager {

    static final String novelFolder = "/dbreader/novels/";
    static ArrayList<DBReaderNovel> novels = new ArrayList<DBReaderNovel>();

    static public void initialize() {
        if (novels.size() > 0) {
            return;
        }
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + novelFolder;
        File dir = new File(path);
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
        String path = getNovelFolder(name) +  index + ".txt";
        try {
            FileOutputStream file = new FileOutputStream(path);
            file.write(text.getBytes());
            file.flush();
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public String getChapterText(String name, int index) {
        String path = getNovelFolder(name) +index + ".txt";
        String chapText="";
        try {
            File file = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                chapText += "\n";
                chapText += line;
                line = reader.readLine();
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    static public void add(DBReaderNovel novel) {
        for(int i = 0 ; i < novels.size(); i++){
            DBReaderNovel item = novels.get(i);
            if(item.name.compareTo(novel.name) == 0){
                novels.remove(i);
               break;
            }
        }
        novels.add(novel);
    }

    static public ArrayList<DBReaderNovel> getNovels() {
        return novels;
    }

    static public String getNovelFolder(String novelName) {
        File root = Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + novelFolder  + novelName + "/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return path;
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
            FileOutputStream file = new FileOutputStream(path);
            file.write(strNovel.getBytes());
            file.flush();
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
