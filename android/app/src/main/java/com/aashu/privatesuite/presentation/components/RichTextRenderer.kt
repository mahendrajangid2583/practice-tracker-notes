package com.aashu.privatesuite.presentation.components

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.aashu.privatesuite.presentation.util.CoilImageGetter

@Composable
fun RichTextRenderer(
    content: String,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit = {}
) {
    if (content.isBlank()) {
        androidx.compose.material3.Text(
            text = "No content", 
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, 
            color = androidx.compose.ui.graphics.Color.Gray,
            modifier = modifier
        )
        return
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            TextView(context).apply {
                setTextColor(android.graphics.Color.WHITE) // Ensure text is visible
                textSize = 16f
                setLinkTextColor(android.graphics.Color.parseColor("#4ea8de")) // Light blue for links
                linksClickable = true
                movementMethod = LinkMovementMethod.getInstance()
                // Ensure the TextView itself doesn't consume clicks unless a link is clicked
                isClickable = false 
                isFocusable = false
            }
        },
        update = { textView: TextView ->
            val imageGetter = CoilImageGetter(textView.context, textView)
            val spanned = HtmlCompat.fromHtml(
                content,
                HtmlCompat.FROM_HTML_MODE_COMPACT,
                imageGetter,
                null
            )
            
            // Use SpannableStringBuilder for mutable spans
            val spannable = android.text.SpannableStringBuilder(spanned)
            val urlSpans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
            
            // Log found spans for debugging (use Logcat to view if needed)
            // android.util.Log.d("RichTextRenderer", "Found ${urlSpans.size} URLSpans")

            for (urlSpan in urlSpans) {
                val start = spannable.getSpanStart(urlSpan)
                val end = spannable.getSpanEnd(urlSpan)
                val flags = spannable.getSpanFlags(urlSpan)
                val url = urlSpan.url
                
                // android.util.Log.d("RichTextRenderer", "URLSpan: $url at $start-$end")

                val clickableSpan = object : android.text.style.ClickableSpan() {
                    override fun onClick(widget: android.view.View) {
                        // android.util.Log.d("RichTextRenderer", "Clicked URL: $url")
                        onLinkClick(url)
                    }
                    
                    override fun updateDrawState(ds: android.text.TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false // Optional: remove underline if desired
                        ds.color = android.graphics.Color.parseColor("#4ea8de") 
                    }
                }
                
                spannable.removeSpan(urlSpan)
                spannable.setSpan(clickableSpan, start, end, flags)
            }
            
            textView.text = spannable
        }
    )
}
