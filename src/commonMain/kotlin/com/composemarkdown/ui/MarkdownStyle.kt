package com.composemarkdown.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp

@Immutable
data class MarkdownStyle(
    val paragraphTextStyle: TextStyle,
    val heading1TextStyle: TextStyle,
    val heading2TextStyle: TextStyle,
    val heading3TextStyle: TextStyle,
    val heading4TextStyle: TextStyle,
    val heading5TextStyle: TextStyle,
    val heading6TextStyle: TextStyle,
    val listTextStyle: TextStyle,
    val quoteTextStyle: TextStyle,
    val codeBlockTextStyle: TextStyle,
    val htmlBlockTextStyle: TextStyle,
    val mathBlockTextStyle: TextStyle,
    val tableHeaderTextStyle: TextStyle,
    val tableBodyTextStyle: TextStyle,
    val imageCaptionTextStyle: TextStyle,
    val blockSpacing: Dp,
    val quoteBorderColor: Color,
    val quoteBorderWidth: Dp,
    val codeBlockBackgroundColor: Color,
    val codeBlockShape: Shape,
    val codeBlockPadding: PaddingValues,
    val htmlBlockBackgroundColor: Color,
    val htmlBlockShape: Shape,
    val htmlBlockPadding: PaddingValues,
    val mathBlockBackgroundColor: Color,
    val mathBlockShape: Shape,
    val mathBlockPadding: PaddingValues,
    val inlineCodeBackgroundColor: Color,
    val inlineMathBackgroundColor: Color,
    val tableBorderColor: Color,
    val tableMinCellWidth: Dp,
    val tableCellPadding: PaddingValues,
    val imageMinHeight: Dp,
    val linkColor: Color,
    val linkTextDecoration: TextDecoration,
) {
    fun headingTextStyle(level: Int): TextStyle = when (level) {
        1 -> heading1TextStyle
        2 -> heading2TextStyle
        3 -> heading3TextStyle
        4 -> heading4TextStyle
        5 -> heading5TextStyle
        else -> heading6TextStyle
    }
}
