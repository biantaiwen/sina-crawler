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
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Crawler {
    private final CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    public void run() throws SQLException {

        List<String> unHandleUrl = dao.selectUnHandleUrl();
        while (unHandleUrl.size() != 0) {
            String link = unHandleUrl.get(0);
            if (!dao.hasHandleUrl(link)) {
                Document document = getHtmlAndParseByUrl(link);
                dao.insertIntoHandleUrl(link);
                dao.deleteUnHandleUrlByUrl(link);
                Set<String> newsHrefAll = getPageNewsHrefAll(document);
                for (String newsHref : newsHrefAll) {
                    dao.insertIntoUnHandleUrl(newsHref);
                }
                parseNewsDetailAndSave(document, link);
            }
            unHandleUrl = dao.selectUnHandleUrl();
        }
    }

    public static void main(String[] args) throws SQLException {

    }


    private Document getHtmlAndParseByUrl(String url) {
        Document document;
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
        System.out.println("httpGet 2= " + httpGet);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            HttpEntity httpResponseEntity = httpResponse.getEntity();
            String htmlString = EntityUtils.toString(httpResponseEntity);
            document = Jsoup.parse(htmlString);
        } catch (IOException e) {
            System.out.println("e = " + e);
            throw new RuntimeException(e);
        }
        return document;
    }


    private void parseNewsDetailAndSave(Document document, String url) throws SQLException {
        System.out.println("isNewsDetailPage(document) = " + isNewsDetailPage(document));
        if (isNewsDetailPage(document)) {
            News news = parseNewsDetail(document);
            if (news != null) {
                news.setUrl(url);
                dao.insertIntoNews(news);
            }
        }
    }

    private static News parseNewsDetail(Document document) {
        Elements article = document.getElementsByTag("article");
        Element firstElement = article.first();
        if (firstElement != null) {
            String title = firstElement.child(0).text();
            Elements contentElement = firstElement.select("section[class*=art_content]");
            String content = "";
            if (contentElement.size() != 0) {
                content = contentElement.get(0).text();
            }
            return new News(title, content);
        }
        return null;
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
