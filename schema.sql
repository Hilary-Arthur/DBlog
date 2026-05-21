-- 星码记 博客数据库建表脚本
-- 数据库: blog (需先创建: CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4;)

-- 博客文章表
CREATE TABLE IF NOT EXISTS `post` (
    `pid`        BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title`      VARCHAR(200) NOT NULL,
    `content`    TEXT         NOT NULL,
    `author`     VARCHAR(50),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
