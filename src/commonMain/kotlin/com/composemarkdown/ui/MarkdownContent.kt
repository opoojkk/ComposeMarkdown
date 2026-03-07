package com.composemarkdown.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import com.composemarkdown.model.HtmlBlock
import com.composemarkdown.model.InlineImage
import com.composemarkdown.model.MdBlock
import com.composemarkdown.model.MdInline

typealias MarkdownBlockContent = @Composable (MdBlock, @Composable () -> Unit) -> Unit

typealias MarkdownHtmlBlockContent = @Composable (HtmlBlock) -> Unit

typealias MarkdownInlineImageContent = AnnotatedString.Builder.(InlineImage, MarkdownStyle) -> Unit

typealias MarkdownInlineContent = AnnotatedString.Builder.(MdInline, MarkdownStyle, () -> Unit) -> Unit
