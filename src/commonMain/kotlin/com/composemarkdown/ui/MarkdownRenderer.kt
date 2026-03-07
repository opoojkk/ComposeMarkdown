package com.composemarkdown.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
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
import com.composemarkdown.model.MdBlock
import com.composemarkdown.model.MdInline
import com.composemarkdown.model.MathBlock
import com.composemarkdown.model.MathInline
import com.composemarkdown.model.ParagraphBlock
import com.composemarkdown.model.QuoteBlock
import com.composemarkdown.model.StrikeInline
import com.composemarkdown.model.StrongInline
import com.composemarkdown.model.TableAlignment
import com.composemarkdown.model.TableBlock
import com.composemarkdown.model.TaskListBlock
import com.composemarkdown.model.TextInline
import com.composemarkdown.model.UnsupportedBlock

@Composable
internal fun MarkdownBlock(
    block: MdBlock,
    style: MarkdownStyle,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    imageContent: (@Composable (ImageBlock) -> Unit)?,
    htmlBlockContent: MarkdownHtmlBlockContent?,
    inlineImageContent: MarkdownInlineImageContent?,
    inlineContent: MarkdownInlineContent?,
) {
    when (block) {
        is ParagraphBlock -> MarkdownInlineText(
            annotated = inlineToAnnotated(block.inlines, style, inlineImageContent, inlineContent),
            style = style.paragraphTextStyle,
            modifier = modifier.fillMaxWidth(),
            onLinkClick = onLinkClick,
        )

        is HeadingBlock -> MarkdownInlineText(
            annotated = inlineToAnnotated(block.inlines, style, inlineImageContent, inlineContent),
            style = style.headingTextStyle(block.level),
            modifier = modifier.fillMaxWidth(),
            onLinkClick = onLinkClick,
        )

        is QuoteBlock -> Box(
            modifier
                .fillMaxWidth()
                .border(style.quoteBorderWidth, style.quoteBorderColor)
                .padding(8.dp),
        ) {
            MarkdownInlineText(
                annotated = inlineToAnnotated(block.inlines, style, inlineImageContent, inlineContent),
                style = style.quoteTextStyle,
                onLinkClick = onLinkClick,
            )
        }

        is ListBlock -> Column(
            modifier = modifier.fillMaxWidth(),
        ) {
            block.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = (item.indent * 16).dp),
                ) {
                    Text("${item.marker} ", style = style.listTextStyle)
                    MarkdownInlineText(
                        annotated = inlineToAnnotated(item.inlines, style, inlineImageContent, inlineContent),
                        style = style.listTextStyle,
                        modifier = Modifier.fillMaxWidth(),
                        onLinkClick = onLinkClick,
                    )
                }
            }
        }

        is TaskListBlock -> Column(
            modifier = modifier.fillMaxWidth(),
        ) {
            block.items.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = (task.indent * 16).dp),
                ) {
                    Checkbox(
                        checked = task.checked,
                        onCheckedChange = null,
                    )
                    MarkdownInlineText(
                        annotated = inlineToAnnotated(task.inlines, style, inlineImageContent, inlineContent),
                        style = style.listTextStyle,
                        modifier = Modifier.fillMaxWidth(),
                        onLinkClick = onLinkClick,
                    )
                }
            }
        }

        is CodeFenceBlock -> Text(
            text = block.code,
            style = style.codeBlockTextStyle,
            modifier = modifier
                .fillMaxWidth()
                .clip(style.codeBlockShape)
                .background(style.codeBlockBackgroundColor)
                .padding(style.codeBlockPadding),
        )

        is MathBlock -> Text(
            text = block.formula,
            style = style.mathBlockTextStyle,
            modifier = modifier
                .fillMaxWidth()
                .clip(style.mathBlockShape)
                .background(style.mathBlockBackgroundColor)
                .padding(style.mathBlockPadding),
        )

        is HtmlBlock -> {
            if (htmlBlockContent != null) {
                htmlBlockContent(block)
            } else {
                Text(
                    text = block.raw.trimEnd(),
                    style = style.htmlBlockTextStyle,
                    modifier = modifier
                        .fillMaxWidth()
                        .clip(style.htmlBlockShape)
                        .background(style.htmlBlockBackgroundColor)
                        .padding(style.htmlBlockPadding),
                )
            }
        }

        is HrBlock -> Box(
            modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(style.tableBorderColor),
        )

        is TableBlock -> Column(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            if (block.header.isNotEmpty()) {
                MarkdownTableRow(
                    cells = block.header,
                    alignments = block.alignments,
                    isHeader = true,
                    style = style,
                )
            }
            block.body.forEach { row ->
                MarkdownTableRow(
                    cells = row,
                    alignments = block.alignments,
                    isHeader = false,
                    style = style,
                )
            }
        }

        is ImageBlock -> {
            if (imageContent != null) {
                imageContent(block)
            } else {
                DefaultMarkdownImage(
                    block = block,
                    style = style,
                    modifier = modifier,
                    onImageClick = onImageClick,
                )
            }
        }

        is UnsupportedBlock -> Text(
            text = block.raw.trimEnd(),
            style = style.paragraphTextStyle,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DefaultMarkdownImage(
    block: ImageBlock,
    style: MarkdownStyle,
    modifier: Modifier = Modifier,
    onImageClick: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onImageClick(block.url) },
    ) {
        SubcomposeAsyncImage(
            model = block.url,
            contentDescription = block.alt,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = style.imageMinHeight),
            loading = {
                Text(
                    text = "Loading image...",
                    style = style.imageCaptionTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            error = {
                Text(
                    text = "Image load failed: ${block.url}",
                    style = style.imageCaptionTextStyle,
                    color = MaterialTheme.colorScheme.error,
                )
            },
        )
        if (!block.alt.isNullOrBlank()) {
            Text(
                text = block.alt,
                style = style.imageCaptionTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        if (block.url.isNotBlank()) {
            Text(
                text = block.url,
                style = style.imageCaptionTextStyle,
                color = style.linkColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
@Suppress("DEPRECATION")
private fun MarkdownInlineText(
    annotated: AnnotatedString,
    style: TextStyle,
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
private fun MarkdownTableRow(
    cells: List<String>,
    alignments: List<TableAlignment>,
    isHeader: Boolean,
    style: MarkdownStyle,
) {
    Row(Modifier.border(1.dp, style.tableBorderColor)) {
        cells.forEachIndexed { index, cell ->
            val baseStyle = if (isHeader) style.tableHeaderTextStyle else style.tableBodyTextStyle
            val textAlign = when (alignments.getOrElse(index) { TableAlignment.Start }) {
                TableAlignment.Start -> TextAlign.Start
                TableAlignment.Center -> TextAlign.Center
                TableAlignment.End -> TextAlign.End
            }
            Box(
                modifier = Modifier
                    .widthIn(min = style.tableMinCellWidth)
                    .border(1.dp, style.tableBorderColor)
                    .padding(style.tableCellPadding),
            ) {
                Text(
                    text = cell,
                    style = baseStyle.copy(textAlign = textAlign),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun inlineToAnnotated(
    inlines: List<MdInline>,
    style: MarkdownStyle,
    inlineImageContent: MarkdownInlineImageContent?,
    inlineContent: MarkdownInlineContent?,
): AnnotatedString = buildAnnotatedString {
    lateinit var appendInline: (MdInline) -> Unit
    val appendInlineDefault: (MdInline) -> Unit = { node ->
        when (node) {
            is TextInline -> append(node.text)
            is EmInline -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                node.children.forEach(appendInline)
            }

            is StrongInline -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                node.children.forEach(appendInline)
            }

            is StrikeInline -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                node.children.forEach(appendInline)
            }

            is CodeInline -> withStyle(
                SpanStyle(
                    background = style.inlineCodeBackgroundColor,
                    fontWeight = FontWeight.Medium,
                ),
            ) {
                append(node.code)
            }

            is MathInline -> withStyle(
                SpanStyle(
                    background = style.inlineMathBackgroundColor,
                    fontWeight = FontWeight.Medium,
                ),
            ) {
                append(node.formula)
            }

            is LinkInline -> {
                val start = length
                node.children.forEach(appendInline)
                val end = length
                addStyle(
                    SpanStyle(
                        color = style.linkColor,
                        textDecoration = style.linkTextDecoration,
                    ),
                    start,
                    end,
                )
                addStringAnnotation("link", node.url, start, end)
            }

            is InlineImage -> {
                if (inlineImageContent != null) {
                    inlineImageContent(node, style)
                } else {
                    append("[image: ${node.alt.orEmpty()}]")
                }
            }
        }
    }

    appendInline = { node ->
        if (node is InlineImage && inlineImageContent != null) {
            appendInlineDefault(node)
        } else if (inlineContent != null) {
            inlineContent(node, style) { appendInlineDefault(node) }
        } else {
            appendInlineDefault(node)
        }
    }

    inlines.forEach(appendInline)
}
