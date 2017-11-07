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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by tangqif on 10/15/2017.
 */

public class BookCaseManager {

    static final String novelFolder = "/dbreader/novels/";
    static ArrayList<String> novels = new ArrayList<String>();

    static public void initialize(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + novelFolder;
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
            return;
        }

        File files[] = dir.listFiles();
        for(File file: files){
            if(file.isDirectory()){
                novels.add(file.getName());
            }
        }
    }

    static public void add(String name){
        novels.add(name);
    }


    static public String getNovelFolder(String novelName){
        File root = Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + novelFolder + novelName;
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return path;
    }

    static public DBReaderNovel readDBReader(String name){
        String path = getNovelFolder(name) + "novel.json";
        String novelTxt="";
        try {
            FileInputStream file = new FileInputStream(path);
            InputStreamReader inputStreamReader = new InputStreamReader(file);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = "";
            while ((line = reader.readLine())!=null){
                novelTxt += line + "\n";
            }
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(novelTxt.isEmpty()){
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(novelTxt,DBReaderNovel.class);
    }

    static public void saveDBReader(DBReaderNovel novel) {
        String path = getNovelFolder(novel.name) + "novel.json";
        Gson gson = new Gson();
        String strNovel = gson.toJson(novel,DBReaderNovel.class);
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
