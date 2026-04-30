# DBlog

一个基于 Spring Boot 的博客系统，支持用户注册/登录、写博客、点赞、后台审核、维护模式等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 4.0.6 + Spring MVC |
| ORM | Spring Data JPA (Hibernate) |
| 数据库 | MySQL 8.x |
| 缓存 | Redis (Lettuce) |
| 连接池 | HikariCP |
| 前端 | Vue 3 + Bootstrap 5（CDN） |
| 构建 | Maven |
| 语言 | Java 17 |

## 功能

- **用户认证** — 注册（含图形验证码）、登录、退出，密码 SHA-256 加密存储
- **博客管理** — 创建文章（纯文本编辑器），首页分页展示已审核通过的文章（每页 6 篇）
- **点赞系统** — 基于 Redis Set 的点赞/取消，每分钟自动同步到 MySQL
- **后台审核** — 管理员可对待审文章进行通过/驳回操作（管理员使用独立 `admin` 表，与用户体系分离）
- **个人主页** — 查看/编辑个人资料（昵称、手机、邮箱），查看自己的文章及审核状态，支持批量删除文章（需密码确认）
- **维护模式** — 管理员一键开启全站维护页面，基于 Redis 状态 + 全局拦截器，开启后所有公开页面自动跳转维护页
- **数据初始化** — 首次启动时自动生成 10 篇示例文章，每次启动自动同步 `basic_info` 记录
- **文章归档统计** — 按月统计已发布文章数量

## 数据库表结构

| 表 | 说明 |
|------|------|
| `user` | 用户表（uid, account, password） |
| `basic_info` | 用户扩展信息（uid, uname, tel, email），一对一关联 user |
| `admin` | 管理员表（aid, account, password），独立于 user 表 |
| `post` | 博客文章（pid, title, content, author_id, created_at, like_count） |
| `review` | 审核记录（rid, pid, status, reviewer_uid, reviewed_at），审核人关联 admin 表 |

完整 DDL 见 [schema.sql](schema.sql)。

## 快速开始

### 前置条件

- JDK 17+
- MySQL 8.x
- Redis（推荐配置自动启停，见下文）
- Maven 3.x（或使用项目自带的 `mvnw`）

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4;
```

### 2. 配置应用

复制模板文件并填入真实配置：

```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

编辑 `src/main/resources/application.properties`，修改 MySQL 凭据。此文件已在 `.gitignore` 中。

### 3. Redis 自动启停（可选）

项目内置了 Redis 进程管理器，可在应用启动时自动拉起 Redis，关闭时自动停止，平时零资源占用。

```properties
# 开启自动启动（false 则手动管理）
redis.server.auto-start=true
# redis-server 路径（已在 PATH 中可简写为 redis-server）
redis.server.command=E:\\Redis\\Redis-8.2.5-Windows-x64-msys2-with-Service\\redis-server.exe
```

如不使用此功能，需手动启动 Redis 后再启动应用。

### 4. 启动应用

```bash
./mvnw spring-boot:run
```

访问 http://localhost:8080/

首次启动时，应用会自动创建名为 `hilary` 的用户（密码 `123456`）并生成 10 篇示例文章。

### 5. 创建管理员账号

管理员系统使用独立的 `admin` 表，在 MySQL 中手动执行：

```sql
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

### 管理员

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/login` | 管理员登录 |
| GET | `/api/admin/me` | 获取当前登录管理员 |
| POST | `/api/admin/logout` | 管理员退出 |
| GET | `/api/admin/pending` | 获取待审文章列表 |
| PUT | `/api/admin/posts/{pid}/approve` | 通过审核 |
| PUT | `/api/admin/posts/{pid}/reject` | 驳回文章 |
| GET | `/api/admin/maintenance` | 查询维护模式状态 |
| PUT | `/api/admin/maintenance` | 切换维护模式 |

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
| GET | `/api/posts` | 分页获取已审核通过的文章（`?page=1`，每页 6 篇） |
| POST | `/api/posts/{pid}/like` | 切换点赞（需登录） |
| GET | `/api/posts/stats` | 获取文章统计（总数 + 按月分布） |
| DELETE | `/api/posts/batch` | 批量删除自己的文章（需密码确认） |

## 页面路由

| 路径 | 说明 |
|------|------|
| `/` (`index.html`) | 博客首页，分页展示已审核通过的文章 |
| `/editor.html` | 写博客页面（需登录） |
| `/profile.html` | 个人主页（需登录） |
| `/admin.html` | 审核管理页面（需管理员角色） |
| `/maintenance.html` | 维护模式页面（含隐藏管理入口） |

## 项目结构

```
src/main/java/com/example/dblog/
├── DBlogApplication.java
├── DataInitializer.java          # 数据初始化（示例文章、basic_info 同步）
├── entity/
│   ├── User.java                 # 用户实体
│   ├── BasicInfo.java            # 用户扩展信息
│   ├── Admin.java                # 管理员实体
│   ├── Post.java                 # 博客文章
│   └── Review.java               # 审核记录
├── repository/
│   ├── UserRepository.java
│   ├── BasicInfoRepository.java
│   ├── AdminRepository.java
│   ├── PostRepository.java
│   └── ReviewRepository.java
├── service/
│   └── LikeService.java          # 点赞服务（Redis Set + MySQL 同步）
├── controller/
│   ├── AuthController.java       # 用户认证 API
│   ├── AdminController.java      # 管理员认证 + 审核 + 维护模式 API
│   ├── ProfileController.java    # 个人中心 API
│   ├── PostController.java       # 文章 API
│   └── CaptchaUtil.java          # 验证码生成工具
├── config/
│   ├── WebConfig.java            # 拦截器注册
│   └── RedisProcessManager.java  # Redis 进程自动管理
└── interceptor/
    └── MaintenanceInterceptor.java  # 维护模式拦截 + 缓存控制

src/main/resources/static/
├── index.html          # 首页
├── editor.html         # 写博客
├── profile.html        # 个人主页
├── admin.html          # 审核管理
├── maintenance.html    # 维护页面
├── css/
│   ├── index.css
│   ├── editor.css
│   ├── profile.css
│   ├── post-card.css
│   └── maintenance.css
└── js/
    ├── index.js
    ├── editor.js
    ├── profile.js
    ├── post-card.js
    └── maintenance.js
```
