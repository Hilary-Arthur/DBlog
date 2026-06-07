-- ═══════════════════════════════════════════════════
-- 星码记 — 完整数据库初始化脚本
-- 使用方法: mysql -u root -p < init.sql
-- ═══════════════════════════════════════════════════

-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4;
USE blog;

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS `post`;

-- 创建表
CREATE TABLE `post` (
    `pid`        BIGINT AUTO_INCREMENT PRIMARY KEY,
    `hash`       VARCHAR(16) UNIQUE,
    `title`      VARCHAR(200) NOT NULL,
    `content`    TEXT         NOT NULL,
    `author`     VARCHAR(50),
    `featured`   TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 清空旧数据（如果有）
TRUNCATE TABLE `post`;

-- 插入文章数据
INSERT INTO `post` (`hash`, `title`, `content`, `author`, `featured`, `created_at`) VALUES
('778fe230', '基于 C++ 的 OSPF 路由协议模拟实现',
'## 项目简介

OSPF（Open Shortest Path First）是一种广泛使用的内部网关路由协议。本项目使用 **C++17** 从零实现了一个 OSPF 协议的教学模拟程序，通过终端交互式菜单让用户动态创建路由器、配置邻居关系、触发 Hello 报文发现流程，并观察路由器之间的连通性状态。

## 技术栈

- **语言**：C++17
- **编译器**：MSYS2/MinGW-w64 g++
- **构建工具**：Makefile
- **UI**：Windows 终端（ANSI 转义码 + _getch() 方向键捕获）
- **数据持久化**：手动 JSON 序列化/解析，零第三方依赖

## 核心功能

1. **路由器创建与管理** — 支持按名称操作，内部采用哈希表 + 线性探测法注册，ID 永不复用
2. **邻居关系配置** — 支持双向自动绑定邻接表
3. **路由器批量启停** — 状态持久化到 JSON 文件
4. **OSPF Hello 报文收发** — 自动邻居发现机制
5. **连通性测试** — 向邻居发送 Hello 并记录结果
6. **独立报文日志** — 每个路由器拥有独立的 JSON 格式日志

## 架构设计

项目采用模块化分层设计：

- `bg_builder` — 终端 UI 与交互逻辑（主菜单、子菜单）
- `router` — 路由器生命周期管理与 JSON 持久化
- `ospf` — OSPF Hello 协议实现
- `io_manager` — 通用文件读写与报文日志记录
- `time_recorder` — 超时计时功能

## 实现亮点

- 设计了 **物理链路**（静态邻接表）与 **逻辑链路**（Hello 报文动态建立）的两层模型，准确反映 OSPF 邻居建立机制
- 路由器注册借鉴数据库 AUTO_INCREMENT 思想，保证 ID 永不复用
- Hello 报文支持超时重试机制，模拟真实网络异步行为
- 全程无第三方依赖，手动实现 JSON 序列化

## 项目地址

[https://gitee.com/rao-jiarui/ospf-protocol-implementation](https://gitee.com/rao-jiarui/ospf-protocol-implementation)

> 项目仍在持续开发中，后续计划实现 LSA 链路状态通告、Dijkstra 最短路径计算和路由表生成。',
'', 1, '2025-04-15 10:00:00'),

('9415d72c', 'Python 从入门到进阶：系统性学习笔记',
'## 项目简介

这是一个系统性的 **Python 学习笔记** 仓库，按照三个阶段循序渐进：基础语法 → 面向对象 → 进阶编程。涵盖了从变量定义到 Socket 网络编程的完整学习路径。

## 学习路线

### 第一阶段：基础语法（11章）

- 字面量、变量、数据类型转换、字符串格式化
- if 判断、for/while 循环、range、break/continue
- 函数定义、返回值、递归、变量作用域
- 列表、元组、字典、集合等数据容器
- 文件操作、模块与包、异常处理
- JSON 数据处理与 pyecharts 数据可视化
- 快速排序算法与疫情地图构建

### 第二阶段：面向对象（2章）

- 类与对象、继承、封装、多态、构造方法
- 类型注解与 Union 联合类型
- 使用 pymysql 操作 MySQL 数据库（CRUD + 分页查询）

### 第三阶段：进阶编程（1章）

- Socket 网络编程（服务端与客户端通信）
- 闭包与高阶函数

## 实践项目亮点

- **COVID-19 疫情数据可视化** — 使用 pyecharts 绘制折线图和疫情地图
- **GDP 数据柱状图** — 带时间线的动态可视化
- **Socket 聊天程序** — 基于 TCP 的双端通信
- **MySQL 分页查询** — 完整的数据库 CRUD 操作

## 技术栈

Python 3 · pyecharts · pymysql · socket · JSON

## 项目地址

[https://gitee.com/rao-jiarui/python_-learning](https://gitee.com/rao-jiarui/python_-learning)',
'', 1, '2025-03-20 10:00:00'),

('df9d9020', 'JavaWeb 课程实践：从基础语法到完整 Web 应用',
'## 项目简介

这是学校课程 JavaWeb 课程的教学实践项目集合，通过 **5 个递进式子模块**，带领学习者从 Java 基础语法一路走到完整的 Web 应用开发。

## 子模块概览

### 1. Java 基础入门

Student、Course 等面向对象编程练习，掌握 Java 语法基础。

### 2. Java IO 操作

实现了一个网络图片下载与管理系统，支持 URL 下载、图片浏览和搜索功能。

### 3. 手写迷你 Web 服务器

从零实现 **MiniWebSvr**，包含：
- HTTP 请求解析器
- URL 路由机制
- 线程池并发处理
- 静态文件服务

### 4. 在线留言板

基于 Servlet 的留言板应用，支持留言提交、历史记录持久化（文件存储）和展示。

### 5. 图书管理系统（综合项目）

最完整的综合项目，实现了：
- 用户登录认证 + 验证码
- 权限拦截器（AuthFilter）
- 图书 CRUD 增删改查
- 分页功能 + 图片上传
- 前后端分离（JSON 数据格式）
- Ant 构建打包（WAR）

## 技术栈

- **语言**：Java 17
- **服务器**：Apache Tomcat 10.1.48（Jakarta Servlet API）
- **数据库**：MySQL + Apache Commons DBCP2 连接池
- **前端**：HTML/CSS + jQuery 3.7.1 + Bootstrap
- **构建**：Apache Ant

## 架构设计

采用经典 JavaWeb 分层架构：`auth` 包处理认证授权，`books` 包实现业务逻辑，`repo` 包封装数据库访问层（单例模式），`utils` 包提供工具类。Servlet 通过 `@WebServlet` 注解映射 URL。

## 项目地址

[https://gitee.com/rao-jiarui/java-web2025](https://gitee.com/rao-jiarui/java-web2025)',
'', 1, '2025-05-01 10:00:00'),

('1d0c0228', 'IndexAge：一个零依赖的浏览器新标签页扩展',
'## 项目简介

IndexAge 是一个纯前端的浏览器新标签页/起始页扩展，旨在替代浏览器默认的新标签页。核心理念是 **本地优先、零依赖、无需联网**，适合作为个人定制化的浏览器主页。

## 技术栈

- **前端**：HTML5 + CSS3 + 原生 JavaScript（Vanilla JS）
- **扩展标准**：Chrome/Edge Manifest V3
- **样式**：CSS Grid 布局、CSS 变量、backdrop-filter 毛玻璃效果

## 核心功能

1. **实时时钟** — 页面右上角显示当前时间（精确到秒）和日期（含星期），每秒自动更新
2. **智能问候语** — 根据时间段（凌晨/上午/下午/晚上）自动切换问候文案
3. **多引擎搜索** — 支持 Google、Bing、DuckDuckGo 三种搜索引擎切换
4. **键盘快捷键** — `/` 键快速聚焦搜索框，`Esc` 键清空输入
5. **常用网站导航** — 内置抖音、学习通、GitHub、bilibili 快捷入口
6. **云端同步** — 提供批处理脚本一键从 Gitee 拉取最新配置

## 项目结构

项目极其简洁，仅 6 个核心文件：

- `manifest.json` — 扩展配置（Manifest V3）
- `index.html` — 页面结构
- `style.css` — 奶油淡黄配色，响应式布局（820px 断点适配移动端）
- `script.js` — 全部交互逻辑

## 设计亮点

- **零网络依赖** — 所有资源本地化，打开即用
- **毛玻璃导航栏** — 使用 backdrop-filter 实现现代感 UI
- **一键同步** — 双击 bat 脚本即可多设备同步配置

## 项目地址

[https://gitee.com/rao-jiarui/indexage](https://gitee.com/rao-jiarui/indexage)',
'', 1, '2025-04-28 10:00:00'),

('718ec74f', '待办清单：一个轻量级 Python 桌面 Todo 工具',
'## 项目简介

这是一个使用 **Python + Tkinter** 开发的轻量级桌面待办事项管理工具。半透明的小窗口可以常驻桌面角落，随时查看和更新任务进度，适合学生和办公人员日常使用。

## 技术栈

- **语言**：Python 3
- **GUI 框架**：Tkinter（标准库自带）
- **打包工具**：PyInstaller（打包为独立 .exe）
- **数据存储**：JSON 文件（todo.json）

## 核心功能

- **双击切换状态** — 双击某项标记为完成/未完成，已完成项显示灰色
- **添加新事项** — 输入框回车或点击"添加"按钮
- **删除选中项** — 删除当前选中的单条待办
- **清除已完成** — 一键清除所有已标记完成的事项
- **窗口置顶** — 始终显示在最上层，透明度 85%
- **数据持久化** — 自动保存到 JSON 文件，重启不丢失

## 架构设计

核心仅一个 `todo.py` 文件（约 150 行），采用单文件架构：

1. 入口处根据是否为打包环境确定数据文件路径
2. 加载或创建默认待办数据
3. Tkinter 构建 UI：顶部标题 → 中部列表区（含滚动条）→ 底部输入区和操作按钮
4. 功能函数：`refresh_list()`、`toggle_done()`、`add_todo()`、`delete_todo()`、`clear_done()`

## 使用方式

- **源码运行**：`python todo.py`
- **打包版本**：直接运行 `dist/` 目录下的 .exe 文件（约 12MB）

## 项目地址

[https://gitee.com/rao-jiarui/to-do-list](https://gitee.com/rao-jiarui/to-do-list)',
'', 1, '2025-03-10 10:00:00'),

('9287f5a8', 'Model Switcher：Claude Code 多模型一键切换工具',
'## 项目简介

在使用 Claude Code CLI 时，经常需要在不同的 AI 模型之间切换（如 GLM、DeepSeek 等）。本项目提供了一个 **图形化模型切换器**，在启动 Claude Code 之前弹出选择窗口，一键切换底层模型配置。

## 技术栈

- **核心逻辑**：Python + tkinter GUI
- **启动脚本**：Windows CMD 批处理
- **目标平台**：Windows

## 核心功能

1. **动态配置扫描** — 自动扫描 `.claude` 目录下所有 `settings-*.json` 文件，无需硬编码模型列表
2. **GUI 选择器** — tkinter 窗口展示所有可用模型配置，单选按钮 + 确认切换
3. **默认回退** — 关闭窗口（未点确认）时自动回退到 DeepSeek 配置
4. **多实例防护** — CMD 脚本通过 `tasklist` 检测是否已有 claude.exe 运行，避免干扰
5. **启动集成** — 自动插入模型切换步骤，用户无需手动操作

## 使用流程

```
运行 claude-1.0.cmd
  → 检测无 Claude 进程
  → 调用 switcher.py 弹出 GUI
  → 用户选择模型
  → 配置文件复制到 settings.json
  → 启动 Claude Code
```

## 项目结构

```
model-switcher/
├── bin/switcher.py          # 核心：Python GUI 切换器
├── claude-release/
│   ├── claude-1.0.cmd       # 生产用启动脚本
│   └── claude-tmp.cmd       # 简化版启动脚本
└── skills/dev-log.md        # 开发操作日志
```

## 设计亮点

- **零配置** — 新增模型只需添加 `settings-*.json` 文件，无需修改代码
- **进程安全** — 多实例检测防止配置被意外覆盖
- **无感切换** — 整个过程对用户透明，选择后自动启动

## 项目地址

[https://gitee.com/rao-jiarui/model-switcher](https://gitee.com/rao-jiarui/model-switcher)',
'', 1, '2025-05-10 10:00:00');

-- 验证
SELECT pid, hash, title, featured FROM post;
