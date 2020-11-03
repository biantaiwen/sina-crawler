# sina-crawler
一个简单的爬虫程序，爬取手机新浪网站

---
本地运行需用docker启动mysql
```shell script
docker run --name my-mysql -p 3306:3306 -v `pwd`/db/mysql:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -d mysql:8.0.22
```