package com.qxz.sina.crawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcCrawlerDao implements CrawlerDao {

    private final Connection databaseConnection;

    public JdbcCrawlerDao() {
        this.databaseConnection = createDatabaseConnection();
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    private Connection createDatabaseConnection() {
        try {
            String USER_NAME = "root";
            String USER_PASSWORD = "root";
            String DATABASE_URL = "jdbc:h2:file:/Users/guoxiaodong/Desktop/btw_test/java/sina-crawler/db/news";
            return DriverManager.getConnection(DATABASE_URL, USER_NAME, USER_PASSWORD);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException(throwables);
        }
    }

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        try (PreparedStatement statement = databaseConnection.prepareStatement("INSERT INTO UN_HANDLE_URL (URL) values ( ? );");
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String url = resultSet.getString(1);
                deleteUnHandleUrlByUrl(url);
                return url;
            }
            return null;
        }
    }

    @Override
    public void insertIntoUnHandleUrl(String url) throws SQLException {
        try (PreparedStatement statement = databaseConnection.prepareStatement("INSERT INTO UN_HANDLE_URL (URL) values ( ? );")) {
            statement.setString(1, url);
            statement.executeUpdate();
        }
    }

    @Override
    public void insertIntoNews(News news) throws SQLException {
        try (PreparedStatement statement = databaseConnection.prepareStatement("INSERT INTO NEWS (URL, TITLE, CONTENT, CREATE_AT, UPDATE_AT) VALUES (?, ?, ?, NOW(), NOW());")) {
            statement.setString(1, news.url);
            statement.setString(2, news.title);
            statement.setString(3, news.content);
            statement.executeUpdate();
        }
    }

    @Override
    public synchronized void insertIntoHandleUrl(String url) throws SQLException {
        try (PreparedStatement statement = databaseConnection.prepareStatement("INSERT INTO HANDLE_URL (URL) values ( ? );")) {
            statement.setString(1, url);
            statement.executeUpdate();
        }
    }

    @Override
    public void deleteUnHandleUrlByUrl(String url) throws SQLException {
        try (PreparedStatement statement = databaseConnection.prepareStatement("DELETE FROM UN_HANDLE_URL WHERE (URL = ?);")) {
            statement.setString(1, url);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean hasHandleUrl(String url) throws SQLException {
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

    @Override
    public List<String> selectUnHandleUrl() throws SQLException {
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
}
