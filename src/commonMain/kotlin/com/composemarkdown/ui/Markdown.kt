package com.composemarkdown.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.composemarkdown.model.ImageBlock
import com.composemarkdown.model.MdDocument
import com.composemarkdown.parser.MarkdownParser

@Composable
fun Markdown(
    markdown: String,
    modifier: Modifier = Modifier,
    parser: MarkdownParser = remember { MarkdownParser() },
    style: MarkdownStyle = MarkdownDefaults.style(),
    onLinkClick: (String) -> Unit = {},
    onImageClick: (String) -> Unit = {},
    imageContent: (@Composable (ImageBlock) -> Unit)? = null,
    htmlBlockContent: MarkdownHtmlBlockContent? = null,
    inlineImageContent: MarkdownInlineImageContent? = null,
    blockContent: MarkdownBlockContent? = null,
    inlineContent: MarkdownInlineContent? = null,
) {
    val document = remember(markdown, parser) { parser.parse(markdown) }
    Markdown(
        document = document,
        modifier = modifier,
        style = style,
        onLinkClick = onLinkClick,
        onImageClick = onImageClick,
        imageContent = imageContent,
        htmlBlockContent = htmlBlockContent,
        inlineImageContent = inlineImageContent,
        blockContent = blockContent,
        inlineContent = inlineContent,
    )
}

@Composable
fun Markdown(
    document: MdDocument,
    modifier: Modifier = Modifier,
    style: MarkdownStyle = MarkdownDefaults.style(),
    onLinkClick: (String) -> Unit = {},
    onImageClick: (String) -> Unit = {},
    imageContent: (@Composable (ImageBlock) -> Unit)? = null,
    htmlBlockContent: MarkdownHtmlBlockContent? = null,
    inlineImageContent: MarkdownInlineImageContent? = null,
    blockContent: MarkdownBlockContent? = null,
    inlineContent: MarkdownInlineContent? = null,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(style.blockSpacing),
    ) {
        items(document.blocks, key = { it.id }) { block ->
            val defaultContent: @Composable () -> Unit = {
                MarkdownBlock(
                    block = block,
                    style = style,
                    onLinkClick = onLinkClick,
                    onImageClick = onImageClick,
                    imageContent = imageContent,
                    htmlBlockContent = htmlBlockContent,
                    inlineImageContent = inlineImageContent,
                    inlineContent = inlineContent,
                )
            }
            if (blockContent != null) {
                blockContent(block, defaultContent)
            } else {
                defaultContent()
            }
        }
    }
}
