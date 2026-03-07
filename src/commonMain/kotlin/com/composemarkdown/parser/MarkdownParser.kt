package com.composemarkdown.parser

import com.composemarkdown.model.CodeFenceBlock
import com.composemarkdown.model.CodeInline
import com.composemarkdown.model.EmInline
import com.composemarkdown.model.HeadingBlock
import com.composemarkdown.model.HtmlBlock
import com.composemarkdown.model.HrBlock
import com.composemarkdown.model.ImageBlock
import com.composemarkdown.model.InlineImage
import com.composemarkdown.model.LinkInline
import com.composemarkdown.model.ListBlock
import com.composemarkdown.model.ListItem
import com.composemarkdown.model.MathBlock
import com.composemarkdown.model.MathInline
import com.composemarkdown.model.MdBlock
import com.composemarkdown.model.MdDocument
import com.composemarkdown.model.MdInline
import com.composemarkdown.model.ParagraphBlock
import com.composemarkdown.model.QuoteBlock
import com.composemarkdown.model.StrikeInline
import com.composemarkdown.model.StrongInline
import com.composemarkdown.model.TableAlignment
import com.composemarkdown.model.TableBlock
import com.composemarkdown.model.TaskItem
import com.composemarkdown.model.TaskListBlock
import com.composemarkdown.model.TextInline
import com.composemarkdown.model.UnsupportedBlock
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

class MarkdownParser(
    flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor(),
) {
    private val parser = JetBrainsMarkdownParser(flavour)

    fun parse(source: String): MdDocument {
        val root = parser.buildMarkdownTreeFromString(source)
        val blocks = root.children.mapIndexed { index, node ->
            node.toBlock(source, index)
        }
        return MdDocument(source = source, blocks = blocks)
    }
}

private fun ASTNode.toBlock(source: String, index: Int): MdBlock {
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

        GFMElementTypes.BLOCK_MATH -> MathBlock(
            id = id,
            raw = raw,
            range = range,
            formula = parseMathNode(this, source),
        )

        MarkdownElementTypes.HTML_BLOCK -> HtmlBlock(
            id = id,
            raw = raw,
            range = range,
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
                alignments = raw.parseTableAlignments(),
                body = bodyNodes.map { it.extractTableCells(source) },
            )
        }

        MarkdownElementTypes.IMAGE -> {
            val image = parseImageInline(this, source)
            if (image != null) {
                ImageBlock(
                    id = id,
                    raw = raw,
                    range = range,
                    alt = image.alt,
                    url = image.url,
                    title = image.title,
                )
            } else {
                UnsupportedBlock(id = id, raw = raw, range = range)
            }
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

        else -> UnsupportedBlock(
            id = id,
            raw = raw,
            range = range,
        )
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
        val ordered = listNode.type == MarkdownElementTypes.ORDERED_LIST
        listNode.children
            .filter { it.type == MarkdownElementTypes.LIST_ITEM }
            .forEachIndexed { index, listItem ->
                val contentNodes = listItem.children.filter {
                    it.type != MarkdownElementTypes.UNORDERED_LIST &&
                        it.type != MarkdownElementTypes.ORDERED_LIST
                }
                val inlines = contentNodes.flatMap { parseInlineNode(it, source) }.normalizeInlines()
                if (inlines.isNotEmpty()) {
                    items += ListItem(
                        inlines = inlines,
                        indent = depth,
                        marker = if (ordered) "${index + 1}." else "-",
                    )
                }

                val checkBoxNode = listItem.findFirstDescendant(GFMTokenTypes.CHECK_BOX)
                if (checkBoxNode != null) {
                    hasTaskMarker = true
                    taskItems += TaskItem(
                        checked = checkBoxNode.getTextInNode(source).let { it.length > 1 && it[1] != ' ' },
                        text = inlines.toPlainText(),
                        inlines = inlines,
                        indent = depth,
                    )
                }

                listItem.children
                    .filter {
                        it.type == MarkdownElementTypes.UNORDERED_LIST ||
                            it.type == MarkdownElementTypes.ORDERED_LIST
                    }
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
        GFMElementTypes.STRIKETHROUGH -> listOf(StrikeInline(parseInlinesFromAst(node, source)))
        GFMElementTypes.INLINE_MATH -> listOf(MathInline(parseMathNode(node, source)))
        MarkdownElementTypes.CODE_SPAN -> listOf(CodeInline(parseCodeSpan(node, source)))
        MarkdownElementTypes.INLINE_LINK,
        MarkdownElementTypes.FULL_REFERENCE_LINK,
        MarkdownElementTypes.SHORT_REFERENCE_LINK,
        -> {
            val link = parseLinkInline(node, source)
            if (link != null) listOf(link) else parseInlinesFromAst(node, source)
        }

        MarkdownElementTypes.IMAGE -> {
            val image = parseImageInline(node, source)
            if (image != null) listOf(InlineImage(image.alt, image.url, image.title))
            else parseInlinesFromAst(node, source)
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
        GFMTokenTypes.CELL,
        -> listOf(TextInline(node.getTextInNode(source).toString()))

        GFMTokenTypes.GFM_AUTOLINK -> {
            val url = node.getTextInNode(source).toString()
            listOf(LinkInline(children = listOf(TextInline(url)), url = url, title = null))
        }

        MarkdownTokenTypes.ATX_HEADER,
        MarkdownTokenTypes.SETEXT_1,
        MarkdownTokenTypes.SETEXT_2,
        MarkdownTokenTypes.BACKTICK,
        MarkdownTokenTypes.EMPH,
        GFMTokenTypes.TILDE,
        MarkdownTokenTypes.LBRACKET,
        MarkdownTokenTypes.RBRACKET,
        MarkdownTokenTypes.LPAREN,
        MarkdownTokenTypes.RPAREN,
        MarkdownTokenTypes.EXCLAMATION_MARK,
        -> emptyList()

        else -> {
            if (node.children.isEmpty()) emptyList() else parseInlinesFromAst(node, source)
        }
    }
}

private fun parseCodeSpan(node: ASTNode, source: String): String {
    val payload = node.children
        .filter { it.type != MarkdownTokenTypes.BACKTICK }
        .joinToString(separator = "") { it.getTextInNode(source).toString() }
    return payload.trim()
}

private fun parseMathNode(node: ASTNode, source: String): String {
    return node.children
        .filter { it.type != GFMTokenTypes.DOLLAR }
        .joinToString(separator = "") { it.getTextInNode(source).toString() }
        .trim()
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
        parseInlinesFromAst(textNode, source)
            .ifEmpty { listOf(TextInline(textNode.getTextInNode(source).toString())) }
    } else {
        listOf(TextInline(url))
    }

    return LinkInline(children = children, url = url, title = title)
}

private data class ParsedImageData(
    val alt: String?,
    val url: String,
    val title: String?,
)

private val standaloneImageRegex = Regex(
    pattern = """^\s*!\[(.*?)]\((\S+?)(?:\s+["'](.*?)["'])?\)\s*$""",
)

private fun String.parseStandaloneImageFromRaw(): ParsedImageData? {
    val match = standaloneImageRegex.matchEntire(this) ?: return null
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

private fun String.parseTableAlignments(): List<TableAlignment> {
    val lines = lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toList()
    if (lines.size < 2) return emptyList()
    return lines[1]
        .trim('|')
        .split('|')
        .map { cell ->
            val token = cell.trim()
            when {
                token.startsWith(":") && token.endsWith(":") -> TableAlignment.Center
                token.endsWith(":") -> TableAlignment.End
                else -> TableAlignment.Start
            }
        }
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
            is StrikeInline -> inline.children.forEach(::appendInline)
            is CodeInline -> append(inline.code)
            is MathInline -> append(inline.formula)
            is LinkInline -> inline.children.forEach(::appendInline)
            is InlineImage -> append(inline.alt.orEmpty())
        }
    }
    forEach(::appendInline)
}
