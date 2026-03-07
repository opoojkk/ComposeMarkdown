package com.composemarkdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.composemarkdown.model.HeadingBlock
import com.composemarkdown.model.HtmlBlock
import com.composemarkdown.model.InlineImage
import com.composemarkdown.model.LinkInline
import com.composemarkdown.model.MdBlock
import com.composemarkdown.model.MathBlock
import com.composemarkdown.model.MathInline
import com.composemarkdown.model.QuoteBlock
import com.composemarkdown.model.StrikeInline
import com.composemarkdown.model.UnsupportedBlock
import com.composemarkdown.ui.MarkdownHtmlBlockContent
import com.composemarkdown.ui.MarkdownInlineImageContent
import com.composemarkdown.parser.MarkdownParser
import com.composemarkdown.ui.Markdown
import com.composemarkdown.ui.MarkdownDefaults
import com.composemarkdown.ui.MarkdownInlineContent

@Composable
fun MarkdownDebugApp() {
    var markdown by remember { mutableStateOf(debugSampleMarkdown()) }
    var showSource by remember { mutableStateOf(false) }
    var usePreParsed by remember { mutableStateOf(false) }
    var useCustomBlocks by remember { mutableStateOf(false) }
    var useCustomStyle by remember { mutableStateOf(false) }
    var useCustomInline by remember { mutableStateOf(false) }
    var useCustomHtmlBlocks by remember { mutableStateOf(false) }
    var useCustomInlineImages by remember { mutableStateOf(false) }
    val parser = remember { MarkdownParser() }
    val parsed = remember(markdown, parser) { parser.parse(markdown) }
    val defaultStyle = MarkdownDefaults.style()
    val demoStyle = if (useCustomStyle) {
        defaultStyle.copy(
            blockSpacing = 12.dp,
            quoteBorderColor = MaterialTheme.colorScheme.primary,
            codeBlockBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            htmlBlockBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            mathBlockBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            inlineCodeBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            inlineMathBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            linkColor = MaterialTheme.colorScheme.primary,
        )
    } else {
        defaultStyle
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(onClick = { markdown = debugSampleMarkdown() }) {
                        Text("Reset Sample")
                    }
                }

                DebugToggleRow(
                    label = if (showSource) "Source On" else "Source Off",
                    checked = showSource,
                    onCheckedChange = { showSource = it },
                )
                DebugToggleRow(
                    label = if (usePreParsed) "Using Pre-parsed Document" else "Parsing From String",
                    checked = usePreParsed,
                    onCheckedChange = { usePreParsed = it },
                )
                DebugToggleRow(
                    label = if (useCustomBlocks) "Custom Block Content On" else "Custom Block Content Off",
                    checked = useCustomBlocks,
                    onCheckedChange = { useCustomBlocks = it },
                )
                DebugToggleRow(
                    label = if (useCustomStyle) "Custom Style On" else "Custom Style Off",
                    checked = useCustomStyle,
                    onCheckedChange = { useCustomStyle = it },
                )
                DebugToggleRow(
                    label = if (useCustomInline) "Custom Inline Content On" else "Custom Inline Content Off",
                    checked = useCustomInline,
                    onCheckedChange = { useCustomInline = it },
                )
                DebugToggleRow(
                    label = if (useCustomHtmlBlocks) "Custom Html Block Renderer On" else "Custom Html Block Renderer Off",
                    checked = useCustomHtmlBlocks,
                    onCheckedChange = { useCustomHtmlBlocks = it },
                )
                DebugToggleRow(
                    label = if (useCustomInlineImages) "Custom Inline Image Renderer On" else "Custom Inline Image Renderer Off",
                    checked = useCustomInlineImages,
                    onCheckedChange = { useCustomInlineImages = it },
                )

                val blocks = parsed.blocks
                Text("Blocks: ${blocks.size}")
                Text("Types: ${blocks.groupBy { it::class.simpleName }.map { "${it.key}:${it.value.size}" }.joinToString(", ")}")

                if (showSource) {
                    Text(
                        text = markdown,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    if (usePreParsed) {
                        Markdown(
                            document = parsed,
                            modifier = Modifier.fillMaxSize(),
                            style = demoStyle,
                            onLinkClick = { println("link: $it") },
                            onImageClick = { println("image: $it") },
                            htmlBlockContent = if (useCustomHtmlBlocks) debugHtmlBlockContent() else null,
                            inlineImageContent = if (useCustomInlineImages) debugInlineImageContent() else null,
                            blockContent = if (useCustomBlocks) { block, defaultContent ->
                                DebugBlockContent(block, defaultContent)
                            } else {
                                null
                            },
                            inlineContent = if (useCustomInline) debugInlineContent() else null,
                        )
                    } else {
                        Markdown(
                            markdown = markdown,
                            modifier = Modifier.fillMaxSize(),
                            style = demoStyle,
                            onLinkClick = { println("link: $it") },
                            onImageClick = { println("image: $it") },
                            htmlBlockContent = if (useCustomHtmlBlocks) debugHtmlBlockContent() else null,
                            inlineImageContent = if (useCustomInlineImages) debugInlineImageContent() else null,
                            blockContent = if (useCustomBlocks) { block, defaultContent ->
                                DebugBlockContent(block, defaultContent)
                            } else {
                                null
                            },
                            inlineContent = if (useCustomInline) debugInlineContent() else null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun debugInlineContent(): MarkdownInlineContent = { inline, style, defaultContent ->
    when (inline) {
        is StrikeInline -> withStyle(
            SpanStyle(
                color = style.linkColor,
                textDecoration = TextDecoration.LineThrough,
            ),
        ) {
            defaultContent()
        }

        is LinkInline -> {
            append("-> ")
            withStyle(SpanStyle(color = style.linkColor)) {
                defaultContent()
            }
        }

        is MathInline -> withStyle(
            SpanStyle(
                color = style.linkColor,
                textDecoration = TextDecoration.None,
            ),
        ) {
            append("math(${inline.formula})")
        }

        else -> defaultContent()
    }
}

@Composable
private fun debugInlineImageContent(): MarkdownInlineImageContent = { inlineImage, style ->
    withStyle(
        SpanStyle(
            color = style.linkColor,
            textDecoration = TextDecoration.Underline,
        ),
    ) {
        append("[img:${inlineImage.alt ?: inlineImage.url}]")
    }
}

@Composable
private fun debugHtmlBlockContent(): MarkdownHtmlBlockContent = { block ->
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Custom HTML Renderer",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = block.raw.trim(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun DebugToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

@Composable
private fun DebugBlockContent(
    block: MdBlock,
    defaultContent: @Composable () -> Unit,
) {
    when (block) {
        is HeadingBlock -> Card {
            Text(
                text = "Custom Heading: ${block.raw.trim()}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(12.dp),
            )
        }

        is QuoteBlock -> Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Custom Quote",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                defaultContent()
            }
        }

        is UnsupportedBlock -> Card {
            Text(
                text = "Unsupported block fallback:\n${block.raw.trimEnd()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp),
            )
        }

        is HtmlBlock -> Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "HTML Block",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                defaultContent()
            }
        }

        is MathBlock -> Card {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Math Block",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                defaultContent()
            }
        }

        else -> defaultContent()
    }
}

private fun debugSampleMarkdown(): String = """
# ComposeMarkdown Debug

> quote block with **bold** and *italic*

Paragraph with ~~strikethrough~~, `inline code`, and [link](https://kotlinlang.org)
Inline math: ${'$'}E = mc^2${'$'}
Inline image token: ![tiny-logo](https://kotlinlang.org/assets/images/twitter/general.png)

- item 1
  - nested item

1. ordered item

${'$'}${'$'}
\int_0^1 x^2 dx = \frac{1}{3}
${'$'}${'$'}

- [ ] todo one
- [x] todo done

| name | status | value |
| :--- | :----: | ----: |
| cpu  |  ok    | 42%   |
| mem  | warn   | 512mb |

![logo](https://kotlinlang.org/assets/images/twitter/general.png)

---

```kotlin
fun hello() = "world"
```

<details>
<summary>HTML block support</summary>
This block is rendered as raw HTML content.
</details>
""".trimIndent()
