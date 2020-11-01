package com.qxz.sina.crawler;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MybatisCrawlerDao implements CrawlerDao {
    private final SqlSessionFactory sqlSessionFactory;

    public MybatisCrawlerDao() {
        String resource = "db/mybatis/config.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertIntoUnHandleUrl(String url) {
        insertIntoUrl(url, "UN_HANDLE_URL");
    }

    @Override
    public void insertIntoNews(News news) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            sqlSession.insert("com.qxz.sina.crawler.MyMapper.insertNews", news);
        }
    }

    @Override
    public void insertIntoHandleUrl(String url) {
        insertIntoUrl(url, "HANDLE_URL");
    }

    private synchronized void insertIntoUrl(String url, String tableName) {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", tableName);
        param.put("URL", url);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.qxz.sina.crawler.MyMapper.insertUrl", param);
        }
    }

    @Override
    public void deleteUnHandleUrlByUrl(String url) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            sqlSession.delete("com.qxz.sina.crawler.MyMapper.deleteUnHandleByUrl", url);
        }
    }

    @Override
    public synchronized boolean hasHandleUrl(String url) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            Integer count = sqlSession.selectOne("com.qxz.sina.crawler.MyMapper.handleUrlCountByUrl", url);
            return count != 0;
        }
    }

    @Override
    public List<String> selectUnHandleUrl() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return sqlSession.selectList("com.qxz.sina.crawler.MyMapper.unHandleUrl");
        }
    }
}
