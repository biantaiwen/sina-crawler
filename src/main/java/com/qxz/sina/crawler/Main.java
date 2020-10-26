package com.qxz.sina.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        // https://sina.cn
        String url = "https://sina.cn";
//        String url = "https://sports.sina.cn/china/2020-10-26/detail-iiznctkc7803967.d.html?vt=4&pos=108";
        ArrayList<String> unHandleUrl = new ArrayList<>(Collections.singletonList(url));
        Set<String> handleUrl = new HashSet<>();
        // https://sports.sina.cn/china/2020-10-26/detail-iiznctkc7803967.d.html?vt=4&pos=108
        while (true) {
            if (unHandleUrl.isEmpty()) {
                break;
            }
            String link = unHandleUrl.remove(unHandleUrl.size() - 1);
            if (!handleUrl.contains(link)) {
                handleUrl.add(link);
                System.out.println("link 1 = " + link);
                Document document = getHtmlAndParseByUrl(link);
                Set<String> newsHrefAll = getPageNewsHrefAll(document);
                unHandleUrl.addAll(newsHrefAll);
                parseNewsDetailAndSave(document);
            }

        }

    }

    private static Document getHtmlAndParseByUrl(String url) {
        Document document;
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
        System.out.println("httpGet 2= " + httpGet);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            HttpEntity httpResponseEntity = httpResponse.getEntity();
            String htmlString = EntityUtils.toString(httpResponseEntity);
            document = Jsoup.parse(htmlString);
//            Document document = Jsoup.parse(htmlString);
////            Set<String> pageNewsHrefAll = getPageNewsHrefAll(document);
//            parseNewsDetailAndSave(document);
        } catch (IOException e) {
            System.out.println("e = " + e);
            throw new RuntimeException(e);
        }
        return document;
    }


    private static void parseNewsDetailAndSave(Document document) {
        System.out.println("isNewsDetailPage(document) = " + isNewsDetailPage(document));
        if (isNewsDetailPage(document)) {
            parseNewsDetail(document);
        }
    }

    private static void parseNewsDetail(Document document) {
        Elements article = document.getElementsByTag("article");
        Element firstElement = article.first();
        if (firstElement != null) {
            System.out.println("firstElement.text() = " + firstElement.text());
        }
    }

    private static boolean isNewsDetailPage(Document document) {
        Elements article = document.getElementsByTag("article");
        System.out.println("article.size() = " + article.size());
        return article.size() != 0;
    }

    private static Set<String> getPageNewsHrefAll(Document document) {
        Set<String> hrefList = new HashSet<>();
        Elements elements = document.select("a");
        for (Element element : elements) {
            String href = element.attr("href");
            if (isNewsPageUrl(href)) {
                hrefList.add(href);
            }
        }
        return hrefList;
    }

    private static boolean isNewsPageUrl(String href) {
        // (http://)|
        String pattern = "^((https://))([a-zA-Z0-9]*)\\.sina\\.cn(/).*";
        return Pattern.matches(pattern, href);
    }
}
