package com.composemarkdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownDebugApp() {
    var markdown by remember { mutableStateOf(debugSampleMarkdown()) }
    var parsed by remember { mutableStateOf<MdDocument?>(null) }
    var showSource by remember { mutableStateOf(false) }

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
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Switch(checked = showSource, onCheckedChange = { showSource = it })
                        Text(if (showSource) "Source On" else "Source Off")
                    }
                }

                val blocks = parsed?.blocks.orEmpty()
                Text("Blocks: ${blocks.size}")
                Text("Types: ${blocks.groupBy { it::class.simpleName }.map { "${it.key}:${it.value.size}" }.joinToString(", ")}")

                if (showSource) {
                    Text(
                        text = markdown,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                MarkdownBlockEditor(
                    markdown = markdown,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onMarkdownChange = { markdown = it },
                    onDocumentParsed = { parsed = it },
                    onLinkClick = { println("link: $it") },
                    onImageClick = { println("image: $it") },
                )
            }
        }
    }
}

private fun debugSampleMarkdown(): String = """
# ComposeMarkdown Debug

> quote block with **bold** and *italic*

- item 1
  - nested item
1. ordered item

- [ ] todo one
- [x] todo done

| name | value |
| :--- | ----: |
| cpu  | 42%   |
| mem  | 512mb |

`inline code` and [link](https://kotlinlang.org)

![logo](https://kotlinlang.org/assets/images/twitter/general.png)

---

```kotlin
fun hello() = "world"
```
""".trimIndent()
