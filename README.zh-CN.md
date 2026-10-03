# pootOS

一个面向自主智能体的智能体操作系统（Agent OS）——带资源租约的持久化 Actor 内核、以内容寻址的
**上下文图**取代逐任务的聊天、注意力分区，以及可在运行时重新配置一切的 System Agent。后端：**Java 25
(Elegant Objects)**；前端：**React + TypeScript**。

> 语言：**en** ([README.md](README.md)) · [ru](README.ru.md) · **zh-CN**（本文件）

## 状态

早期开发（0.1.0 之前）。**目前尚无可运行的应用**——基础已就位（上下文图原语、沙箱 SPI、执行器
SPI、内核资源租约、CI 安全门禁）。见 [`docs/ROADMAP.md`](docs/ROADMAP.md)。

## 快速开始（从源码构建）

需要 **JDK 25** 与 **Maven**（稍后运行智能体时需要 Docker）。

```bash
git clone https://github.com/ArtemGet/pootOS.git
cd pootOS
# Windows PowerShell:
$env:JAVA_HOME = "C:\Users\<you>\.jdks\temurin-25"
mvn --errors --batch-mode clean install -Pqulice -Pjtcop
```

可运行的 `pootos` 应用尚未提供（目标：0.1.0，Web 优先）。

## 文档

- 架构：[ru](docs/ARCHITECTURE.md) · [en](docs/ARCHITECTURE.en.md) · **zh-CN**（本文件）
- [安全策略](docs/SECURITY.md) · [并行工作区](docs/PARALLELISM.md)
- [ADR](docs/adr/) · [路线图](docs/ROADMAP.md) · [贡献者契约](AGENTS.md)

## 模块

`pootos-context` · `pootos-agent` · `pootos-sandbox` · `pootos-kernel`（后续更多）。

## 许可证

MIT —— 见 [LICENSE](LICENSE)。
