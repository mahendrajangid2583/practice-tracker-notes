package com.aashu.privatesuite.presentation.components

import android.content.Context
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.BulletSpan
import android.text.style.ImageSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat

import android.graphics.drawable.Drawable

@Composable
fun RichTextEditor(
    modifier: Modifier = Modifier,
    initialHtml: String,
    onContentChanged: (String) -> Unit,
    onMentionQuery: (String, (com.aashu.privatesuite.data.local.entities.TaskEntity) -> Unit) -> Unit = { _, _ -> },
    onMentionDismiss: () -> Unit = {},
    onRequestInsertLink: (currentLink: String?, onLinkEntered: (String) -> Unit) -> Unit = { _, _ -> },
    onRequestInsertImage: (onUrlEntered: (String) -> Unit) -> Unit = { _ -> },
    onCursorPositionChange: (Int, Int) -> Unit = { _, _ -> }
) {
    var editText: EditText? by remember { mutableStateOf(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Commands exposed to parent if needed, but here we handle toolbar internally
    
    Column(modifier = modifier) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = { editText?.let { toggleStyle(it, StyleSpan(android.graphics.Typeface.BOLD)) } }) {
                Icon(Icons.Default.FormatBold, "Bold", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = { editText?.let { toggleStyle(it, StyleSpan(android.graphics.Typeface.ITALIC)) } }) {
                Icon(Icons.Default.FormatItalic, "Italic", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = { editText?.let { toggleStyle(it, UnderlineSpan()) } }) {
                Icon(Icons.Default.FormatUnderlined, "Underline", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = { editText?.let { toggleStyle(it, StrikethroughSpan()) } }) {
                Icon(Icons.Default.FormatStrikethrough, "Strikethrough", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = { 
                val currentUrl = getUrlAtSelection(editText)
                onRequestInsertLink(currentUrl) { url ->
                    editText?.let { insertLink(it, url) }
                }
            }) {
                Icon(Icons.Default.Link, "Link", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = { 
                onRequestInsertImage { url ->
                    editText?.let { 
                        insertImage(context, it, url) { text ->
                            val html = HtmlCompat.toHtml(text as Spannable, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                            onContentChanged(html)
                        } 
                    }
                }
            }) {
                Icon(Icons.Filled.Image, "Image", tint = MaterialTheme.colorScheme.onSurface)
            }

        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            factory = { ctx ->
                EditText(ctx).apply {
                    background = null
                    setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
                    textSize = 16f
                    setHint("Write your tactical brief...")
                    setHintTextColor(ContextCompat.getColor(ctx, android.R.color.darker_gray))
                    
                    if (initialHtml.isNotEmpty()) {
                        // Use CoilImageGetter for initial load too if we want images to show effectively? 
                        // But CoilImageGetter is for TextView. EditText is a TextView.
                        // However, maintaining editable spans with Async images is tricky.
                        // For now, let's just use basic loading or the new getter.
                        val imageGetter = com.aashu.privatesuite.presentation.util.CoilImageGetter(ctx, this)
                        val spanned = HtmlCompat.fromHtml(initialHtml, HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, null)
                        setText(spanned)
                    }

                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            val text = s.toString()
                            val cursor = selectionEnd
                            
                            // Report cursor position for popup
                            // We need absolute coordinates ideally, but here we can't easily get them without Layout.
                            // The listener in EditorScreen will handle getting coordinates if we pass the EditText or use selection.
                            // Actually, onCursorPositionChange passed simple coordinates is not enough for Popup anchor.
                            // We need relative to screen.
                            
                            val layout = this@apply.layout
                            if (layout != null && cursor >= 0) {
                                val line = layout.getLineForOffset(cursor)
                                val x = layout.getPrimaryHorizontal(cursor).toInt()
                                val y = layout.getLineBottom(line)
                                onCursorPositionChange(x, y)
                            }

                            if (cursor > 0) {
                                val lastAt = text.lastIndexOf('@', cursor - 1)
                                if (lastAt != -1) {
                                    val candidate = text.substring(lastAt, cursor)
                                    // simple check: no spaces or newlines in candidate
                                    if (!candidate.contains(" ") && !candidate.contains("\n")) {
                                        val query = candidate.substring(1)
                                        onMentionQuery(query) { selectedTask ->
                                            val editable = this@apply.text // Editable
                                            val mentionText = "@${selectedTask.title}"
                                            editable.replace(lastAt, cursor, mentionText)
                                            val newEnd = lastAt + mentionText.length
                                            editable.setSpan(
                                                URLSpan("/collection/${selectedTask.collectionId}?focus=${selectedTask.id}"), 
                                                lastAt, 
                                                newEnd, 
                                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                            )
                                            onMentionDismiss()
                                        }
                                    } else {
                                        onMentionDismiss()
                                    }
                                } else {
                                    onMentionDismiss()
                                }
                            } else {
                                onMentionDismiss()
                            }
                        }
                        override fun afterTextChanged(s: Editable?) {
                            val html = HtmlCompat.toHtml(s as Spannable, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
                            onContentChanged(html)
                        }
                    })
                    
                    // Also listen to cursor movements directly? 
                    // setOnSelectionChangedListener is protected.
                    // We can use AccessibilityDelegate or just rely on onTextChanged and click events.
                    setOnClickListener { 
                        val cursor = selectionEnd
                        val layout = layout
                        if (layout != null && cursor >= 0) {
                            val line = layout.getLineForOffset(cursor)
                            val x = layout.getPrimaryHorizontal(cursor).toInt()
                            val y = layout.getLineBottom(line)
                            onCursorPositionChange(x, y)
                        }
                    }
                    
                    editText = this
                }
            }
        )
    }
}

private fun toggleStyle(editText: EditText, styleSpan: Any) {
    val start = editText.selectionStart
    val end = editText.selectionEnd
    if (start == end) return 
    
    val spannable = editText.text as Spannable
    val existingSpans = spannable.getSpans(start, end, styleSpan::class.java)
    
    var exists = false
    for (span in existingSpans) {
        if (span::class.java == styleSpan::class.java) {
            exists = true
            spannable.removeSpan(span)
        }
    }
    
    if (!exists) {
        spannable.setSpan(styleSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

private fun toggleParagraphStyle(editText: EditText, styleSpan: Any) {
    val selectionStart = editText.selectionStart
    val selectionEnd = editText.selectionEnd
    val text = editText.text as Spannable
    
    // Handle BulletSpan specially - it needs to be applied per paragraph

    
    // For other paragraph styles (if needed in future), use original logic
    var start = selectionStart
    while (start > 0 && text[start - 1] != '\n') {
        start--
    }
    
    var end = selectionEnd
    while (end < text.length && text[end] != '\n') {
        end++
    }
    
    val existingSpans = text.getSpans(start, end, styleSpan::class.java)
    
    var exists = false
    for (span in existingSpans) {
        if (span::class.java == styleSpan::class.java) {
            exists = true
            text.removeSpan(span)
        }
    }
    
    if (!exists) {
        text.setSpan(styleSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}



private fun getUrlAtSelection(editText: EditText?): String? {
    if (editText == null) return null
    val start = editText.selectionStart
    val end = editText.selectionEnd
    val spannable = editText.text as Spannable
    val spans = spannable.getSpans(start, end, URLSpan::class.java)
    return spans.firstOrNull()?.url
}

private fun insertLink(editText: EditText, url: String) {
    val start = editText.selectionStart
    val end = editText.selectionEnd
    val editable = editText.text // Editable
    
    // Remove existing URLSpans in range
    val existing = editable.getSpans(start, end, URLSpan::class.java)
    for (span in existing) editable.removeSpan(span)
    
    if (start < end) {
        editable.setSpan(URLSpan(url), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    } else {
        val linkText = url
        editable.insert(start, linkText)
        editable.setSpan(URLSpan(url), start, start + linkText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

private fun insertImage(context: Context, editText: EditText, url: String, onContentChanged: (Editable) -> Unit) {
    val start = editText.selectionStart
    val editable = editText.text 
    
    // Insert placeholder immediately
    val placeholder = "￼" // Object replacement character
    editable.insert(start, placeholder)
    
    // Create initial placeholder ImageSpan (Synchronous)
    // This ensures that even before the image loads, there is a span with the source URL.
    val placeholderDrawable = android.graphics.drawable.ColorDrawable(0xFFCCCCCC.toInt())
    placeholderDrawable.setBounds(0, 0, 100, 100) // Small square
    val initialSpan = ImageSpan(placeholderDrawable, url, ImageSpan.ALIGN_BOTTOM)
    
    val spanStart = start
    val spanEnd = start + 1
    editable.setSpan(initialSpan, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    
    // Trigger save immediately
    onContentChanged(editable)
    
    val imageLoader = coil.ImageLoader(context)
    val request = coil.request.ImageRequest.Builder(context)
        .data(url)
        .target(object : coil.target.Target {
            override fun onSuccess(result: Drawable) {
                 val bitmap = (result as android.graphics.drawable.BitmapDrawable).bitmap
                 val drawable = android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
                 
                 // Use full width minus padding logic similar to CoilImageGetter
                 val displayMetrics = context.resources.displayMetrics
                 val screenWidth = displayMetrics.widthPixels
                 val padding = (32 * displayMetrics.density).toInt()
                 
                 val targetWidth = if (editText.width > 0) {
                     editText.width - editText.paddingLeft - editText.paddingRight
                 } else {
                     screenWidth - padding
                 }
                 
                 val width = targetWidth.coerceAtLeast(100)
                 
                 // Avoid division by zero
                 val intrinsicWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
                 val aspectRatio = drawable.intrinsicHeight.toFloat() / intrinsicWidth.toFloat()
                 val height = (width * aspectRatio).toInt()
                 
                 drawable.setBounds(0, 0, width, height)
                 
                 val newImageSpan = ImageSpan(drawable, url, ImageSpan.ALIGN_BOTTOM)
                 
                 // Remove old span and add new one
                 // We need to find where our span is now (user might have typed)
                 val currentSpanStart = editable.getSpanStart(initialSpan)
                 val currentSpanEnd = editable.getSpanEnd(initialSpan)
                 
                 if (currentSpanStart != -1 && currentSpanEnd != -1) {
                     editable.removeSpan(initialSpan)
                     editable.setSpan(newImageSpan, currentSpanStart, currentSpanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                     
                     // Trigger save again with updated drawable (though HTML might be same if only src matters)
                     onContentChanged(editable)
                     editText.invalidate() // Force redraw
                 }
            }
            override fun onError(error: Drawable?) {
                // Handle error
            }
        })
        .build()
    
    imageLoader.enqueue(request)
}
