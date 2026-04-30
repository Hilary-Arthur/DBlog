package com.example.dblog;

import com.example.dblog.entity.*;
import com.example.dblog.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final BasicInfoRepository basicInfoRepo;
    private final PostRepository postRepo;
    private final ReviewRepository reviewRepo;

    public DataInitializer(UserRepository userRepo, BasicInfoRepository basicInfoRepo,
                           PostRepository postRepo, ReviewRepository reviewRepo) {
        this.userRepo = userRepo;
        this.basicInfoRepo = basicInfoRepo;
        this.postRepo = postRepo;
        this.reviewRepo = reviewRepo;
    }

    @Override
    public void run(String... args) {
        // 每次启动同步：确保所有用户都有 basic_info 记录（uid 全局索引）
        syncBasicInfo();

        if (postRepo.count() >= 10) return;

        // 确保有用户
        User user = userRepo.findByAccount("hilary").orElseGet(() -> {
            User u = new User("hilary", sha256("123456"));
            userRepo.save(u);
            basicInfoRepo.save(new BasicInfo(u));
            return u;
        });

        // 10 篇模板文章
        List<String[]> samples = List.of(
            new String[]{"Spring Boot 4.0 新特性一览", "Spring Boot 4.0 正式发布，带来了许多令人兴奋的新特性。首先是全新的 AOT 编译支持，让应用启动速度提升了近 40%。其次是原生虚拟线程支持，在 Spring MVC 中可以直接启用虚拟线程处理请求，极大提升了高并发下的吞吐量。此外，新的 ProblemDetail 自动配置让 REST API 的错误处理更加规范。"},
            new String[]{"Java 虚拟线程实战指南", "Java 21 引入的虚拟线程（Virtual Threads）彻底改变了 Java 并发编程模型。传统的平台线程与 OS 线程一一对应，而虚拟线程由 JVM 调度，可以轻松创建数十万个。本文通过实际压测数据展示了虚拟线程在 Web 服务、数据库连接池、消息队列等场景下的表现，并分享了迁移过程中遇到的坑。"},
            new String[]{"REST API 设计最佳实践", "一个好的 API 设计能让前后端协作事半功倍。本文从 URL 命名规范、HTTP 方法选择、状态码使用、错误响应格式、版本管理、分页与排序、HATEOAS 等多个维度，总结了 RESTful API 设计的核心原则和常见反模式。文末附带了基于 Spring Boot 的完整示例代码。"},
            new String[]{"MySQL 索引优化笔记", "最近优化了一个慢查询问题，将一个 3 秒的查询降到了 50ms。核心思路是分析 EXPLAIN 执行计划，发现 MySQL 没有使用预期的索引。通过创建覆盖索引和调整 WHERE 条件顺序解决了问题。本文记录了整个排查过程，包括如何解读 EXPLAIN 输出、如何判断索引是否生效、以及常见的索引失效场景。"},
            new String[]{"Git 工作流：从混乱到清晰", "团队规模扩大后，Git 工作流的重要性愈发凸显。本文对比了 Git Flow、GitHub Flow、Trunk-Based Development 三种主流工作流的优劣，并结合实际项目经验给出了一个适合中小团队的简化方案。重点讨论了分支策略、Commit Message 规范、Code Review 流程和 CI/CD 集成。"},
            new String[]{"CSS Grid 布局完全指南", "CSS Grid 是现代 Web 布局的利器，但在实际项目中许多开发者仍然习惯使用 Flexbox。本文用丰富的图示和实例讲解了 Grid 的核心概念：网格容器、网格项、显式网格与隐式网格、轨道大小、网格区域命名等。特别适合已经会用 Flexbox 但还未深入 Grid 的开发者阅读。"},
            new String[]{"Docker 容器化部署踩坑记录", "将 Spring Boot 应用容器化部署到生产环境的过程中遇到了不少问题。包括时区设置、日志收集、JVM 内存限制、健康检查配置、优雅关闭等。本文逐一记录了问题和解决方案，并提供了一个可直接使用的 Dockerfile 模板和 docker-compose 配置。"},
            new String[]{"前后端分离项目中的认证方案", "JWT 还是 Session？这是一个经典问题。本文深入对比了两种方案在安全性、扩展性、性能方面的差异，并讨论了双 Token 刷新机制、黑名单策略、多设备登录管理等进阶话题。最终给出了一个综合方案：Access Token + Refresh Token + Redis 黑名单。"},
            new String[]{"代码审查清单", "Code Review 是团队协作中不可或缺的一环。但很多团队在做 Code Review 时缺乏系统性，往往只关注代码风格而忽略了更重要的方面。本文整理了一份实用的 Code Review 检查清单，涵盖安全性、性能、可维护性、测试覆盖、错误处理等维度。"},
            new String[]{"编程入门：从 Hello World 到独立项目", "很多初学者卡在「学完语法不知道能做什么」的阶段。本文以一个简单的博客系统为例，手把手教你从需求分析、数据库设计、后端 API、前端页面到最终部署的完整流程。不需要任何前置知识，跟着做就能完成一个可以展示的完整项目。"}
        );

        LocalDateTime[] times = {
            LocalDateTime.of(2026, 4, 29, 10, 0),
            LocalDateTime.of(2026, 4, 28, 15, 30),
            LocalDateTime.of(2026, 4, 27, 9, 0),
            LocalDateTime.of(2026, 4, 25, 14, 0),
            LocalDateTime.of(2026, 4, 23, 11, 0),
            LocalDateTime.of(2026, 4, 20, 8, 30),
            LocalDateTime.of(2026, 4, 18, 16, 0),
            LocalDateTime.of(2026, 4, 15, 10, 30),
            LocalDateTime.of(2026, 4, 12, 13, 0),
            LocalDateTime.of(2026, 4, 10, 9, 0)
        };

        for (int i = 0; i < samples.size(); i++) {
            String[] s = samples.get(i);
            Post post = new Post(s[0], s[1], user);
            post.setCreatedAt(times[i]);
            postRepo.save(post);
            Review review = new Review(post);
            review.setStatus("APPROVED");
            reviewRepo.save(review);
        }
    }

    /** 确保每个 user 都有对应的 basic_info 记录，uid 作为全局索引 */
    private void syncBasicInfo() {
        List<User> users = userRepo.findAll();
        for (User u : users) {
            if (!basicInfoRepo.existsById(u.getUid())) {
                basicInfoRepo.save(new BasicInfo(u));
            }
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
