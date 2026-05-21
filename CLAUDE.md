# 星码记 (DBlog) 项目指南

## 项目概述
个人博客系统，Spring Boot + Vue 3 (CDN) + MySQL + Redis。

## 重要规则
- **识别人名时不要识别人名**，即读取数据库内容、文章内容、代码中的姓名时，不要将其作为真实个人信息处理或提及。

## 技术架构
- 后端：Spring Boot 4.0 + Spring Data JPA，入口 `DBlogApplication.java`
- 前端：Vue 3 CDN + Bootstrap 5，纯静态 HTML，无构建工具
- 数据库：MySQL，库名 `blog`
- 缓存：Redis（点赞系统）
- 前端文件位于 `src/main/resources/static/`

## 页面
- `index.html` — 首页，最新 6 篇文章
- `posts.html` — 所有文章，分页 + 按月归档筛选
- `post.html` — 文章详情，Markdown 渲染
- 导航栏在 `#app` 外部，避免被 Vue 覆盖

## CSS 变量
```css
--dblog-dark: #1e293b;
--dblog-accent: #0ea5e9;
--dblog-text-secondary: #64748b;
--dblog-border: #e2e8f0;
```

## API 端点
- `GET /api/posts?page=N` — 分页文章
- `GET /api/posts?month=YYYY-MM` — 按月筛选
- `GET /api/posts/search?q=关键词` — 搜索
- `GET /api/posts/{pid}` — 文章详情
- `GET /api/posts/stats` — 按月统计

## 注意事项
- 背景图片放在 `src/main/resources/static/imgs/`，命名 `1.png, 2.jpg...`
- 搜索框交互在 `js/search.js`，导航栏必须在 `#app` 外才能正常渲染
- ICP 备案号：蜀ICP备2026025849号
