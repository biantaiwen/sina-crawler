package com.qxz.sina.crawler;

public class Main {
    public static void main(String[] args) {
        MybatisCrawlerDao dao = new MybatisCrawlerDao();
        new Crawler(dao).start();
    }
}
