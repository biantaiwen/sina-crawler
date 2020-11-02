package com.qxz.sina.crawler;

public class Main {
    public static void main(String[] args) {
        MybatisCrawlerDao dao = new MybatisCrawlerDao();
        for (int i = 0; i < 12; i++) {
            new Crawler(dao).start();
        }
    }
}
