package com.qxz.sina.crawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Main {
    private static final String USER_NAME = "root";
    private static final String USER_PASSWORD = "root";
    private static final String DATABASE_URL = "jdbc:h2:file:/Users/guoxiaodong/Desktop/btw_test/java/sina-crawler/db/news";

    public static void main(String[] args) throws SQLException {

        Connection databaseConnection = createDatabaseConnection();
        List<String> unHandleUrl = selectUnHandleUrl(databaseConnection);
        while (unHandleUrl.size() != 0) {
            String link = unHandleUrl.get(0);
            if (!hasHandleUrl(databaseConnection, link)) {
                Document document = getHtmlAndParseByUrl(link);
                insertIntoHandleUrl(databaseConnection, link);
                deleteUnHandleUrlByUrl(databaseConnection, link);
                Set<String> newsHrefAll = getPageNewsHrefAll(document);
                for (String newsHref : newsHrefAll) {
                    insertIntoUnHandleUrl(databaseConnection, newsHref);
                }
                parseNewsDetailAndSave(databaseConnection, document, link);
            }
            unHandleUrl = selectUnHandleUrl(databaseConnection);
        }

    }

    private static void insertIntoUnHandleUrl(Connection databaseConnection, String url) throws SQLException {
        try (PreparedStatement statement = databaseConnection.prepareStatement("INSERT INTO UN_HANDLE_URL (URL) values ( ? );")) {
            statement.setString(1, url);
            statement.executeUpdate();
        }
    }

    private static void insertIntoNews(Connection databaseConnection, News news) throws SQLException {

        try (PreparedStatement statement = databaseConnection.prepareStatement("INSERT INTO NEWS (URL, TITLE, CONTENT, CREATE_AT, UPDATE_AT) VALUES (?, ?, ?, NOW(), NOW());")) {
            statement.setString(1, news.url);
            statement.setString(2, news.title);
            statement.setString(3, news.content);
            statement.executeUpdate();
        }
    }

    private static void insertIntoHandleUrl(Connection databaseConnection, String url) throws SQLException {
        try (PreparedStatement statement = databaseConnection.prepareStatement("INSERT INTO HANDLE_URL (URL) values ( ? );")) {
            statement.setString(1, url);
            statement.executeUpdate();
        }
    }

    private static void deleteUnHandleUrlByUrl(Connection databaseConnection, String url) throws SQLException {
        try (PreparedStatement statement = databaseConnection.prepareStatement("DELETE FROM UN_HANDLE_URL WHERE (URL = ?);")) {
            statement.setString(1, url);
            statement.executeUpdate();
        }
    }

    private static boolean hasHandleUrl(Connection databaseConnection, String url) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = databaseConnection.prepareStatement("SELECT COUNT(*) FROM HANDLE_URL WHERE URL = ?;")) {
            statement.setString(1, url);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) != 0;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    private static List<String> selectUnHandleUrl(Connection databaseConnection) throws SQLException {
        List<String> stringList = new ArrayList<>();
        try (PreparedStatement statement = databaseConnection.prepareStatement("SELECT * FROM UN_HANDLE_URL;");
             ResultSet resultSet = statement.executeQuery();
        ) {
            while (resultSet.next()) {
                stringList.add(resultSet.getString(1));
            }
        }
        return stringList;
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    private static Connection createDatabaseConnection() {
        try {
            return DriverManager.getConnection(DATABASE_URL, USER_NAME, USER_PASSWORD);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException(throwables);
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


    private static void parseNewsDetailAndSave(Connection databaseConnection, Document document, String url) throws SQLException {
        System.out.println("isNewsDetailPage(document) = " + isNewsDetailPage(document));
        if (isNewsDetailPage(document)) {
            News news = parseNewsDetail(document);
            if (news != null) {
                news.setUrl(url);
                insertIntoNews(databaseConnection, news);
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
