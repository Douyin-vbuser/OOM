package com.vbuser.cr;

import com.vbuser.database.Console;
import com.vbuser.database.New;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

public class BilibiliCrawler {
    private static final OkHttpClient client = new OkHttpClient();

    static String table;

    public static void run(String keyword){
        int totalPages = 50;
        table = keyword;
        New.createTable(keyword,Main.columns,Main.db);

        for (int page = 1; page <= totalPages; page++) {
            String url = buildUrl(keyword, page);
            System.out.println("Fetching page: " + page + ", URL: " + url);

            try {
                String json = fetchData(url);
                parseAndInsertData(json);

                Thread.sleep(3000 + (int) (Math.random() * 1000));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String buildUrl(String keyword, int page) {
        return "https://api.bilibili.com/x/web-interface/wbi/search/type" +
                "?search_type=video" +
                "&order=pubdate" +
                "&keyword=" + keyword +
                "&page=" + page;
    }

    private static String fetchData(String url) throws IOException {

        String cookie = "buvid3="+g(8)+"-"+g(4)+"-"+g(4)+"-"+g(4)+"-"+g(17)+"infoc; " +
                "_uuid="+g(9)+"-"+g(4)+"-"+g(4)+"-"+g(5)+"-"+g(18)+"infoc; " +
                "enable_web_push=DISABLE; " +
                "enable_feed_channel=DISABLE; " +
                "home_feed_column=4; " +
                "browser_resolution="+((int)(300+Math.random()*50))+"-"+((int)(600+Math.random()*110));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", cookie)
                .addHeader("Referer", "https://www.bilibili.com")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code: " + response);
            }
            assert response.body() != null;
            return response.body().string();
        }
    }

    private static void parseAndInsertData(String json) {
        JSONObject root = new JSONObject(json);
        if (root.getInt("code") != 0) {
            System.out.println("Error: " + root.getString("message"));
            return;
        }

        JSONObject data = root.getJSONObject("data");
        try {
            JSONArray result = data.getJSONArray("result");


            for (int i = 0; i < result.length(); i++) {
                JSONObject video = result.getJSONObject(i);

                String mid = String.valueOf(video.getLong("mid"));
                String bvid = video.getString("bvid");
                String title = video.getString("title");
                long pubdate = video.getLong("pubdate");
                int play = video.getInt("play");

                insertIntoDatabase(mid, bvid, title, pubdate, play);
            }
        }catch (Exception e){
            System.out.println(data.toString(2));
        }
    }

    private static void insertIntoDatabase(String mid, String bvid, String title, long pubdate, int play) {
        String[] columns = {"mid", "bvid", "title", "pubdate", "play"};
        String[] values = {mid, bvid, title, String.valueOf(pubdate), String.valueOf(play)};

        New.insert(table, columns, values, Console.getDataBase());
    }

    private static String g(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(Integer.toHexString(random.nextInt(16)));
        }
        return sb.toString().toUpperCase();
    }
}
