package com.moss.dbreader.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tangqif on 2017/9/23.
 */
public final class PiaoTianNovel implements IFetchNovelEngine {
    @Override
    public ArrayList<DBReaderRemoteNovel> searchNovels(String name) {
        String encodeName = null;
        ArrayList<DBReaderRemoteNovel> nvs = new ArrayList<DBReaderRemoteNovel>();
        try {
            encodeName = URLEncoder.encode(name, "gb2312");
            String url = "http://www.piaotian.com/modules/article/search.php?searchtype=articlename&searchkey=" + encodeName;
            while (url != null) {
                Document doc = Jsoup.connect(url).timeout(3000).get();
                parseSearchPage(doc, nvs);
                Elements nexts = doc.select("a.next");
                if (nexts.size() == 0) {
                    url = null;
                } else {
                    url = "http://www.piaotian.com" + nexts.first().attr("href");
                }
            }
            if (nvs.size() > 0) {
                return nvs;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean fetchNovel(final DBReaderRemoteNovel remote,DBReaderLocalNovel local) {
        try {
            Document doc = Jsoup.connect(remote.url).timeout(3000).get();

            Elements eles = doc.select("caption");
            Elements items = eles.first().select("a");
            local.url = items.first().attr("href");
            collectChapters(local);

            eles = doc.select("a");
            for(Element img : eles){
                String target = img.attr("target");
                if(target.compareTo("_blank")==0){
                    local.img = img.select("img").first().attr("src");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String fetchChapter(DBReaderLocalNovel.Chapter chapter){
        String cont = "";
        try {
            Document doc = Jsoup.connect("http://www.piaotian.com/html/8/8511/5863410.html").timeout(3000).get();
            String html = doc.outerHtml();
            int begin = html.indexOf("&nbsp;&nbsp;&nbsp;&nbsp;");
            if(begin == -1){
                return "";
            }
            int end = html.indexOf("<!--",begin+1);
            if(end ==-1){
                return "";
            }
            cont = html.substring(begin,end);
            cont = cont.replace("&nbsp;&nbsp;&nbsp;&nbsp;","  ");
            cont = cont.replace("<br>","\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cont;
    }

    private void parseSearchPage(Document doc, ArrayList<DBReaderRemoteNovel> nvs) {
        Elements items = doc.select("div#content");
        if (items.size() == 0) {
            return;
        }
        Elements novels = items.select("tr");
        if (novels.size() == 0) {
            return;
        }
        for (Element ele : novels) {
            DBReaderRemoteNovel it = new DBReaderRemoteNovel();
            boolean ret = parseNovel(ele, it);
            if (ret) {
                nvs.add(it);
            }
        }
    }

    private boolean parseNovel(Element novel, DBReaderRemoteNovel it) {
        Element item = novel.child(0);
        if (item.tagName().compareTo("td") != 0) {
            return false;
        }
        Element ele = item.select("a").first();
        it.name = ele.text();
        it.url = ele.attr("href");

        item = novel.child(2);
        it.author = item.text();

        item = novel.child(4);
        it.updateDate = item.text();

        item = novel.child(5);
        it.type = item.text();

        return true;
    }

    private void collectChapters(DBReaderLocalNovel local){
        try {
            Document doc = Jsoup.connect(local.url).timeout(3000).get();
            Elements chaps = doc.select("li");
            String regEx = "^[0-9]{1,}.html";
            Pattern pattern = Pattern.compile(regEx);
            for(Element chap : chaps){
                Element it = chap.select("a").first();
                if(it == null){
                    continue;
                }
                String url = it.attr("href");

                Matcher matcher = pattern.matcher(url);
                boolean bRet = matcher.matches();
                if(bRet){
                    local.Add(it.text(),url);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
