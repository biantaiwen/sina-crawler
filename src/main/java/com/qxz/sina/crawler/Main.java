package com.qxz.sina.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {
        // https://sina.cn


    }

    private static String getHtmlStringByUrl(String url) {
        String htmlString;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            HttpEntity httpResponseEntity = httpResponse.getEntity();
            htmlString = EntityUtils.toString(httpResponseEntity);
//            Document document = Jsoup.parse(htmlString);
////            Set<String> pageNewsHrefAll = getPageNewsHrefAll(document);
//            parseNewsDetailAndSave(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return htmlString;
    }


    private static void parseNewsDetailAndSave(Document document) {
        if (isNewsDetailPage(document)){
            parseNewsDetail(document);
        }
    }

    private static void parseNewsDetail(Document document) {
        Elements article = document.getElementsByTag("article");
        Element firstElement = article.first();
        if (firstElement!=null){
            System.out.println("firstElement.text() = " + firstElement.text());
        }
    }

    private static boolean isNewsDetailPage(Document document) {
        Elements article = document.getElementsByTag("article");
        return article.size()!=0;
    }

    private static Set<String> getPageNewsHrefAll(Document document) {
        Set<String> hrefList = new HashSet<>();
        Elements elements = document.select("a");
        for (Element element : elements) {
            String href = element.attr("href");
            if (isNewsPageUrl(href)){
                hrefList.add(href);
            }
        }
        return hrefList;
    }
    private static boolean isNewsPageUrl(String href) {
        String pattern = "^((http://)|(https://))([a-zA-Z0-9]*)\\.sina\\.cn(/).*";
        return Pattern.matches(pattern, href);
    }
}
