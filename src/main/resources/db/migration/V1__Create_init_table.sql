-- 未处理的url
CREATE TABLE UN_HANDLE_URL
(
    URL VARCHAR(2000)
);

-- 已处理的url
CREATE TABLE HANDLE_URL
(
    URL VARCHAR(2000)
);

-- 新闻表
CREATE TABLE NEWS (
    ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    URL VARCHAR(2000) DEFAULT '',
    TITLE TEXT DEFAULT '',
    CONTENT TEXT DEFAULT '',
    CREATE_AT TIMESTAMP DEFAULT NOW(),
    UPDATE_AT TIMESTAMP DEFAULT NOW()
);

-- 初始化数据
INSERT INTO UN_HANDLE_URL (URL)
VALUES ('https://sina.cn');