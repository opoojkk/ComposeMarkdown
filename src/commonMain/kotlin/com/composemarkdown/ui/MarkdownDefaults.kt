package com.composemarkdown.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

object MarkdownDefaults {
    @Composable
    fun style(): MarkdownStyle = MarkdownStyle(
        paragraphTextStyle = MaterialTheme.typography.bodyLarge,
        heading1TextStyle = MaterialTheme.typography.headlineLarge,
        heading2TextStyle = MaterialTheme.typography.headlineMedium,
        heading3TextStyle = MaterialTheme.typography.headlineSmall,
        heading4TextStyle = MaterialTheme.typography.titleLarge,
        heading5TextStyle = MaterialTheme.typography.titleMedium,
        heading6TextStyle = MaterialTheme.typography.titleSmall,
        listTextStyle = MaterialTheme.typography.bodyLarge,
        quoteTextStyle = MaterialTheme.typography.bodyLarge,
        codeBlockTextStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        htmlBlockTextStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        mathBlockTextStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        tableHeaderTextStyle = MaterialTheme.typography.titleSmall,
        tableBodyTextStyle = MaterialTheme.typography.bodyMedium,
        imageCaptionTextStyle = MaterialTheme.typography.bodySmall,
        blockSpacing = 8.dp,
        quoteBorderColor = MaterialTheme.colorScheme.outlineVariant,
        quoteBorderWidth = 2.dp,
        codeBlockBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        codeBlockShape = RoundedCornerShape(8.dp),
        codeBlockPadding = PaddingValues(12.dp),
        htmlBlockBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        htmlBlockShape = RoundedCornerShape(8.dp),
        htmlBlockPadding = PaddingValues(12.dp),
        mathBlockBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        mathBlockShape = RoundedCornerShape(8.dp),
        mathBlockPadding = PaddingValues(12.dp),
        inlineCodeBackgroundColor = Color(0xFFECECEC),
        inlineMathBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        tableBorderColor = MaterialTheme.colorScheme.outlineVariant,
        tableMinCellWidth = 120.dp,
        tableCellPadding = PaddingValues(8.dp),
        imageMinHeight = 120.dp,
        linkColor = Color(0xFF1565C0),
        linkTextDecoration = TextDecoration.Underline,
    )
}
