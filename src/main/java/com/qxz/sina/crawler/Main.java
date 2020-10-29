package com.qxz.sina.crawler;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        JdbcCrawlerDao jdbcCrawlerDao = new JdbcCrawlerDao();
        Crawler crawler = new Crawler(jdbcCrawlerDao);
        try {
            crawler.run();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new RuntimeException();
        }
    }
}
