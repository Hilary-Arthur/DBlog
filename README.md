# DBlog

一个基于 Spring Boot 的博客系统，支持用户注册/登录、写博客、后台审核等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 4.0.6 + Spring MVC |
| ORM | Spring Data JPA (Hibernate) |
| 数据库 | MySQL 8.x |
| 连接池 | HikariCP |
| 前端 | 原生 HTML/CSS/JS（无框架） |
| 构建 | Maven |
| 语言 | Java 17 |

## 功能

- **用户认证** — 注册（含图形验证码）、登录、退出，密码 SHA-256 加密存储
- **博客管理** — 创建文章（纯文本编辑器），首页展示已审核通过的文章
- **后台审核** — 管理员可对待审文章进行通过/驳回操作
- **个人主页** — 查看/编辑个人资料（昵称、手机、邮箱），查看自己的文章及审核状态
- **角色控制** — 普通用户 (user) 和管理员 (admin) 两种角色

## 数据库表结构

| 表 | 说明 |
|------|------|
| `user` | 用户表（uid, account, password, role） |
| `basic_info` | 用户扩展信息（uid, uname, tel, email），一对一关联 user |
| `admin` | 管理员表（aid, account, password），独立于 user 表 |
| `post` | 博客文章（pid, title, content, author_id, created_at） |
| `review` | 审核记录（rid, pid, status, reviewer_uid, reviewed_at），审核人关联 admin 表 |

完整 DDL 见 [schema.sql](schema.sql)。

## 快速开始

### 前置条件

- JDK 17+
- MySQL 8.x
- Maven 3.x（或使用项目自带的 `mvnw`）

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4;
```

### 2. 配置数据库连接

复制模板文件并填入真实配置：

```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

编辑 `src/main/resources/application.properties`，将用户名和密码改为你的 MySQL 凭据。此文件已在 `.gitignore` 中，不会被提交到 Git。

### 3. 启动应用

```bash
./mvnw spring-boot:run
```

访问 http://localhost:8080/

### 4. 创建管理员账号

管理员系统使用独立的 `admin` 表，在 MySQL 中手动执行：

```sql
-- 密码为 SHA-256 哈希值，下面的值是 "admin123" 的哈希
INSERT INTO admin (account, password) VALUES ('admin', SHA2('admin123', 256));
```

之后在首页底部找到隐藏的管理入口（页脚灰色小圆点），点击进入管理员登录。

## API 概览

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/captcha` | 获取图形验证码 |
| POST | `/api/register` | 注册（account, password, captcha） |
| POST | `/api/login` | 登录（account, password） |
| GET | `/api/user/me` | 获取当前登录用户 |
| POST | `/api/logout` | 退出登录 |

### 个人中心

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/profile` | 获取个人资料 |
| PUT | `/api/user/profile` | 修改个人资料（uname, tel, email） |
| GET | `/api/user/posts` | 获取我的文章及审核状态 |

### 博客文章

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/posts` | 创建文章（title, content） |
| GET | `/api/posts` | 获取所有已审核通过的文章 |

### 管理员审核

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/pending` | 获取待审文章列表 |
| PUT | `/api/admin/posts/{pid}/approve` | 通过审核 |
| PUT | `/api/admin/posts/{pid}/reject` | 驳回文章 |

## 页面路由

| 路径 | 说明 |
|------|------|
| `/` (`index.html`) | 博客首页，展示已审核通过的文章 |
| `/editor.html` | 写博客页面（需登录） |
| `/profile.html` | 个人主页（需登录） |
| `/admin.html` | 审核管理页面（需管理员角色） |

## 项目结构

```
src/main/java/com/example/dblog/
├── DBlogApplication.java
├── entity/
│   ├── User.java          # 用户实体
│   ├── BasicInfo.java     # 用户扩展信息
│   ├── Post.java          # 博客文章
│   └── Review.java        # 审核记录
├── repository/
│   ├── UserRepository.java
│   ├── BasicInfoRepository.java
│   ├── PostRepository.java
│   └── ReviewRepository.java
└── controller/
    ├── AuthController.java    # 认证 API
    ├── ProfileController.java # 个人中心 API
    ├── PostController.java    # 文章 API
    ├── AdminController.java   # 审核管理 API
    └── CaptchaUtil.java       # 验证码生成工具

src/main/resources/static/
├── index.html      # 首页
├── editor.html     # 写博客
├── profile.html    # 个人主页
└── admin.html      # 审核管理
```
