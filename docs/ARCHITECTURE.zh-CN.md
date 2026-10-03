# pootOS — 架构

> Agent Operating System：面向智能体的沙箱，带调度器与资源租约的内核，用上下文图取代
> per-task 聊天，以及一个在运行期管理一切的系统智能体。
>
> 本文档确定架构、技术与功能。后端语言为 Java 25（Elegant Objects），前端为
> React + TypeScript。首要目标是在 Windows 和 macOS 上（Linux 稍后）提供一个安全
> 应用，具备最小功能：安全运行智能体并解决一个简单任务（EO PR 审查）。
>
> **文档语言：** ru（本文件为规范）· en（`ARCHITECTURE.en.md`）·
> zh-CN（`ARCHITECTURE.zh-CN.md`）。翻译由独立智能体维护，时效性由审计智能体
> 定期校验（见 §22）。
>
> zh-CN (本文件) · [ru](ARCHITECTURE.md) · [en](ARCHITECTURE.en.md)

---

## 0. Product Vision

格式来自 EO 流程（actors、features、quality、constraints）。**Thinking** 阶段：规格。

### 0.1 参与者

1. **Owner** — 人类操作者（ArtemGet）。
2. **System Agent** — 元智能体；通过主聊天与 Owner 对话的唯一对象。
3. **Worker Agent** — 任务执行者（MVP 中为审查智能体）。
4. **External systems** — GitHub（webhook/API）、LLM 提供方、MCP 服务器。

### 0.2 Owner ↔ 系统交互模型

- **一个主聊天——永久存在。** 它不针对某个任务；它关于管理操作系统。Owner 通过它：
  - 下达任务（“需要做什么”）并交给 **System Agent**；
  - 给出修改/更正；
  - 阅读 System Agent 的报告。
- **System Agent 接受指令并向下分发，不阻塞自身循环。** 系统智能体是协调者：
  创建 `Task` 节点，挑选智能体/模板，授予资源，跟踪进度。当智能体工作时，它保持响应。
- **所有工作不在聊天里，而在画布上**（任务、产物、智能体）。
- 聊天的功能被严格限制：启动、指令/修改、报告。没有“按任务的聊天”。

### 0.3 画布与注意力区域

画布是主要工作空间。按人对注意力的需求分为三个区域：

| 区域 | 含义 | 示例 |
|---|---|---|
| 🔴 **RED** | 需要人的决策；该分支的工作被阻塞 | 请求访问令牌/密钥；挂载请求；需求含糊；预算耗尽；疑似不可逆操作 |
| 🟡 **YELLOW** | 人最好看一下，但没有阻塞 | 结果处于边界、多个方案、隔离退化 |
| 🟢 **GREEN** | 智能体自行处理，无需干预 | 正常执行 |

- **任务在区域间移动**，随状态变化（调度器/智能体移动卡片）。
- 区域中是**卡片**。可以从卡片深入：看到智能体在做什么、它们的步骤、产物，
  并**直接写入卡片**（修改/澄清）。

### 0.4 卡片 ≠ 智能体

- **卡片**是任务或产物。一个卡片上可以有**多个智能体**工作
  （例如执行者 + 审查者 + 修复者）。
- **智能体**是独立实体，拥有自己的：技能、MCP 访问、shell 访问、一组已挂载的文件夹、
  模型、模板。
- 独立的视图：**“正在工作”**和**“全部智能体”**。

### 0.5 按参与者的功能（MVP，每个 3–4 项为限）

- **Owner：** 通过聊天下达任务；查看区域/卡片/智能体；回应 RED/YELLOW；查看资源并做
  决策（优化 / 停止 / 重启卡住的智能体）。
- **System Agent：** 创建/分发任务；添加提供方、工具、MCP、智能体模板；配置
  **自身以及 pootOS 应用本身（通过智能体）**；更改策略——全部**无需重启进程**。
- **Worker Agent：** 在沙箱中启动；读写上下文图；以**不同模型**派生子智能体；
  请求挂载；产出产物与教训。
- **审查智能体（MVP）：** PR webhook → EO 审查 → 向 GitHub 发布审查与标签。

### 0.6 质量要求（可度量）

- 智能体**不能**读写其 sandbox-root 之外的主机（负向测试）。
- 任何两个智能体不会同时持有一个资源（`gpu:0`、`port:N`、`llm:rate:*`）。
- webhook → 审查启动 < 30 秒；小 PR 的审查 < 10 分钟。
- 智能体崩溃不阻塞其他智能体；watchdog 在 TTL 内释放租约。
- 配置更改（新提供方）无需进程重启即可生效。
- 一个智能体 = 一个 durable actor；数百个智能体在 virtual threads 上并行。

### 0.7 约束

- 后端 Java 25、Maven、EO 风格、门禁 Qulice 0.36 + jtcop + JaCoCo/PIT。
- 隔离——Docker + gVisor（`runsc`）。Windows 需要 Docker 的 WSL2 后端。
- Web UI——React + TS；桌面端（Kotlin Multiplatform/Compose）——稍后。
- GitHub——单一账号 ⇒ 审查结论以**标签**（`approved`）表达，而非 Approve。

---

## 1. 需求（R1–R18）

| # | 需求 | 层 / 方案 | MVP |
|---|---|---|---|
| R1 | 真实的文件系统分段：智能体在其他 root 下运行，无法触及数据 | 沙箱（Docker/gVisor）、独立 root、仅显式挂载 | ✅ |
| R2 | System Agent 添加提供方/工具/MCP | System Agent + 配置文件 | ✅ |
| R3 | 按技能/工具自动配置智能体，不同提供方 | Agent Templates + Provider SPI | ✅ |
| R4 | UI 中的监控 | 画布（区域/卡片）+ 资源仪表盘 | ✅ |
| R5 | 对系统智能体的动态控制 | 配置文件 + hot-reload | ✅ |
| R6 | 资源/超时；并行智能体不死锁 | Resource Arbiter (leases) + Watchdog + Budgets | ✅ |
| R7 | 从起点即工作图 | Context Graph: `Task` + DAG 边 | ✅ |
| R8 | 上下文是图不是聊天；智能体修复另一个 | Context Graph + repair-flow | ✅ 基础 |
| R9 | 按层级处理 human 任务：存储、绕过、继续 | RED/YELLOW 区域 + `HumanQuestion` L0–L3 | ✅ |
| R10 | 跨会话/项目的记忆压缩；问题日志 | `Summary` + `Lesson` | ✅ |
| R11 | 机器之间的上下文同步 | Append-only 日志 + content-addressing | ⏳ 设计 |
| R12 | 工作既非聊天，也非“一个项目——一个目录” | 画布 + `Project` 作为一组挂载 | ✅ |
| R13 | 并行智能体不阻塞主循环 | Virtual threads + scheduler；没有主聊天循环 | ✅ |
| R14 | 拥有与父级不同模型的子智能体 | 每智能体 `ModelRef` | ✅ |
| R15 | 隔离的 shell | 容器中的 PTY | ✅ |
| R16 | 无需重启进程的配置 | 配置文件 + watch 解析器 | ✅ |
| R17 | 仅用于管理的单一主聊天 | Owner ↔ System Agent；非阻塞分发 | ✅ |
| R18 | 安全的自我配置：配置——可以，令牌——不行 | Config-write 无 secret-read；密钥请求 → RED | ✅ |

---

## 2. 原则

1. **图，而非转录。** 状态单元是带连接的类型化节点。
2. **默认隔离。** 对主机/密钥的访问是显式授予。
3. **一切皆带租约的资源。** GPU、端口、egress、提供方 rate-limit。
4. **Durable by design。** 智能体经受重启（event-sourced）。
5. **配置即文件。** 运行期更改、版本化、可回滚（ADR-006）。
6. **密钥不可侵犯。** 智能体可以写配置，但不能读令牌。
7. **Fail fast, recover once。**
8. **EO 风格的 Java 核心。** `final`、`private final`、构造函数只赋值，
   无 `null`/静态/`instanceof`，通过装饰器实现行为。门禁 Qulice + jtcop。

---

## 3. 总体架构

```
┌───────────────────────────────────────────────────────────────┐
│ UI (React + TS + React Flow)                                   │
│  Главный чат · Канвас (RED/YELLOW/GREEN) · Карточки · Агенты   │
│  Дашборд ресурсов · Human-очередь                              │
└───────────────────────────▲───────────────────────────────────┘
              REST + WebSocket │
┌───────────────────────────┴───────────────────────────────────┐
│ pootOS-kernel (Java 25)                                        │
│  Actors · Scheduler · Resource Arbiter · Watchdog · Budgets    │
│  ConfigResolver (hot) · Event Log · Repair-flow · Zones        │
├───────────────────────────────────────────────────────────────┤
│ pootOS-context   Context Graph (nodes/edges, lessons, slices)  │
│ pootOS-agent     Agent runtime · Templates · Skills · Tools    │
│ pootOS-providers Provider SPI (OpenAI-compat, Anthropic, local)│
│ pootOS-system    System Agent (meta operations)                │
│ pootOS-github    Webhook receiver · GitHub client · Review flow │
├───────────────────────────────────────────────────────────────┤
│ pootOS-sandbox (SPI)                                           │
│   ├─ docker+runsc (gVisor)   ← основной, Win/macOS/Linux       │
│   └─ process (dev-only, небезопасно, помечено)                 │
└───────────────────────────▲───────────────────────────────────┘
              Docker Engine API │ (kernel ↔ Docker; агенты — НЕТ)
```

**安全规则 #1：** Docker socket 仅内核可用；智能体获得容器，但不能访问 docker/主机。

---

## 4. 模块（Maven 多模块）

| 模块 | 职责 |
|---|---|
| `pootos-kernel` | Actors、scheduler、leases、watchdog、budgets、event log、hot-config、区域 |
| `pootos-context` | 上下文图：节点、边、content-addressing、切片、压缩、教训 |
| `pootos-sandbox` | `Sandbox` SPI |
| `pootos-sandbox-docker` | Docker + gVisor 适配器 |
| `pootos-sandbox-process` | 开发适配器（无隔离；仅本地） |
| `pootos-providers` | `ModelProvider` SPI + 适配器 |
| `pootos-mcp` | MCP 客户端：SPI + JSON-RPC framing、服务器连接 |
| `pootos-agent` | 智能体运行时：模板、步骤、技能、工具 |
| `pootos-system` | System Agent（对配置文件/图的元操作） |
| `pootos-github` | webhook 接收、GitHub 客户端、审查流程 |
| `pootos-ui-api` | 供 Web UI 的 REST + WebSocket |
| `pootos-skills` | 内置技能（资源），如 EO-review |
| `pootos-cli` | 启动/停止、入口点 |
| `pootos-web` | 前端（独立的 Vite 构建，非 Maven） |

MVP 从子集开始：`kernel`、`context`、`sandbox`、`sandbox-docker`、`providers`、
`mcp`、`agent`、`github`、`ui-api`、`cli`。

---

## 5. 内核（kernel）

### 5.1 执行模型

- **Agent = durable actor。** 状态：`AgentRecord{id, template, modelRef, status,
  leases[], heartbeat, budget, currentCard, edgesToContext}`。
- 监督：根 supervisor；actor 崩溃不会拖垮内核。
- 并行——**virtual threads (Loom)**：I/O（LLM、shell、GitHub）不阻塞调度器。
  没有主聊天循环——只有任务 DAG 和图。
- **Event log**——append-only（`SQLite` WAL + 事件表）；恢复——重放。

### 5.2 Resource Arbiter (R6)

- 资源有名字：`gpu:0`、`port:8080`、`disk:/x`、`net:egress`、`llm:openai:rpm`。
- **Lease** = `{resource, owner, ttl, heartbeatAt, mode(shared|exclusive)}`。
- `acquire`：空闲 → 立即；被 exclusive 占用 → 任务进入 `queued-on-resource`
  （不挂起），调度器会在释放时将其拉起（优先级 + FIFO）。
- `renew` 随 heartbeat；TTL 到期 → release。死锁在构造上不可能
  （获取顺序 + TTL + 崩溃时强制 release）。

### 5.3 超时、watchdog、预算、操作员决策 (R6)

- **Watchdog** 捕捉过期的 leases 和 heartbeat 静默 → `stalled`，释放 leases，
  升级到 repair-flow。
- **Budget** 按任务/图：`wallclock`、`tokens`、`vram`、`cost`。超出 → kill，
  `Failure` 节点，`Lesson` 候选。
- **资源仪表盘**显示：可用资源、谁持有 leases、TTL、卡住的智能体。
  Owner/System Agent 可以**优化 / 停止 / 重启**智能体。
- 长时间前台构建不盲目启动：超时 + 日志文件 + 尾部轮询。

### 5.4 区域 (R9)

- 调度器按规则和智能体事件（访问请求、预算耗尽、隔离退化等）在
  `RED`/`YELLOW`/`GREEN` 之间移动卡片。

### 5.5 Hot config (R16, R18)

- 配置（提供方、工具、MCP、模板、策略）存储在配置目录的**普通文件**中
  （`ConfigDirectory`/`ConfigFile`）——这是数据，不是图节点。
- `ConfigWatcher` 通过 `java.nio.file.WatchService` 监视文件并触发 hot reload，
  无需重启；`ConfigResolver` 提供当前配置。
- System Agent 通过写入文件来更改配置；内核在 watch 事件时应用更改。
- **权限分离：** 允许写配置文件；**不允许读密钥**；文件中只有 `SecretRef` 引用（见 §12）。

---

## 6. 沙箱 (R1, R15)

### 6.1 SPI

```java
interface Sandbox {
    Shell shell();       // PTY
    Workspace fs();      // контролируемая ФС
    Exec exec();         // запуск процессов
    void close();
}
```

### 6.2 Docker + gVisor（主要）

- 智能体容器：
  - **non-root**；**read-only rootfs**；独立的可写层（overlay）；
  - **不挂载 host FS**；仅项目明确批准的挂载；
  - `--runtime=runsc`（gVisor）；
  - `--cap-drop=ALL`、`no-new-privileges`、seccomp/AppArmor；
  - 限制 `--memory`、`--pids-limit`、`--cpus`；
  - **网络 default-deny**：egress 仅通过 allowlist 代理（GitHub API、提供方）。
- **Docker socket 对智能体不可用。**
- 隔离的 shell = 容器中的 PTY，由内核启动。

### 6.3 平台

- **macOS/Linux：** Linux-VM 中的 Docker Engine → `runsc`。标准做法。
- **Windows：** 使用 **WSL2 后端**的 Docker Desktop；`runsc`——在 WSL2 发行版中。
- **Fallback：** 无 `runsc` → hardened Docker；退化事实在 UI 中**显式**呈现（不静默），
  并将受影响的卡片移到 🟡 YELLOW。

### 6.4 项目与挂载 (R12)

- **`Project` 是逻辑实体，不绑定到单一文件夹。** 一个项目可以包含 1、2 个或更多
  真实 OS 的已挂载目录。
- **挂载由智能体请求，决定权在人。** 流程：
  1. 智能体写入节点 `MountRequest{path, mode(ro|rw), reason}`；
  2. 请求卡片移到 🔴 RED；
  3. 人批准/拒绝；
  4. 批准后，内核将挂载加入相关智能体的容器。
- 默认挂载为 `ro`；`rw` 需要显式确认。
- 一个智能体可以涉及多个项目；项目共享教训（见 §7.5），但
  各自分开产物/挂载。

---

## 7. 上下文图 (R7, R8, R10, R12)

### 7.1 节点

`Task`、`Decision`、`Evidence`、`Artifact`、`Failure`、`Lesson`、`HumanQuestion`、`Summary`、
`Review`、`Finding`、`Config`、`MountRequest`。

### 7.2 边

`depends_on`、`derived_from`、`supersedes`、`invalidated_by`、`taught_by`、`addresses`、
`produces`。

### 7.3 Content-addressing 与存储

- 节点以规范 JSON 的 SHA-256 寻址。存储：**SQLite**（节点、边、事件）
  + 用于产物的 blob-store。可移植，无服务器。
- 幂等写入：相同节点 = 相同地址（同步的基础，R11）。

### 7.4 切片与读取

- 智能体读取**切片**：`slice(taskId)`——沿边
  `derived_from`/`depends_on`/`addresses` 到给定深度的闭包，优先新的和
  相关的。切片是智能体 prompt 的输入。
- 写入：`attach(node, edges[])`。

### 7.5 压缩与教训 (R10)

- `Summary` 节点引用其来源（`derived_from`）；原件保留/归档
  ⇒ 压缩**不丢失溯源**。教训跨越会话/项目变更而存续。
- **`Lesson`**（问题日志，一等公民）：
  - `trigger`（对任务标签 / 错误签名的 glob）、`advice`、`validity`、
    `sourceFailure`、`confidence`；
  - 在步骤之前智能体查询相关教训；匹配项作为**约束**注入 prompt；
  - 成功（“做了修改——奏效了”）被记为 `Lesson` 并复用 ⇒ 解决
    “成功在任务变更时丢失”的痛点。

### 7.6 拯救死亡智能体的上下文 (R8)

- 崩溃/stall → 子树 `stalled`。
- **Repair-flow：** repair 智能体沿边继承上游上下文（决策、
  证据、产物），而非原始对话，并从断点继续。
- Repair 智能体可以拥有不同的模型。

### 7.7 `pootos-context` 包

- `io.github.artemget.pootos.context.node` — `Node`、`NodeId`、`LessonNode`、`EscapedText`
  （JSON 转义的 `org.cactoos.Text`）；文本与摘要原语来自 `org.cactoos.*`；
- `io.github.artemget.pootos.context.edge` — `Edge`、`TypedEdge`、`Relation`；
- `io.github.artemget.pootos.context.graph` — `Graph`、`MemoryGraph`。

### 7.8 记忆与召回 (ADR-003, *提议中*)

- **事实来源是我们自己的 content-addressed 图**（`Task`/`Decision`/`Lesson` +
  类型化边）。确定性的保证是问题日志所必需的；概率性记忆层无法提供。
- 记忆是一个 **SPI**：`ContextGraph` + `Recall`。持久化为 SQLite；语义召回是
  可插拔的（可选且可替换）。
- **TencentDB Agent Memory 被接受为设计参考，而非运行时依赖：** 我们借用
  分层记忆（conversation → atom → scenario → persona）的理念以及
  skill-asset/ACL 模型。
- 如果之后 SPI 背后需要外部召回/时序引擎，首先考虑 **Graphiti/Zep**
  （双时序知识图），然后才是 TencentDB。
- 状态——**proposed**；等待 Owner 确认。详情：`docs/adr/ADR-003-context-memory.md`。

---

## 8. 依赖人的任务分类 (R9)

- `HumanQuestion{level, requiredCapability, deadline, escalationPolicy}`：
  - **L0** 自动 · **L1** 通知 · **L2** 询问 · **L3** 仅人。
- 阻塞性任务 → deferred 队列；独立的 DAG 分支**继续**。
- 通过区域显示：L2/L3 → 🔴；“最好看一下” → 🟡。

---

## 9. System Agent (R2, R5, R17, R18)

与智能体相同的运行时，带有特权操作集：

- `define_provider`、`define_tool`、`define_mcp`、`define_agent_template`、`define_skill`、
  `set_policy`、`define_human_task_level`、`create_task`、`assign_agent`、`move_zone`、
  `secret_form`。
- 每个操作 = 写入配置文件（数据，不是密钥）；内核应用 hot-reload。
- **从主聊天接受指令并分发它们，不阻塞自身循环。**
- **自我配置：** System Agent 配置自身以及 pootOS 应用**通过智能体**；
  它展示 **`SecretForm`**（仅字段模式，无值），而值由**人**通过 UI 表单
  → OS secret store 提供；智能体与执行器只持有 `SecretRef`（见 §12）。

---

## 10. 智能体运行时 (R3, R14, R15)

- **Agent Template**——声明：系统 prompt、技能、允许的工具/MCP、沙箱类型、
  默认 `ModelRef`、预算、资源需求、允许的挂载。
- **Skills**——可加载的配方（包括 EO-review，§13）；通过
  `programming-philosophy-skill` 流水线编写。
- **Tools / MCP**——Java MCP 客户端；便捷连接；工具按模板授予。
- **子智能体 (R14)：** `spawn(template, ModelRef)`；子智能体的 `ModelRef` 可以不同于
  父级。父级不被阻塞：子智能体是独立的 actor。
- 智能体步骤：`read slice → query lessons → act → attach nodes → renew lease`。每一步
  幂等并记录日志。
- **AgentExecutor (ADR-002)：**“如何执行智能体的一个回合”——可替换的 SPI
  `AgentExecutor`；智能体是 durable actor，其回合委托给执行器。第一个适配器是
  **`OpenCodeExecutor`**：内核在**沙箱内**启动 **opencode**（server/SDK），
  将**上下文图切片**投射到执行器的输入，并把其输出写回为图节点。模型/提供方的选择
  通过执行器的配置，但**从属于 pootOS 的租约和预算**。opencode 的 Node/TS 运行时
  **隔离在沙箱中**，不是内核依赖。之后可在同一 SPI 背后实现原生 Java/EO 执行器。
- 智能体配置——**手动或通过 System Agent**。

---

## 11. 提供方 (R3)

- SPI `ModelProvider { complete(request): response }` + 注册表。
- MVP 适配器：OpenAI 兼容、Anthropic、本地（Ollama/其他）。
- 每智能体 `ModelRef{provider, model, params}`；提供方 rate-limit——带租约的资源
  （`llm:openai:rpm`），防止并行智能体时的节流。

---

## 12. 配置与密钥安全 (R18)

关键分离（“填写配置——可以，获取令牌——不行”）：

- **Config-write：** System Agent/智能体可以创建和编辑配置文件（提供方、
  工具、MCP、模板）——这些是数据，不是密钥。
- **Secret-read：** 对令牌/密钥的访问对智能体和执行器禁止。存储——Windows
  Credential Manager / macOS Keychain。
- **SecretForm：** System Agent 展示 **`SecretForm`**——仅字段的**模式**
  （`SecretField{name, label, provider, purpose}`），**无值**。
- **需要密钥时的流程：**
  1. System Agent 声明 `SecretForm`——卡片移到 🔴 RED；
  2. **人**通过 UI 表单输入值；值写入 **OS secret store**（绝不是智能体）；
  3. 值**不**进入上下文图、配置文件、prompt 或日志（集中式脱敏）；
     沙箱边界只有 `SecretRef`；
  4. `Config`（文件）获得对密钥的**引用**（`SecretRef`），而非值。
- **No agent/executor secret access：** 智能体与 `AgentExecutor`（opencode）都不读取值；
  只有内核在注入时解析 `SecretRef`。
- **GitHub 令牌：** 尽可能**通过内核**传递（智能体看不到值）；
  替代方案是沙箱边界的 `SecretRef`。决定——ADR-005。

---

## 13. GitHub 集成与审查智能体（MVP 任务）

### 13.1 流程

```
GitHub webhook (pull_request: opened|synchronize|reopened)
        │  HMAC-подпись проверяется ядром
        ▼
Card(Task) ──► lease(net:egress, llm:*) ──► зона GREEN
        │
        ▼
Ревью-агент в песочнице:
  1. resolve repo/PR; прочитать связанный issue (PR закрывает РОВНО его)
  2. получить diff (GitHub API через ядро; токен не в контейнере)
  3. EO-проверки (§13.2)
  4. при необходимости — фоновый `mvn -Pqulice -Pjtcop` в песочнице (таймаут + лог)
  5. опубликовать ревью + строчные комментарии; развести nit vs follow-up
  6. применить метку `approved`/`needs-review`
  7. записать узлы Review/Finding/Lesson
```

### 13.2 审查约定（EO-skill + `teleroute` 实践）

- **Correctness**——做 issue 要求的事；边界情况。
- **Tests**——复现测试（之前失败）、每个测试一个 `assertThat`、Hamcrest、`final`
  类、无字段、名为 `*Test`。
- **Style/EO**——`final` 类、`private final` 字段、无 static/`null`/`instanceof`
  （`equals` 除外）、无盲目转型。
- **Process**——按 issue 建分支、提交带 `#<issue>`、单一 concern、不重写历史。
- **Gates**——Qulice 0.36 + jtcop 绿色；通过 GitHub MCP 读取 CI（不盲目运行长时间
  前台构建）。
- **三分类：** nit → 在本 PR 中修改；较大项 → 独立 issue `Follow-up: #NNN`；
  回复每条评论；解决自己的线程。
- **单一账号：** `Approve` 不可能 ⇒ **标签即结论**；无元评论。
- **回复格式：** `verdict / checks / actions / remaining`。
- 来源：EO skill 的 `references/11-reviewer-playbook.md`、`references/07-process.md`；
  `ArtemGet/teleroute` 的 PR/issue（issue-first 且带最小复现，`Closes #N`，
  What/Why/How/Test plan、gate、PDD 拼图）。

---

## 14. UI (R4, R12)

- **主聊天**——唯一；System Agent 控制、报告、修改。
- **画布**——RED/YELLOW/GREEN 区域；卡片（任务/产物）带 drill-in 和直接
  写入；在区域间移动卡片。
- **智能体**——“正在工作”和“全部”；智能体卡片：template、`ModelRef`、heartbeat、
  leases、步骤、事件流、挂载。
- **资源仪表盘**——leases、TTL、预算、成本、卡住；操作
  优化/停止/重启。
- **Human 队列**——`HumanQuestion` L0–L3、截止时间、升级。
- 前端：React + TS + Vite + React Flow；通过 WebSocket 实时更新。稍后——基于
  相同 `pootos-ui-api` 的 Kotlin Multiplatform/Compose 桌面端。

---

## 15. 机器之间的同步 (R11)——设计

- 图 = append-only 日志；节点 content-addressed ⇒ 合并 = 集合的并集。
- 传输（稍后选择）：git 仓库 data-dir · Syncthing · sync 服务器。
- `HumanQuestion`/`Config`——冲突点；CRDT 语义（LWW/显式解决）。
- “服务器 → 服务器”迁移：日志 + blob-store；智能体通过重放恢复。

---

## 16. 技术栈

| 层 | 选择 | 原因 |
|---|---|---|
| 后端 | Java 25 (Loom), Maven, EO | virtual threads = 数百智能体成本低廉；EO 风格 + 门禁 |
| 质量 | Qulice 0.36, jtcop, JaCoCo/PIT | 如 `teleroute`；固化在 POM 中 |
| 智能体执行器 | SPI `AgentExecutor`；沙箱中的 `OpenCodeExecutor` 适配器 (opencode) | 复用 opencode；Node/TS 运行时**隔离**在沙箱中 (ADR-002) |
| 依赖门禁 | `google/osv-scanner`（keyless） | 无密钥、自包含 CI；在 high/critical 时失败 (ADR-004) |
| 调度器 | 自研 event-sourced kernel（MVP）；稍后为 Temporal 提供 SPI | 轻量本地启动，无需服务器 |
| 存储 | SQLite (WAL) + blob-store | 可移植，无外部服务器 |
| 沙箱 | Docker + gVisor (`runsc`) | R1/R15；跨平台 |
| 提供方 | OpenAI-compat / Anthropic / local | R3，低成本启动 |
| MCP | Java MCP 客户端 | 开箱即用的工具 |
| 密钥 | Win Credential Manager / macOS Keychain | 不将密钥写入图 |
| UI | React + TS + Vite + React Flow | 快速启动、图可视化 |
| 打包 | jpackage/bootstrap + Docker | 面向 Win/macOS 的应用 |

### 16.1 工作环境（已验证）

- git 2.43 · Maven 3.9.9 · Docker 28.5 · **JDK 25 = `C:\Users\Артем\.jdks\temurin-25`**
  （PATH 中默认是 JDK 17——构建时将 `JAVA_HOME` 指向 25）。
- 未安装 `gh`；**本地非交互式 `git push` 不可能**（无
  已保存令牌）⇒ 远程写入和 PR/merge 走 **GitHub MCP**。
  可选：为子智能体的直接 `git push` 颁发 fine-grained PAT（见 §22）。

---

## 17. 安全（我们的保证）

- **FS：** 智能体只看到已批准的挂载；外部 host 不可访问。
- **进程：** gVisor syscall 隔离；无 `--privileged`；cap-drop ALL。
- **网络：** default-deny；egress 通过代理按 allowlist。
- **Docker：** socket 仅属于内核。
- **密钥：** keychain；**智能体不读取**；只有 `SecretRef`；日志脱敏。
- **GitHub：** webhook HMAC、allowlist、最小权限（read + comment/label）。
- **依赖：** `google/osv-scanner` 门禁（keyless，OSV 数据库）——构建在
  high/critical 漏洞时失败；无需 API 密钥或外部密钥（ADR-004）。
- **审计：** 所有操作即事件（谁、什么、何时、在哪个租约下）。

---

## 18. 可观测性

- 指标：活跃智能体、队列深度、资源占用、TTL 到期、令牌、
  成本、步骤延迟、budgets/watchdog 触发。
- UI 中的事件流；日志适合事后复盘并产生 `Lesson`。

---

## 19. 开发工厂（pootOS 如何被创建）

角色（除 Owner 外全部是智能体）：

- **Orchestrator**（主智能体）：描述架构、切成子任务、维护赛道、
  向 Owner 报告；最小化验证。**自身不写代码。**
- **Coding subagent**：在隔离工作区实现一个子任务；必须留下
  测试；遵守 EO + 门禁。
- **Review subagent**：独立审查；**成功时——合并**（见 §19.2）。
- **Docs subagent**：保持 `ru/en/zh-CN` 同步。
- **Docs-auditor agent**：定期检查文档时效性。

### 19.1 流程

```
Owner → главный чат → Orchestrator
Orchestrator → подзадача → Coding subagent (локальный клон, ветка)
        → build `mvn --errors --batch-mode clean install -Pqulice -Pjtcop` (JAVA_HOME=25)
        → PR (через GitHub MCP)
        → Review subagent (по EO-плейбуку)
        → если ОК: Review subagent мержит; иначе: замечания → новый цикл
Orchestrator → трек + отчёт Owner
```

### 19.2 规则

- 每个 PR 单一 concern；小 PR；每个行为/缺陷都有测试。
- 门禁 `Qulice 0.36 + jtcop + JaCoCo` 强制；绿色 build 是合并条件。
- 审查者**仅在成功时合并**（单一账号——标签作为结论）。
- 不重写历史；提交引用 issue（`#<issue>`）。
- 代码**只由子智能体**编写；Orchestrator——规格/拆分/赛道/报告。

---

## 20. Roadmap / MVP 切片

1. **Thinking**（现在）：本文档。✅
2. **Block 0 — scaffold：** 父 POM（Java 25、Qulice 0.36、jtcop、JaCoCo）、
   `.gitignore`、`LICENSE`、`README`、CI-workflow；带平凡模块的绿色 build。
3. **Block 1 — context：** 节点/边/切片/content-addressing/压缩 + `Lesson`。
4. **Block 2 — kernel：** virtual threads 上的 actors + event log + leases + watchdog +
   budgets + 区域。
5. **Block 3 — sandbox：** `Sandbox` SPI + `docker+runsc`（隔离 shell、挂载）。
6. **Block 4 — providers + agent runtime：** 每智能体 `ModelRef`、模板、技能、子智能体。
7. **Block 5 — system agent：** config-write、hot-reload、secret 请求 → RED。
8. **Block 6 — github + review agent：** webhook → EO 审查 → 标签
   （子智能体 PR 的 dogfooding）。
9. **Block 7 — ui-api + web：** 画布（区域/卡片/智能体）、资源仪表盘、聊天。
10. **Block 8 — docs：** en + zh-CN；auditor。

**MVP 的 Definition of Done：** 隔离负向测试绿色；两个并行智能体不
共享资源；webhook → 审查符合 EO 约定；无重启的配置变更；
包在 Windows 和 macOS 上安装并运行。

---

## 21. 风险与开放问题

- **Docker Desktop（Win/macOS）上的 gVisor：** 验证 WSL2/Linux-VM 中的 `runsc`；
  否则——显式退化为 hardened Docker（§6.3）。
- **LLM 审查质量：** 通过严格 EO skill + `Lesson` 缓解。
- **审查成本/延迟：** 预算、allowlist、按 PR 大小区分模型。
- **单节点 durability：** event log + SQLite；之后——把日志移出。
- **同步：** 推迟；content-addressing 从一开始就铺设。
- **本地 push：** 无非交互令牌 ⇒ MCP；可选——PAT（§22）。

---

## 22. 供 Owner 的开放决定

1. **git 的 PAT：** 颁发 fine-grained PAT（contents: read/write）以让子智能体直接
   `git push`——还是保持通过 GitHub MCP 提交？（我推荐 PAT：开发工厂的自主性
   更高。）
2. **JDK 25：** 确认使用 `temurin-25`（PATH 中当前是 17）。
3. **MVP 沙箱约束：** DoD 中是否需要隔离负向测试作为阻塞项。
4. **语言规范：** 保持 `ru` 为规范（推荐），还是以 `en` 为规范？

---

## 23. ADR (Architecture Decision Records)

完整列表与索引见 [`docs/adr/README.md`](adr/README.md)。

| ADR | 决策 | 状态 |
|---|---|---|
| [ADR-001](adr/ADR-001-sandbox-isolation.md) | 沙箱隔离 = Docker + gVisor | Accepted |
| [ADR-002](adr/ADR-002-agent-executor.md) | 智能体通过 SPI `AgentExecutor`；第一个适配器是 opencode | Accepted |
| [ADR-003](adr/ADR-003-context-memory.md) | 记忆 = 我们的 content-addressed 图作为事实来源 + SPI 背后的外部召回 | Proposed |
| [ADR-004](adr/ADR-004-dependency-vulnerability-gate.md) | 依赖门禁 = `osv-scanner`（keyless） | Accepted |
| [ADR-005](adr/ADR-005-inbound-channels.md) | 不可信入站通道 + 调度智能体 | Accepted |
| [ADR-006](adr/ADR-006-config-and-secrets.md) | 配置 = 文件 + filesystem watch；密钥通过 `SecretForm` | Accepted |

开放（尚未起草）：kernel 的起源（自研 vs Temporal/Restate）、存储选择
（SQLite vs KV/LMDB）、同步（git-dir vs Syncthing vs sync 服务器）、GitHub 令牌
（通过内核代理 vs `SecretRef` 进容器）。
