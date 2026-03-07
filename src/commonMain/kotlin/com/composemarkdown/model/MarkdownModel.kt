package com.composemarkdown.model

data class MdDocument(
    val source: String,
    val blocks: List<MdBlock>,
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
    val marker: String,
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
    val indent: Int,
)

data class CodeFenceBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
    val lang: String?,
    val code: String,
) : MdBlock

data class MathBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
    val formula: String,
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
    val alignments: List<TableAlignment>,
    val body: List<List<String>>,
) : MdBlock

enum class TableAlignment {
    Start,
    Center,
    End,
}

data class ImageBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
    val alt: String?,
    val url: String,
    val title: String?,
) : MdBlock

data class HtmlBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
) : MdBlock

data class UnsupportedBlock(
    override val id: String,
    override val raw: String,
    override val range: IntRange,
) : MdBlock

sealed interface MdInline

data class TextInline(val text: String) : MdInline

data class EmInline(val children: List<MdInline>) : MdInline

data class StrongInline(val children: List<MdInline>) : MdInline

data class StrikeInline(val children: List<MdInline>) : MdInline

data class CodeInline(val code: String) : MdInline

data class MathInline(val formula: String) : MdInline

data class LinkInline(
    val children: List<MdInline>,
    val url: String,
    val title: String?,
) : MdInline

data class InlineImage(
    val alt: String?,
    val url: String,
    val title: String?,
) : MdInline
