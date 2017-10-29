package com.moss.dbreader.service;

import android.net.Uri;

import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tangqif on 2017/9/23.
 */
public final class PiaoTianNovel implements IFetchNovelEngine {

    @Override
    public boolean searchNovels(final String name, ArrayList<DBReaderNovel> nvs) {
        try {
            String url = "http://www.piaotian.com/modules/article/search.php?searchtype=articlename&searchkey=";
            String encodeName = URLEncoder.encode(name, "gb2312");
            url += encodeName;
            StringWriter buf = new StringWriter();
            if(!get(url,buf)){
                return false;
            }
            String cont = buf.toString();
            Document doc = Jsoup.parse(cont);
            while (true) {
                parseSearchPage(doc, nvs);
                Elements nexts = doc.select("a.next");
                if (nexts.size() == 0) {
                    break;
                } else {
                    url = "http://www.piaotian.com" + nexts.first().attr("href");
                    buf = new StringWriter();
                    if(!get(url,buf)){
                        break;
                    }
                    cont = buf.toString();
                    doc = Jsoup.parse(cont);
                }
            }
            if (nvs.size() > 0) {
                return true;
            } else {
                DBReaderNovel novel = new DBReaderNovel();
                if (fetchNovelFromDoc(doc, novel)) {
                    nvs.add(novel);
                    return true;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    @Override
    public boolean fetchChapter(final DBReaderNovel.Chapter chapter, StringWriter buf) {
        try {
            Document doc = Jsoup.connect(chapter.url).timeout(3000).get();
            String html = doc.outerHtml();
            int begin = html.indexOf("&nbsp;&nbsp;&nbsp;&nbsp;");
            if (begin == -1) {
                return false;
            }
            int end = html.indexOf("<!--", begin + 1);
            if (end == -1) {
                return false;
            }

            String cont = html.substring(begin, end);
            cont = cont.replace("&nbsp;", " ");
            cont = cont.replace("<br>", "\n");
            cont = arrangeNovel(cont);
            cont = chapter.name + cont;
            buf.write(cont);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean fetchNovel(DBReaderNovel novel) {
        try {
            Document doc = Jsoup.connect(novel.url).timeout(3000).get();
            novel.decs = fetchNovelDecs(doc);
            return fetchNovelFromDoc(doc, novel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String fetchNovelDecs(Document doc){
        String html  = doc.outerHtml();
        String head = "内容简介：</span>";
        int begin = html.indexOf(head);
        if(begin == -1){
            return "";
        }
        begin += head.length();
        int end = html.indexOf("</td>",begin);
        String str = html.substring(begin,end);
        str = str.replace("&nbsp;","");
        str = str.replace("<br />","");
        str = str.replace("\n","");
        str = str.replace("<br>","");
        str = str.replace("</div>","");
        str = str.replace(" ","");
        return str;
    }

    private String arrangeNovel(final String txt) {
        BufferedReader reader = new BufferedReader(new StringReader(txt));
        String cont = "";
        try {
            String line = "";
            while ((line = reader.readLine()) != null) {
                String data = line;
                data = data.trim();
                if(data.length() == 0){
                    continue;
                }
                cont += "\n";
                cont += line;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return cont;
    }

    private boolean get(final String urlPath,StringWriter buf) {
        try {
            URL url = new URL(urlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (200 != urlConnection.getResponseCode()) {
                return false;
            }

            InputStream is = urlConnection.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while (-1 != (len = is.read(buffer))) {
                output.write(buffer, 0, len);
                output.flush();
            }
            buf.write(output.toString("gb2312"));
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean fetchNovelFromDoc(Document doc, DBReaderNovel novel) {
        Elements eles = doc.select("caption");
        if (eles.size() == 0) {
            return false;
        }
        Elements items = eles.first().select("a");
        if (items.size() == 0) {
            return false;
        }
        novel.url = items.first().attr("href");
        collectChapters(novel);

        if(novel.name == null){
            novel.name = doc.select("h1").first().text();
        }

        eles = doc.select("a");
        for (Element img : eles) {
            String target = img.attr("target");
            String href = img.attr("href");
            Elements imgs = img.select("img");
            if (target.compareTo("_blank") == 0 && imgs.size() >0) {
                String imgUrl = imgs.first().attr("src");
                if(imgUrl.indexOf(".jpg")!=-1 && imgUrl.compareTo(href) == 0){
                    novel.img = imgUrl;
                    break;
                }

            }
        }
        return true;
    }


    private void parseSearchPage(Document doc, ArrayList<DBReaderNovel> nvs) {
        Elements items = doc.select("div#content");
        if (items.size() == 0) {
            return;
        }
        Elements novels = items.select("tr");
        if (novels.size() == 0) {
            return;
        }
        for (Element ele : novels) {
            DBReaderNovel it = new DBReaderNovel();
            boolean ret = parseNovel(ele, it);
            if (ret) {
                nvs.add(it);
            }
        }
    }

    private boolean parseNovel(Element novel, DBReaderNovel it) {
        if (novel.children().size() < 6) {
            return false;
        }
        Element item = novel.child(0);
        if (item.tagName().compareTo("td") != 0) {
            return false;
        }
        Elements els = item.select("a");
        if (els.size() == 0) {
            return false;
        }
        Element ele = els.first();
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

    private void collectChapters(DBReaderNovel local) {
        try {
            Document doc = Jsoup.connect(local.url).timeout(3000).get();
            Elements chaps = doc.select("li");
            String regEx = "^[0-9]{1,}.html";
            Pattern pattern = Pattern.compile(regEx);
            for (Element chap : chaps) {
                Element it = chap.select("a").first();
                if (it == null) {
                    continue;
                }

                String url = it.attr("href");
                ;
                Matcher matcher = pattern.matcher(url);
                boolean bRet = matcher.matches();
                if (bRet) {
                    int end = local.url.indexOf("index.html");
                    url = local.url.substring(0, end) + url;
                    local.Add(it.text(), url);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
