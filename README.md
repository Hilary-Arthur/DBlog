# 星码记 (DBlog)

一个简洁的个人博客系统，基于 Spring Boot + Vue 3，支持文章发布、搜索、按月归档。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 4.0 + Spring Data JPA |
| 数据库 | MySQL 8.x |
| 缓存 | Redis (Lettuce) |
| 前端 | Vue 3 + Bootstrap 5（CDN） |
| 构建 | Maven |
| 语言 | Java 17 |

## 功能

- **博客展示** — 首页显示最新 6 篇文章，所有文章页支持分页浏览
- **文章详情** — Markdown 渲染，支持标题、代码块、表格、引用等
- **搜索** — 导航栏搜索框，支持标题和正文模糊搜索（防 SQL 注入）
- **按月归档** — 侧栏按月份筛选文章
- **用户认证** — 注册（含图形验证码）、登录、退出
- **点赞** — 基于 Redis Set 的点赞/取消
- **背景轮播** — 支持多张背景图片自动轮播

## 快速开始

### 前置条件

- JDK 17+
- MySQL 8.x
- Redis
- Maven 3.x（或使用项目自带的 `mvnw`）

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4;
```

### 2. 配置应用

```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

编辑 `application.properties`，填入 MySQL 和 Redis 连接信息。

### 3. 启动应用

```bash
./mvnw spring-boot:run
```

访问 http://localhost:8080/

首次启动自动创建示例用户 `hilary`（密码 `123456`）并生成示例文章。

## 页面

| 路径 | 说明 |
|------|------|
| `/` | 首页，最新 6 篇文章 |
| `/posts.html` | 所有文章，分页 + 按月筛选 |
| `/post.html?pid=N` | 文章详情 |

## API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/posts?page=N` | 分页获取文章（每页 6 篇） |
| GET | `/api/posts?month=YYYY-MM` | 按月筛选文章 |
| GET | `/api/posts/{pid}` | 获取文章详情 |
| GET | `/api/posts/search?q=关键词&page=N` | 搜索文章 |
| GET | `/api/posts/stats` | 文章统计（按月分布） |
| POST | `/api/posts` | 创建文章 |
| POST | `/api/posts/{pid}/like` | 点赞/取消 |
| POST | `/api/register` | 注册 |
| POST | `/api/login` | 登录 |
| GET | `/api/captcha` | 获取验证码 |

## 项目结构

```
src/main/resources/static/
├── index.html          # 首页
├── posts.html          # 所有文章
├── post.html           # 文章详情
├── imgs/               # 背景图片（1.png, 2.jpg...）
├── css/
│   ├── index.css       # 全局样式 + 导航栏 + 搜索框
│   ├── post.css        # 文章详情样式
│   └── post-card.css   # 文章卡片样式
└── js/
    ├── index.js        # 首页逻辑
    ├── posts.js        # 所有文章 + 搜索 + 归档筛选
    ├── post.js         # 文章详情 + Markdown 渲染
    ├── post-card.js    # 文章卡片组件
    ├── search.js       # 搜索框交互
    └── background.js   # 背景轮播
```
