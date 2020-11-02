package com.qxz.sina.crawler;

import java.sql.SQLException;
import java.util.List;

public interface CrawlerDao {

    String getNextLinkThenDelete() throws SQLException;

    void insertIntoUnHandleUrl(String url) throws SQLException;

    void insertIntoNews(News news) throws SQLException;

    void insertIntoHandleUrl(String url) throws SQLException;

    void deleteUnHandleUrlByUrl(String url) throws SQLException;

    boolean hasHandleUrl(String url) throws SQLException;

    List<String> selectUnHandleUrl() throws SQLException;
}
