package com.composemarkdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import coil3.compose.SubcomposeAsyncImage
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.MarkdownParser as JetBrainsMarkdownParser

data class MdDocument(
    val source: String,
    val blocks: List<MdBlock>,
    val editingId: String? = null,
)

sealed interface MdBlock {
    val id: String
    val raw: String
    val range: IntRange
}

data class ParagraphBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
    val inlines: List<MdInline>,
) : MdBlock

data class HeadingBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
    val level: Int,
    val inlines: List<MdInline>,
) : MdBlock

data class QuoteBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
    val inlines: List<MdInline>,
) : MdBlock

data class ListBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
    val ordered: Boolean,
    val items: List<ListItem>,
) : MdBlock

data class ListItem(
    val inlines: List<MdInline>,
    val indent: Int,
)

data class TaskListBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
    val items: List<TaskItem>,
) : MdBlock

data class TaskItem(
    val checked: Boolean,
    val text: String,
    val inlines: List<MdInline>,
)

data class CodeFenceBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
    val lang: String?,
    val code: String,
) : MdBlock

data class HrBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
) : MdBlock

data class TableBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
    val header: List<String>,
    val body: List<List<String>>,
) : MdBlock

data class ImageBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
    val alt: String?,
    val url: String,
    val title: String?,
) : MdBlock

sealed interface MdInline
data class TextInline(val text: String) : MdInline
data class EmInline(val children: List<MdInline>) : MdInline
data class StrongInline(val children: List<MdInline>) : MdInline
data class CodeInline(val code: String) : MdInline
data class LinkInline(val children: List<MdInline>, val url: String, val title: String?) : MdInline
data class InlineImage(val alt: String?, val url: String, val title: String?) : MdInline

class MarkdownParser(
    flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor(),
) {
    private val parser = JetBrainsMarkdownParser(flavour)

    fun parse(source: String): MdDocument {
        val root = parser.buildMarkdownTreeFromString(source)
        val blocks = root.children.mapIndexedNotNull { index, node ->
            node.toBlock(source, index)
        }
        return MdDocument(
            source = source,
            blocks = blocks,
            editingId = null,
        )
    }
}

private fun ASTNode.toBlock(source: String, index: Int): MdBlock? {
    val id = "b_${startOffset}_${endOffset}_$index"
    val range = startOffset..(endOffset - 1).coerceAtLeast(startOffset)
    val raw = source.substring(startOffset, endOffset)
    return when (type) {
        MarkdownElementTypes.PARAGRAPH -> {
            val inlines = parseInlinesFromAst(this, source)
            val standaloneImage = inlines.standaloneInlineImage()
            val rawImage = standaloneImage ?: raw.parseStandaloneImageFromRaw()?.let {
                InlineImage(it.alt, it.url, it.title)
            }
            if (rawImage != null) {
                ImageBlock(
                    id = id,
                    raw = raw,
                    range = range,
                    alt = rawImage.alt,
                    url = rawImage.url,
                    title = rawImage.title,
                )
            } else {
                ParagraphBlock(
                    id = id,
                    raw = raw,
                    range = range,
                    inlines = inlines,
                )
            }
        }
        MarkdownElementTypes.ATX_1, MarkdownElementTypes.SETEXT_1 -> HeadingBlock(
            id = id,
            raw = raw,
            range = range,
            level = 1,
            inlines = parseInlinesFromAst(this, source),
        )
        MarkdownElementTypes.ATX_2, MarkdownElementTypes.SETEXT_2 -> HeadingBlock(
            id = id,
            raw = raw,
            range = range,
            level = 2,
            inlines = parseInlinesFromAst(this, source),
        )
        MarkdownElementTypes.ATX_3 -> HeadingBlock(
            id = id,
            raw = raw,
            range = range,
            level = 3,
            inlines = parseInlinesFromAst(this, source),
        )
        MarkdownElementTypes.ATX_4 -> HeadingBlock(
            id = id,
            raw = raw,
            range = range,
            level = 4,
            inlines = parseInlinesFromAst(this, source),
        )
        MarkdownElementTypes.ATX_5 -> HeadingBlock(
            id = id,
            raw = raw,
            range = range,
            level = 5,
            inlines = parseInlinesFromAst(this, source),
        )
        MarkdownElementTypes.ATX_6 -> HeadingBlock(
            id = id,
            raw = raw,
            range = range,
            level = 6,
            inlines = parseInlinesFromAst(this, source),
        )
        MarkdownElementTypes.BLOCK_QUOTE -> QuoteBlock(
            id = id,
            raw = raw,
            range = range,
            inlines = parseInlinesFromAst(this, source),
        )
        MarkdownElementTypes.CODE_FENCE -> {
            val lang = findChildOfType(MarkdownTokenTypes.FENCE_LANG)
                ?.getTextInNode(source)
                ?.toString()
                ?.trim()
                ?.ifBlank { null }
            val code = children
                .filter { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }
                .joinToString(separator = "") { it.getTextInNode(source).toString() }
            CodeFenceBlock(
                id = id,
                raw = raw,
                range = range,
                lang = lang,
                code = code,
            )
        }
        MarkdownElementTypes.CODE_BLOCK -> CodeFenceBlock(
            id = id,
            raw = raw,
            range = range,
            lang = null,
            code = children
                .filter { it.type == MarkdownTokenTypes.CODE_LINE }
                .joinToString(separator = "") { it.getTextInNode(source).toString() },
        )
        MarkdownTokenTypes.HORIZONTAL_RULE -> HrBlock(
            id = id,
            raw = raw,
            range = range,
        )
        GFMElementTypes.TABLE -> {
            val headerNode = children.firstOrNull { it.type == GFMElementTypes.HEADER }
            val bodyNodes = children.filter { it.type == GFMElementTypes.ROW }
            TableBlock(
                id = id,
                raw = raw,
                range = range,
                header = headerNode?.extractTableCells(source).orEmpty(),
                body = bodyNodes.map { it.extractTableCells(source) },
            )
        }
        MarkdownElementTypes.IMAGE -> {
            val image = parseImageInline(this, source) ?: return null
            ImageBlock(
                id = id,
                raw = raw,
                range = range,
                alt = image.alt,
                url = image.url,
                title = image.title,
            )
        }
        MarkdownElementTypes.UNORDERED_LIST, MarkdownElementTypes.ORDERED_LIST -> {
            val parsed = parseListNode(this, source)
            if (parsed.hasTaskMarker) {
                TaskListBlock(
                    id = id,
                    raw = raw,
                    range = range,
                    items = parsed.taskItems,
                )
            } else {
                ListBlock(
                    id = id,
                    raw = raw,
                    range = range,
                    ordered = type == MarkdownElementTypes.ORDERED_LIST,
                    items = parsed.items,
                )
            }
        }
        else -> null
    }
}

private data class ParsedListData(
    val items: List<ListItem>,
    val taskItems: List<TaskItem>,
    val hasTaskMarker: Boolean,
)

private fun parseListNode(node: ASTNode, source: String): ParsedListData {
    val items = mutableListOf<ListItem>()
    val taskItems = mutableListOf<TaskItem>()
    var hasTaskMarker = false

    fun walkList(listNode: ASTNode, depth: Int) {
        listNode.children
            .filter { it.type == MarkdownElementTypes.LIST_ITEM }
            .forEach { listItem ->
                val contentNodes = listItem.children.filter {
                    it.type != MarkdownElementTypes.UNORDERED_LIST && it.type != MarkdownElementTypes.ORDERED_LIST
                }
                val inlines = contentNodes.flatMap { parseInlineNode(it, source) }.normalizeInlines()
                if (inlines.isNotEmpty()) {
                    items += ListItem(inlines = inlines, indent = depth)
                }

                val checkBoxNode = listItem.findFirstDescendant(GFMTokenTypes.CHECK_BOX)
                if (checkBoxNode != null) {
                    hasTaskMarker = true
                    taskItems += TaskItem(
                        checked = checkBoxNode.getTextInNode(source).let { it.length > 1 && it[1] != ' ' },
                        text = inlines.toPlainText(),
                        inlines = inlines,
                    )
                }

                listItem.children
                    .filter { it.type == MarkdownElementTypes.UNORDERED_LIST || it.type == MarkdownElementTypes.ORDERED_LIST }
                    .forEach { nested -> walkList(nested, depth + 1) }
            }
    }

    walkList(node, 0)
    return ParsedListData(items = items, taskItems = taskItems, hasTaskMarker = hasTaskMarker)
}

private fun ASTNode.extractTableCells(source: String): List<String> =
    children
        .filter { it.type == GFMTokenTypes.CELL }
        .map { parseInlinesFromAst(it, source).toPlainText().trim() }

private fun parseInlinesFromAst(node: ASTNode, source: String): List<MdInline> =
    node.children.flatMap { parseInlineNode(it, source) }.normalizeInlines()

private fun parseInlineNode(node: ASTNode, source: String): List<MdInline> {
    return when (node.type) {
        MarkdownElementTypes.EMPH -> listOf(EmInline(parseInlinesFromAst(node, source)))
        MarkdownElementTypes.STRONG -> listOf(StrongInline(parseInlinesFromAst(node, source)))
        MarkdownElementTypes.CODE_SPAN -> listOf(CodeInline(parseCodeSpan(node, source)))
        MarkdownElementTypes.INLINE_LINK, MarkdownElementTypes.FULL_REFERENCE_LINK, MarkdownElementTypes.SHORT_REFERENCE_LINK -> {
            val link = parseLinkInline(node, source)
            if (link != null) listOf(link) else parseInlinesFromAst(node, source)
        }
        MarkdownElementTypes.IMAGE -> {
            val image = parseImageInline(node, source)
            if (image != null) listOf(InlineImage(image.alt, image.url, image.title)) else parseInlinesFromAst(node, source)
        }
        MarkdownElementTypes.AUTOLINK -> {
            val url = node.getTextInNode(source).toString().trim('<', '>')
            listOf(LinkInline(children = listOf(TextInline(url)), url = url, title = null))
        }
        MarkdownTokenTypes.TEXT,
        MarkdownTokenTypes.WHITE_SPACE,
        MarkdownTokenTypes.EOL,
        MarkdownTokenTypes.ATX_CONTENT,
        MarkdownTokenTypes.SETEXT_CONTENT,
        MarkdownTokenTypes.CODE_LINE,
        GFMTokenTypes.CELL -> listOf(TextInline(node.getTextInNode(source).toString()))
        GFMTokenTypes.GFM_AUTOLINK -> {
            val url = node.getTextInNode(source).toString()
            listOf(LinkInline(children = listOf(TextInline(url)), url = url, title = null))
        }
        MarkdownTokenTypes.ATX_HEADER,
        MarkdownTokenTypes.SETEXT_1,
        MarkdownTokenTypes.SETEXT_2,
        MarkdownTokenTypes.BACKTICK,
        MarkdownTokenTypes.EMPH,
        MarkdownTokenTypes.LBRACKET,
        MarkdownTokenTypes.RBRACKET,
        MarkdownTokenTypes.LPAREN,
        MarkdownTokenTypes.RPAREN,
        MarkdownTokenTypes.EXCLAMATION_MARK -> emptyList()
        else -> {
            if (node.children.isEmpty()) emptyList()
            else parseInlinesFromAst(node, source)
        }
    }
}

private fun parseCodeSpan(node: ASTNode, source: String): String {
    val payload = node.children
        .filter { it.type != MarkdownTokenTypes.BACKTICK }
        .joinToString(separator = "") { it.getTextInNode(source).toString() }
    return payload.trim()
}

private fun parseLinkInline(node: ASTNode, source: String): LinkInline? {
    val textNode = node.findChildOfType(MarkdownElementTypes.LINK_TEXT)
    val destinationNode = node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)
    val titleNode = node.findChildOfType(MarkdownElementTypes.LINK_TITLE)

    val url = destinationNode
        ?.getTextInNode(source)
        ?.toString()
        ?.trim()
        ?.trim('<', '>')
        ?.ifBlank { null }
        ?: return null

    val title = titleNode
        ?.getTextInNode(source)
        ?.toString()
        ?.trim()
        ?.removeSurrounding("\"")
        ?.removeSurrounding("'")
        ?.removeSurrounding("(", ")")
        ?.ifBlank { null }

    val children = if (textNode != null) {
        parseInlinesFromAst(textNode, source).ifEmpty { listOf(TextInline(textNode.getTextInNode(source).toString())) }
    } else {
        listOf(TextInline(url))
    }

    return LinkInline(children = children, url = url, title = title)
}

private data class ParsedImageData(val alt: String?, val url: String, val title: String?)

private val StandaloneImageRegex = Regex(
    pattern = """^\s*!\[(.*?)]\((\S+?)(?:\s+["'](.*?)["'])?\)\s*$""",
)

private fun String.parseStandaloneImageFromRaw(): ParsedImageData? {
    val match = StandaloneImageRegex.matchEntire(this) ?: return null
    val alt = match.groupValues[1].ifBlank { null }
    val url = match.groupValues[2].ifBlank { return null }
    val title = match.groupValues.getOrNull(3)?.ifBlank { null }
    return ParsedImageData(alt = alt, url = url, title = title)
}

private fun parseImageInline(node: ASTNode, source: String): ParsedImageData? {
    val textNode = node.findChildOfType(MarkdownElementTypes.LINK_TEXT)
    val destinationNode = node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)
    val titleNode = node.findChildOfType(MarkdownElementTypes.LINK_TITLE)

    val url = destinationNode
        ?.getTextInNode(source)
        ?.toString()
        ?.trim()
        ?.trim('<', '>')
        ?.ifBlank { null }
        ?: return null

    val alt = textNode?.let { parseInlinesFromAst(it, source).toPlainText() }?.ifBlank { null }
    val title = titleNode
        ?.getTextInNode(source)
        ?.toString()
        ?.trim()
        ?.removeSurrounding("\"")
        ?.removeSurrounding("'")
        ?.removeSurrounding("(", ")")
        ?.ifBlank { null }

    return ParsedImageData(alt = alt, url = url, title = title)
}

private fun ASTNode.findFirstDescendant(type: org.intellij.markdown.IElementType): ASTNode? {
    if (this.type == type) return this
    for (child in children) {
        val nested = child.findFirstDescendant(type)
        if (nested != null) return nested
    }
    return null
}

private fun List<MdInline>.normalizeInlines(): List<MdInline> {
    if (isEmpty()) return this
    val merged = mutableListOf<MdInline>()
    for (item in this) {
        val last = merged.lastOrNull()
        if (last is TextInline && item is TextInline) {
            merged[merged.lastIndex] = TextInline(last.text + item.text)
        } else {
            merged += item
        }
    }
    return merged
}

private fun List<MdInline>.standaloneInlineImage(): InlineImage? {
    val meaningful = filterNot { it is TextInline && it.text.isBlank() }
    return if (meaningful.size == 1) meaningful.firstOrNull() as? InlineImage else null
}

private fun List<MdInline>.toPlainText(): String = buildString {
    fun appendInline(inline: MdInline) {
        when (inline) {
            is TextInline -> append(inline.text)
            is EmInline -> inline.children.forEach(::appendInline)
            is StrongInline -> inline.children.forEach(::appendInline)
            is CodeInline -> append(inline.code)
            is LinkInline -> inline.children.forEach(::appendInline)
            is InlineImage -> append(inline.alt.orEmpty())
        }
    }
    forEach(::appendInline)
}

@Composable
fun MarkdownBlockEditor(
    markdown: String,
    modifier: Modifier = Modifier,
    parser: MarkdownParser = remember { MarkdownParser() },
    onMarkdownChange: (String) -> Unit,
    onDocumentParsed: (MdDocument) -> Unit = {},
    onLinkClick: (String) -> Unit = {},
    onImageClick: (String) -> Unit = {},
) {
    var document by remember(markdown) { mutableStateOf(parser.parse(markdown)) }
    var editingId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(markdown) {
        document = parser.parse(markdown).copy(editingId = editingId)
        onDocumentParsed(document)
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(document.blocks, key = { it.id }) { block ->
            if (editingId == block.id) {
                EditingBlock(
                    block = block,
                    onCommit = { newRaw ->
                        val newSource = replaceRange(document.source, block.range, newRaw)
                        editingId = null
                        onMarkdownChange(newSource)
                        document = parser.parse(newSource)
                        onDocumentParsed(document)
                    },
                )
            } else {
                RenderBlock(
                    block = block,
                    onClick = { editingId = block.id },
                    onToggleTask = { itemIndex, checked ->
                        val task = block as? TaskListBlock ?: return@RenderBlock
                        val lines = task.raw.split('\n').toMutableList()
                        if (itemIndex !in lines.indices) return@RenderBlock
                        lines[itemIndex] = lines[itemIndex]
                            .replaceFirst("- [ ] ", if (checked) "- [x] " else "- [ ] ")
                            .replaceFirst("- [x] ", if (checked) "- [x] " else "- [ ] ")
                            .replaceFirst("- [X] ", if (checked) "- [x] " else "- [ ] ")
                        val newRaw = lines.joinToString("\n")
                        val newSource = replaceRange(document.source, block.range, newRaw)
                        onMarkdownChange(newSource)
                        document = parser.parse(newSource)
                        onDocumentParsed(document)
                    },
                    onLinkClick = onLinkClick,
                    onImageClick = onImageClick,
                )
            }
        }
    }
}

private fun replaceRange(source: String, range: IntRange, replacement: String): String {
    if (range.first < 0 || range.last >= source.length || range.first > range.last) return source
    return buildString(source.length - (range.last - range.first + 1) + replacement.length) {
        append(source.substring(0, range.first))
        append(replacement)
        append(source.substring(range.last + 1))
    }
}

@Composable
private fun EditingBlock(
    block: MdBlock,
    onCommit: (String) -> Unit,
) {
    var value by remember(block.id) { mutableStateOf(block.raw) }
    BasicTextField(
        value = value,
        onValueChange = { value = it },
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
            .onFocusChanged { state ->
                if (!state.isFocused) onCommit(value)
            },
    )
}

@Composable
private fun RenderBlock(
    block: MdBlock,
    onClick: () -> Unit,
    onToggleTask: (itemIndex: Int, checked: Boolean) -> Unit,
    onLinkClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
) {
    when (block) {
        is ParagraphBlock -> InlineText(
            annotated = inlineToAnnotated(block.inlines),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            onLinkClick = onLinkClick,
        )
        is HeadingBlock -> InlineText(
            annotated = inlineToAnnotated(block.inlines),
            style = when (block.level) {
                1 -> MaterialTheme.typography.headlineLarge
                2 -> MaterialTheme.typography.headlineMedium
                3 -> MaterialTheme.typography.headlineSmall
                4 -> MaterialTheme.typography.titleLarge
                5 -> MaterialTheme.typography.titleMedium
                else -> MaterialTheme.typography.titleSmall
            },
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            onLinkClick = onLinkClick,
        )
        is QuoteBlock -> Box(
            Modifier
                .fillMaxWidth()
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant)
                .padding(8.dp)
                .clickable(onClick = onClick),
        ) {
            InlineText(
                annotated = inlineToAnnotated(block.inlines),
                style = MaterialTheme.typography.bodyLarge,
                onLinkClick = onLinkClick,
            )
        }
        is ListBlock -> Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        ) {
            block.items.forEachIndexed { index, item ->
                Row {
                    val marker = if (block.ordered) "${index + 1}." else "-"
                    Text(" ".repeat(item.indent * 2) + marker + " ")
                    InlineText(
                        annotated = inlineToAnnotated(item.inlines),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        onLinkClick = onLinkClick,
                    )
                }
            }
        }
        is TaskListBlock -> Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        ) {
            block.items.forEachIndexed { idx, task ->
                Row {
                    Checkbox(
                        checked = task.checked,
                        onCheckedChange = { checked -> onToggleTask(idx, checked) },
                    )
                    InlineText(
                        annotated = inlineToAnnotated(task.inlines),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        onLinkClick = onLinkClick,
                    )
                }
            }
        }
        is CodeFenceBlock -> Text(
            text = block.code,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
                .clickable(onClick = onClick),
        )
        is HrBlock -> Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
                .clickable(onClick = onClick),
        )
        is TableBlock -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .clickable(onClick = onClick),
        ) {
            TableRow(block.header, isHeader = true)
            block.body.forEach { row -> TableRow(row, isHeader = false) }
        }
        is ImageBlock -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onImageClick(block.url) },
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SubcomposeAsyncImage(
                model = block.url,
                contentDescription = block.alt,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                loading = {
                    Text(
                        text = "Loading image...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                error = {
                    Text(
                        text = "Image load failed: ${block.url}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                },
            )
            if (!block.alt.isNullOrBlank()) {
                Text(
                    text = block.alt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
@Suppress("DEPRECATION")
private fun InlineText(
    annotated: AnnotatedString,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit,
) {
    ClickableText(
        text = annotated,
        style = style,
        modifier = modifier,
        onClick = { offset ->
            annotated.getStringAnnotations("link", offset, offset)
                .firstOrNull()
                ?.let { onLinkClick(it.item) }
        },
    )
}

@Composable
private fun TableRow(cells: List<String>, isHeader: Boolean) {
    Row(Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        cells.forEach { cell ->
            val style = if (isHeader) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
            Text(
                text = cell,
                style = style,
                modifier = Modifier
                    .widthIn(min = 120.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun inlineToAnnotated(inlines: List<MdInline>): AnnotatedString = buildAnnotatedString {
    fun appendInline(node: MdInline) {
        when (node) {
            is TextInline -> append(node.text)
            is EmInline -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                node.children.forEach(::appendInline)
            }
            is StrongInline -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                node.children.forEach(::appendInline)
            }
            is CodeInline -> withStyle(SpanStyle(background = Color(0xFFECECEC))) {
                append(node.code)
            }
            is LinkInline -> {
                val start = length
                node.children.forEach(::appendInline)
                val end = length
                addStyle(
                    SpanStyle(
                        color = Color(0xFF1565C0),
                        textDecoration = TextDecoration.Underline,
                    ),
                    start,
                    end,
                )
                addStringAnnotation("link", node.url, start, end)
            }
            is InlineImage -> {
                append("[image: ${node.alt.orEmpty()}]")
            }
        }
    }
    inlines.forEach(::appendInline)
}
