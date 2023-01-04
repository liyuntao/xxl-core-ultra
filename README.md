xxl-core-ultra
---

## Introduction
[源项目地址 xxl-job](https://github.com/xuxueli/xxl-job)的 Java 执行器(xxl-job-core)修改优化版，兼容原 Admin-UI
运行环境为 JDK 17 + spring boot 3.X

有需要自行修改取用，建议内网发布使用

## feature && 变更

Release Note Part I：

* 面向容器运行的微服务场景的，安全提升、依赖与功能删减
  * 移除 glue/脚本执行方式
  * 移除 groovy 语言的传递依赖
  * 移除大量日志本地容器的落盘逻辑
* 大幅降低基础资源占用
  * 移除微服务场景无需的日志任务与相关常驻线程
  * 移除 EmbedServer 原逻辑导致的额外常驻线程占用
  * 使用非阻塞写法调整 handler 逻辑，移除 embedHttpServer 占用的最大数量为 200 的常驻线程池
* 使用 netty-bom 管理 netty 依赖，减少无用传递依赖。若干依赖组件的版本升级更新。

Release Note Part II：

* 在 Part I 版本优化与资源精简的基础上，重写整个执行器内核
* 梳理线程结构，以线程池方式支撑全部任务调度; 从 NettyHandler 到 RPC 通信全链路 async-IO
* RPC 通信变更为 JDK 11 new HttpClient,  单例复用资源（和里面的线程池 CommonPool）全异步执行
* 后台注册、清理等任务由专有的 SchedulerModule 集中处理
* 非侵入的 MDC traceLog 跟踪，以 jobHandlerName + triggerId 为格式；任务执行完成自动清除 MDC
* 非侵入的监控支持，自动监控任务耗时(分布); 在 Actuator 脚手架下可以自动注入任务耗时监控至 PrometheusMeterRegistry
* 全局梳理，规范化执行器各模块线程命名，便于跟进执行细节与日志查询
* 提供 starter 简化配置，无需写配置代码。yaml 配置项可以被 IDE 解析检查(拼写错会标黄警告)，支持 Intellij IDEA 的 hint 检查