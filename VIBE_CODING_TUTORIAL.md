# Vibe Coding 实战：从零构建多语言 AI 翻译系统

## 什么是 Vibe Coding (氛围编程)？

**Vibe Coding** 是一种由大语言模型（LLM）驱动的新型软件开发范式。它不再要求开发者精通每一个 API 的参数或每一行样板代码，而是将开发者的角色转变为 **“意图导演” (Vibe Director)**。

*   **传统编程**：关注“怎么写” (How) —— 语法、类层次、内存管理。
*   **Vibe Coding**：关注“想要什么” (What) —— 系统行为、用户体验、业务流。

在这种模式下，开发者提供“氛围”（设计文档、技术选型、功能描述），AI 负责“编码” (Coding)。当开发者的直觉（Vibe）与 AI 的执行力完美同步时，开发效率会产生量级飞跃。

---

## 项目案例：Multilingual Generation System

本项目是一个典型的 **全栈 KMP (Kotlin Multiplatform) 应用**，包含：
1.  **后端**：Ktor 服务，集成 PostgreSQL 数据库。
2.  **前端**：Compose for Web (Wasm/JS) 仪表盘。
3.  **AI**：本地 MLX 引擎 (Gemma 3 模型)。
4.  **核心功能**：文件上传、AI 自动翻译、多格式导出 (ZIP)、翻译记忆 (TM) 缓存。

---

## Vibe Coding 教程步骤

### 第一步：设定项目“口味” (Setting the Context)
在 Vibe Coding 中，**上下文 (Context) 就是一切**。
*   **操作**：在启动时提供详细的 `memory-bank/` 文件夹，包含 `lan-sys-design-document.md` 和 `tech-stack.md`。
*   **核心逻辑**：通过让 AI 阅读这些文档，AI 就获得了项目的“灵魂”。它知道你喜欢用 Ktor 而不是 Spring Boot，知道你倾向于使用 Exposed 作为 ORM。

### 第二步：快速搭建骨架 (Skeleton & Stubs)
不要试图让 AI 一次性写出完美的逻辑，先让它搭建“能动的骨架”。
*   **操作**：要求 AI 先实现所有接口的 **Stub (存根)**。
*   **Vibe 技巧**：先用一个总是返回 `[fr] hello` 的假翻译引擎跑通上传、生成、下载的全流程。这样可以第一时间验证 UI 和后端的连通性。

### 第三步：灵活的“轴转” (The Pivot - 从 OpenAI 到本地 AI)
Vibe Coding 最强大的地方在于应对需求变更。
*   **转折点**：项目中途，我们将翻译引擎从 OpenAI API 切换到了本地 MLX 模型 (`Gemma 3`)。
*   **操作**：只需向 AI 发出指令：“我不想用 OpenAI 了，我本地启动了 `mlx_lm.server` 在 5002 端口，请帮我重写 `TranslationService`。”
*   **执行**：AI 自动处理了从 HttpClient 到 JSON 序列化的所有细节，而开发者只需要确保本地模型已经跑起来。

### 第四步：基于视觉反馈的 UI 迭代 (UI Polish)
UI 开发是 Vibe Coding 的强项。
*   **操作**：开发者截图给 AI (例如 `Result1.png`)，AI 识别布局问题。
*   **反馈循环**：开发者发现“浏览文件按钮没反应”，AI 立即识别出 Web 平台 DOM 操作的缺失，并使用 `expect/actual` 模式修复了跨平台的文件选择逻辑。

### 第五步：氛围检查与调试 (The Vibe Check)
当代码跑不通时，不要去读几百行日志，直接让 AI “感受” 报错。
*   **案例**：当出现 `403 Forbidden` 或 `Unknown Key 'id'` 时，直接把原始日志贴给 AI。
*   **AI 的直觉**：它能迅速判断出是 CORS 配置缺失，或者是 `mlx_lm.server` 返回了额外的字段导致解析失败，并给出 `ignoreUnknownKeys = true` 的精准修复。

---

## 总结：Vibe Coding 的成功秘诀

1.  **保持文档同步**：随时更新 `progress.md`，让 AI 知道自己在哪里。
2.  **大胆描述意图**：不要害怕提出“我要一个支持拖拽的进度条”这种高层要求。
3.  **小步快跑**：每次修改后都要求进行一次 `curl` 或浏览器测试，确保 Vibe 没有跑偏。

**Vibe Coding 并不意味着不写代码，它意味着作为开发者的你，可以将精力集中在最有价值的地方：创造力和系统设计。**
