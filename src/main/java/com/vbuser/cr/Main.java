package com.vbuser.cr;

import com.vbuser.database.Console;
import com.vbuser.database.Init;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        initEv(System.getProperty("timestamp"));
        BilibiliCrawler.run("原神");
    }

    static String[] columns = {"mid", "bvid", "title", "pubdate", "play"};

    public static void initEv(String timestamp) throws IOException {
        File root = new File(System.getProperty("user.dir"));
        File dataBase = Init.initBase(timestamp, root);
        db = dataBase;

        Console.setDataBase(dataBase);
    }

    public static File db;
}