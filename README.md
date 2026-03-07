# ComposeMarkdown

[简体中文](README.zh-CN.md)

`ComposeMarkdown` is a **Compose Multiplatform** Markdown rendering component designed for shared UI layers. It aims to provide a reusable, extensible, and customizable solution for displaying Markdown content across platforms.

The project currently focuses on:

- Parsing and rendering Markdown in `commonMain`
- Providing a stable document model instead of exposing the underlying AST directly to business code
- Offering useful default rendering behavior while keeping enough extension points for customization

## Positioning

This repository is not a Markdown editor. It is a **Markdown viewer / renderer component**.

It is well suited for:

- Chat messages, article details, help center pages, announcements, and similar Markdown-based content
- Shared UI components in Compose Multiplatform projects
- Product scenarios that need default rendering with partial custom overrides

## Usage

### 1. Render directly from a Markdown string

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

### 2. Parse first, then render

This is useful when you want to cache parsed results and avoid repeated parsing.

```kotlin
import com.composemarkdown.parser.MarkdownParser
import com.composemarkdown.ui.Markdown

val parser = MarkdownParser()
val document = parser.parse(markdown)

Markdown(document = document)
```

### 3. Customize styles

The repository already supports style overrides through `MarkdownStyle` and `MarkdownDefaults`.

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

### 4. Customize partial rendering

You can override block content, HTML blocks, images, and inline rendering behavior.

```kotlin
Markdown(
    markdown = markdown,
    style = MarkdownDefaults.style(),
    htmlBlockContent = { htmlBlock ->
        // Custom HTML block rendering
    },
    imageContent = { image ->
        // Custom block image rendering
    },
    blockContent = { block, defaultContent ->
        // Intercept blocks when needed
        defaultContent()
    },
    inlineContent = { inline, style, defaultContent ->
        // Intercept inline rendering when needed
        defaultContent()
    },
)
```

`inlineImageContent` takes priority over the generic `inlineContent`, which makes it a better fit for dedicated inline image placeholder strategies.

## Current Capabilities

- Headings
- Paragraphs
- Quotes
- Ordered and unordered lists
- Nested lists
- Task lists
- Fenced code blocks and indented code blocks
- Horizontal rules
- Tables with alignment metadata
- HTML blocks
- Image blocks
- Inline bold, italic, strikethrough, code, links, and inline image tokens
- Block and inline math structure parsing
- Fallback rendering for unsupported syntax

## Advantages

### 1. Clear layering

Parsing, modeling, and rendering are decoupled, which makes the project easier to evolve.

### 2. Built for Compose Multiplatform

The core logic lives in `commonMain`, making it naturally suited for shared UI and cross-platform reuse.

### 3. Strong customization support

It supports not only unified style overrides, but also partial control over images, HTML blocks, block-level rendering, and inline rendering.

### 4. Easy to integrate

It supports both direct string rendering and pre-parsed document rendering, which works well for lists, caching scenarios, and rich text display.

### 5. Safe fallback strategy

Unsupported Markdown structures are not silently dropped. They can still be preserved through fallback blocks such as `UnsupportedBlock`.

## Dependencies

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("org.jetbrains:markdown:0.7.3")
        implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    }
}
```

## Roadmap

- [x] Custom styles
- [ ] LaTeX formula rendering
- [ ] Native rendering support for iOS and macOS

