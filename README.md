# ComposeMarkdown

Block-first Markdown editor component for Compose Multiplatform.

## Dependency (KMP commonMain)

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("org.jetbrains:markdown:0.7.3")
    }
}
```

## What is implemented

- `MdDocument` + `MdBlock`/`MdInline` data model
- `MarkdownParser` (JetBrains Markdown AST -> block/inline render model)
- `MarkdownBlockEditor` composable:
  - `LazyColumn` with stable block keys
  - only current block enters edit mode (`BasicTextField`)
  - other blocks stay in render mode
  - on blur commit and full re-parse
  - task checkbox toggle writes back to markdown source

Implemented file:
- `/Users/xx/Projects/ComposeMarkdown/src/commonMain/kotlin/com/composemarkdown/MarkdownEditor.kt`

## Supported syntax (current)

- Heading
- Paragraph
- Quote
- List (ordered/unordered, basic nesting by indent)
- Task list
- Code fence
- Horizontal rule
- Table (basic pipe table)
- Image block
- Inline: emphasis, strong, code, link, inline image token

## Usage

```kotlin
var markdown by remember { mutableStateOf("# Title\n\n- [ ] task") }

MarkdownBlockEditor(
    markdown = markdown,
    onMarkdownChange = { markdown = it },
    onLinkClick = { url -> /* open link */ },
    onImageClick = { url -> /* open image */ },
)
```

## Debug entry

- Desktop:
  - `./gradlew run`
- Android:
  - set `ANDROID_HOME` or create `local.properties` with `sdk.dir=...`
  - then run `./gradlew installDebug`

Debug app file:
- `/Users/xx/Projects/ComposeMarkdown/src/commonMain/kotlin/com/composemarkdown/MarkdownDebugApp.kt`

## Next step

Add neighborhood incremental re-parse (`edited block +/- N lines`) and table alignment metadata mapping.
