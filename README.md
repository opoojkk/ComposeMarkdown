# ComposeMarkdown

A Compose Multiplatform Markdown component focused on parsing and rendering Markdown content in shared UI.

## Features

- `MdDocument` + `MdBlock`/`MdInline` render model
- `MarkdownParser` based on JetBrains Markdown AST
- `Markdown(...)` composable for rendering Markdown strings or pre-parsed documents
- `MarkdownStyle` + `MarkdownDefaults` for style customization
- Custom link and image click callbacks
- Optional custom image renderer slot
- Optional custom block renderer slot with default-content fallback
- Optional dedicated HTML block renderer slot
- Optional dedicated inline-image renderer slot
- Optional inline rendering slot based on `AnnotatedString.Builder`
- Table alignment metadata mapped into the render model
- HTML block support rendered as dedicated raw-content blocks
- GFM strikethrough inline support
- GFM inline math and block math support
- `UnsupportedBlock` fallback to avoid silently dropping unsupported syntax

## Dependency

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("org.jetbrains:markdown:0.7.3")
        implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    }
}
```

## Package Layout

- `com.composemarkdown.model`: document and inline/block model
- `com.composemarkdown.parser`: Markdown parsing
- `com.composemarkdown.ui`: public composables and styles

## Supported Syntax

- Heading
- Paragraph
- Quote
- List and nested list
- Task list
- Code fence and indented code block
- Horizontal rule
- Pipe table with start/center/end alignment
- HTML block
- Block math
- Image block
- Inline emphasis, strong, strikethrough, code, math, link, inline image token

## Usage

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

## Pre-parsed Usage

```kotlin
import com.composemarkdown.parser.MarkdownParser
import com.composemarkdown.ui.Markdown

val parser = MarkdownParser()
val document = parser.parse(markdown)

Markdown(document = document)
```

## Customization

```kotlin
Markdown(
    markdown = markdown,
    htmlBlockContent = { htmlBlock ->
        // render raw html block your own way
    },
    inlineImageContent = { inlineImage, style ->
        withStyle(SpanStyle(color = style.linkColor)) {
            append("[img:${inlineImage.alt ?: inlineImage.url}]")
        }
    },
    style = MarkdownDefaults.style(),
    imageContent = { image ->
        // render your own image composable
    },
    blockContent = { block, defaultContent ->
        defaultContent()
    },
)
```

## Inline Customization

```kotlin
Markdown(
    markdown = markdown,
    inlineContent = { inline, style, defaultContent ->
        when (inline) {
            is StrikeInline -> withStyle(
                SpanStyle(
                    color = style.linkColor,
                    textDecoration = TextDecoration.LineThrough,
                ),
            ) {
                defaultContent()
            }

            else -> defaultContent()
        }
    },
)
```

`inlineImageContent` 会优先于通用的 `inlineContent`，适合专门处理 `InlineImage` 的文本化展示策略。

## Debug Entry

- Desktop: `./gradlew run`
- Android: set `ANDROID_HOME` or create `local.properties`, then run `./gradlew installDebug`

Debug app file:
- `src/commonMain/kotlin/com/composemarkdown/MarkdownDebugApp.kt`

## Current Limitations

- Unsupported Markdown constructs are rendered as raw text fallback blocks
- The project is a renderer component, not a Markdown editor
