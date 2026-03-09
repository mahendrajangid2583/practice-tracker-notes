package com.aashu.privatesuite.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReadNoteDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit,
    onLinkClick: (String) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            // Simple text for now, or use a markdown renderer if available/needed.
            // Since we stored it as simple string/markdown.
            // For a "Senior Dev" touch, we could try to render markdown, but standard Text is safer for now
            // to avoid unresolved reference "Markwon" which is a library we might not have added to Compose.
            // Wait, we don't have Markwon in dependencies list provided.
            // So we'll use a scrollable Text.
            /*
            Text(
                text = content.ifEmpty { "No notes available." },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodyMedium
            )
            */
            com.aashu.privatesuite.presentation.components.RichTextRenderer(
                content = content.ifEmpty { "No notes available." },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                onLinkClick = onLinkClick
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
