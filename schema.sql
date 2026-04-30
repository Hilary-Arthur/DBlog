-- DBlog 数据库表结构
-- 数据库: blog (需先创建: CREATE DATABASE blog DEFAULT CHARACTER SET utf8mb4;)

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `uid`       BIGINT AUTO_INCREMENT PRIMARY KEY,
    `account`   VARCHAR(50)  NOT NULL UNIQUE,
    `password`  VARCHAR(128) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户扩展信息表（一对一关联 user）
CREATE TABLE IF NOT EXISTS `basic_info` (
    `uid`   BIGINT PRIMARY KEY,
    `uname` VARCHAR(30),
    `tel`   VARCHAR(20),
    `email` VARCHAR(100),
    FOREIGN KEY (`uid`) REFERENCES `user`(`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 博客文章表
CREATE TABLE IF NOT EXISTS `post` (
    `pid`        BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title`      VARCHAR(200) NOT NULL,
    `content`    TEXT         NOT NULL,
    `author_id`  BIGINT       NOT NULL,
    `created_at` DATETIME,
    `like_count` INT          NOT NULL DEFAULT 0,
    FOREIGN KEY (`author_id`) REFERENCES `user`(`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 管理员表（独立于 user 表）
CREATE TABLE IF NOT EXISTS `admin` (
    `aid`      BIGINT AUTO_INCREMENT PRIMARY KEY,
    `account`  VARCHAR(50)  NOT NULL UNIQUE,
    `password` VARCHAR(128) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 审核记录表（独立于文章表）
CREATE TABLE IF NOT EXISTS `review` (
    `rid`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `pid`          BIGINT       NOT NULL,
    `status`       VARCHAR(12)  NOT NULL DEFAULT 'PENDING',
    `reviewer_uid` BIGINT,
    `reviewed_at`  DATETIME,
    FOREIGN KEY (`pid`)          REFERENCES `post`(`pid`),
    FOREIGN KEY (`reviewer_uid`) REFERENCES `admin`(`aid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
