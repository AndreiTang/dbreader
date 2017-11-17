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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tangqif on 2017/9/23.
 */
public final class PiaoTianNovel implements IFetchNovelEngine {

    private boolean isCancel = false;

    @Override
    public int searchNovels(final String name, ArrayList<DBReaderNovel> nvs) {
        isCancel = false;
        try {
            String url = "http://www.piaotian.com/modules/article/search.php?searchtype=articlename&searchkey=";
            String encodeName = URLEncoder.encode(name, "gb2312");
            url += encodeName;
            StringWriter buf = new StringWriter();
            if (!get(url, buf)) {
                return ERROR_NETWORK;
            }
            String cont = buf.toString();
            Document doc = Jsoup.parse(cont);

            Elements ems = doc.select("em#pagestats");
            if (ems.size() == 0) {
                DBReaderNovel novel = new DBReaderNovel();
                if (fetchNovelByDoc(doc, novel) == NO_ERROR) {
                    nvs.add(novel);
                    return NO_ERROR;
                }
                else{
                    return ERROR_NO_RESULT;
                }
            }
            String pages = ems.first().text();
            int begin = pages.indexOf('/');
            pages = pages.substring(begin+1);
            if(Integer.parseInt(pages) > 4){
                return ERROR_TOO_MANY;
            }

            while (true && !isCancel) {
                parseSearchPage(doc, nvs);
                Elements nexts = doc.select("a.next");
                if (nexts.size() == 0) {
                    break;
                } else {
                    url = "http://www.piaotian.com" + nexts.first().attr("href");
                    buf = new StringWriter();
                    if (!get(url, buf)) {
                        break;
                    }
                    cont = buf.toString();
                    doc = Jsoup.parse(cont);
                }
            }
            if (isCancel) {
                return ERROR_CANCEL;
            }
            if (nvs.size() > 0) {
                return NO_ERROR;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ERROR_NO_RESULT;
    }


    @Override
    public int fetchChapter(final DBReaderNovel.Chapter chapter, StringWriter buf) {
        isCancel = false;
        try {
            Document doc = Jsoup.connect(chapter.url).timeout(3000).get();
            String html = doc.outerHtml();
            int begin = html.indexOf("&nbsp;&nbsp;&nbsp;&nbsp;");
            if (begin == -1) {
                return ERROR_NO_RESULT;
            }
            int end = html.indexOf("<!--", begin + 1);
            if (end == -1) {
                return ERROR_NO_RESULT;
            }

            String cont = html.substring(begin, end);
            cont = cont.replace("&nbsp;", " ");
            cont = cont.replace("<br>", "\n");
            cont = arrangeNovel(cont);
            cont = chapter.name + cont;
            buf.write(cont);
        } catch (IOException e) {
            e.printStackTrace();
            return ERROR_NETWORK;
        }
        if (isCancel) {
            return ERROR_CANCEL;
        }
        return NO_ERROR;
    }

    public int fetchNovel(DBReaderNovel novel) {
        isCancel = false;
        try {
            Document doc = Jsoup.connect(novel.url).timeout(3000).get();
            return fetchNovelByDoc(doc,novel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ERROR_NETWORK;
    }

    public void cancel() {
        isCancel = true;
    }

    private int fetchNovelByDoc(Document doc,DBReaderNovel novel){
        novel.decs = fetchNovelDecs(doc);
        if(novel.author == null || novel.author.isEmpty()){
            novel.author = fetchNovelAuthor(doc);
        }
        if(novel.type == null || novel.type.isEmpty()){
            novel.type = fetchNovelType(doc);
        }
        return fetchNovelFromDoc(doc, novel);
    }

    private String fetchNovelType(Document doc){
        String html = doc.outerHtml();
        html = html.replace("&nbsp;","");
        html = html.replace(" ","");
        String head = "文章状态：";
        int begin = html.indexOf(head);
        if (begin == -1) {
            return "";
        }
        begin += head.length();
        int end = html.indexOf("</td>", begin);
        String str = html.substring(begin, end);
        str = str.replace(" ", "");
        return str;
    }

    private String fetchNovelAuthor(Document doc){
        String html = doc.outerHtml();
        html = html.replace("&nbsp;","");
        html = html.replace(" ","");
        String head = "作者：";
        int begin = html.indexOf(head);
        if (begin == -1) {
            return "";
        }
        begin += head.length();
        int end = html.indexOf("</td>", begin);
        String str = html.substring(begin, end);
        str = str.replace(" ", "");
        return str;
    }

    private String fetchNovelDecs(Document doc) {
        String html = doc.outerHtml();
        String head = "内容简介：";
        int begin = html.indexOf(head);
        if (begin == -1) {
            return "";
        }
        begin += head.length();
        int end = html.indexOf("</td>", begin);
        String str = html.substring(begin, end);
        str = str.replace("</span>", "");
        str = str.replace("&nbsp;", "");
        str = str.replace("<br />", "");
        str = str.replace("\n", "");
        str = str.replace("<br>", "");
        str = str.replace("</div>", "");
        str = str.replace(" ", "");
        str = str.replace("\r", "");
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
                if (data.length() == 0) {
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

    private boolean get(final String urlPath, StringWriter buf) {
        try {
            URL url = new URL(urlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            //urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
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

    private int fetchNovelFromDoc(Document doc, DBReaderNovel novel) {
        Elements eles = doc.select("caption");
        if (eles.size() == 0) {
            return ERROR_NO_RESULT;
        }
        Elements items = eles.first().select("a");
        if (items.size() == 0) {
            return ERROR_NO_RESULT;
        }
        novel.url = items.first().attr("href");
        if (isCancel) {
            return ERROR_NO_RESULT;
        }
        collectChapters(novel);

        if (novel.name == null) {
            novel.name = doc.select("h1").first().text();
        }

        eles = doc.select("a");
        for (Element img : eles) {
            String target = img.attr("target");
            String href = img.attr("href");
            Elements imgs = img.select("img");
            if (target.compareTo("_blank") == 0 && imgs.size() > 0) {
                String imgUrl = imgs.first().attr("src");
                if (imgUrl.indexOf(".jpg") != -1 && imgUrl.compareTo(href) == 0) {
                    novel.img = imgUrl;
                    break;
                }

            }
        }
        if (isCancel) {
            return ERROR_CANCEL;
        }
        return NO_ERROR;
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
                if (isCancel) {
                    break;
                }
                Element it = chap.select("a").first();
                if (it == null) {
                    continue;
                }

                String url = it.attr("href");
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
