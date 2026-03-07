# ComposeMarkdown

[English](README.md)

`ComposeMarkdown` 是一个面向 **Compose Multiplatform** 的 Markdown 渲染组件，目标是在共享 UI 层中提供一套可复用、可扩展、可自定义的 Markdown 展示方案。

仓库当前重点是：

- 在 `commonMain` 中完成 Markdown 解析与渲染
- 提供统一的文档模型，避免业务层直接依赖底层 AST
- 提供默认渲染能力，同时保留足够多的自定义扩展点

## 仓库定位

这个仓库不是一个 Markdown 编辑器，而是一个 **Markdown Viewer / Renderer 组件**。

它更适合以下场景：

- 聊天消息、文档详情、帮助中心、公告内容等 Markdown 展示
- Compose Multiplatform 项目中的共享渲染组件
- 希望在默认渲染基础上进行局部自定义的业务场景

## 使用方式

### 1. 直接传入 Markdown 字符串

```kotlin
import com.composemarkdown.ui.Markdown

val markdown = """
# Title

This is **Compose Markdown** with a [link](https://kotlinlang.org).
""".trimIndent()

Markdown(
    markdown = markdown,
    onLinkClick = { url -> println(url) },
    onImageClick = { url -> println(url) },
)
```

### 2. 预解析后再渲染

适合需要缓存解析结果、避免重复解析的场景。

```kotlin
import com.composemarkdown.parser.MarkdownParser
import com.composemarkdown.ui.Markdown

val parser = MarkdownParser()
val document = parser.parse(markdown)

Markdown(document = document)
```

### 3. 自定义样式

仓库已经支持通过 `MarkdownStyle` 和 `MarkdownDefaults` 做统一样式覆盖。

```kotlin
import com.composemarkdown.ui.Markdown
import com.composemarkdown.ui.MarkdownDefaults

Markdown(
    markdown = markdown,
    style = MarkdownDefaults.style().copy(
        blockSpacing = 12.dp,
        linkColor = MaterialTheme.colorScheme.primary,
    ),
)
```

### 4. 自定义局部渲染

支持对块级内容、HTML 块、图片、行内内容等进行扩展。

```kotlin
Markdown(
    markdown = markdown,
    style = MarkdownDefaults.style(),
    htmlBlockContent = { htmlBlock ->
        // 自定义 HTML block 渲染
    },
    imageContent = { image ->
        // 自定义块级图片渲染
    },
    blockContent = { block, defaultContent ->
        // 按需拦截 block
        defaultContent()
    },
    inlineContent = { inline, style, defaultContent ->
        // 按需拦截 inline
        defaultContent()
    },
)
```

`inlineImageContent` 会优先于通用的 `inlineContent`，适合专门处理 `InlineImage` 的文本化展示策略。

## 当前支持能力

- 标题
- 段落
- 引用
- 有序/无序列表
- 嵌套列表
- Task List
- 围栏代码块与缩进代码块
- 分割线
- 表格及对齐信息
- HTML Block
- 图片 Block
- 行内粗体、斜体、删除线、代码、链接、图片标记
- 数学公式的块级与行内结构解析
- 不支持语法的兜底渲染

## 优势

### 1. 分层清晰

解析、模型、渲染彼此解耦，后续演进成本更低。

### 2. 适合 Compose Multiplatform

核心逻辑放在 `commonMain`，天然适合共享 UI 和跨平台复用。

### 3. 自定义能力强

不仅支持统一样式覆盖，还支持图片、HTML、block、inline 等多个层级的局部接管。

### 4. 便于业务集成

既支持直接传字符串，也支持预解析模型复用，适合列表场景、缓存场景和富文本展示场景。

### 5. 有兜底策略

对于暂未支持的 Markdown 结构，不会直接静默丢失，而是通过 `UnsupportedBlock` 等方式保留原始内容。

## 依赖

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("org.jetbrains:markdown:0.7.3")
        implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    }
}
```

## 未来计划

- [x] 自定义样式
- [ ] LaTeX 公式渲染
- [ ] 支持 iOS、Mac 原生渲染
