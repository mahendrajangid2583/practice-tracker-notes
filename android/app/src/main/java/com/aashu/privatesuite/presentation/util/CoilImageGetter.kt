package com.aashu.privatesuite.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import coil.ImageLoader
import coil.request.ImageRequest
import coil.target.Target
import java.lang.ref.WeakReference

class CoilImageGetter(
    private val context: Context,
    textView: TextView
) : Html.ImageGetter {

    private val container = WeakReference(textView)

    override fun getDrawable(source: String): Drawable {
        val drawablePlaceholder = BitmapDrawablePlaceholder()
        
        // Calculate width immediately for placeholder
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val padding = (32 * displayMetrics.density).toInt()
        
        val textView = container.get()
        val targetWidth = if (textView != null && textView.width > 0) {
            textView.width - textView.paddingLeft - textView.paddingRight
        } else {
            screenWidth - padding
        }
        val width = targetWidth.coerceAtLeast(100)
        // Default placeholder height (16:9 aspect ratio)
        val height = (width * 9f / 16f).toInt()
        
        drawablePlaceholder.setBounds(0, 0, width, height)
        
        // Asynchronous image loading
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(source)
            .target(object : Target {
                override fun onStart(placeholder: Drawable?) {
                    // Optional: set a loading placeholder if desired
                }

                override fun onSuccess(result: Drawable) {
                    val textView = container.get() ?: return
                    
                    val drawable: Drawable = if (result is BitmapDrawable) {
                        BitmapDrawable(context.resources, result.bitmap)
                    } else {
                        result
                    }
                    
                    // Recalculate width/height based on actual image aspect ratio
                    // Use same width logic
                    val currentWidth = if (textView.width > 0) {
                        textView.width - textView.paddingLeft - textView.paddingRight
                    } else {
                        screenWidth - padding
                    }.coerceAtLeast(100)
                    
                    // Avoid division by zero
                    val intrinsicWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
                    val aspectRatio = drawable.intrinsicHeight.toFloat() / intrinsicWidth.toFloat()
                    
                    val currentHeight = (currentWidth * aspectRatio).toInt()
                    
                    drawable.setBounds(0, 0, currentWidth, currentHeight)
                    
                    drawablePlaceholder.setDrawable(drawable)
                    drawablePlaceholder.setBounds(0, 0, currentWidth, currentHeight)
                    
                    // Trigger invalidation to redraw the TextView with the new drawable
                    textView.text = textView.text
                }

                override fun onError(error: Drawable?) {
                    // Optional: set an error drawable
                }
            })
            .build()
        
        imageLoader.enqueue(request)

        return drawablePlaceholder
    }

    @Suppress("DEPRECATION")
    private class BitmapDrawablePlaceholder : BitmapDrawable() {
        private var drawable: Drawable? = null
        private val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            style = android.graphics.Paint.Style.FILL
        }

        override fun draw(canvas: Canvas) {
            if (drawable != null) {
                drawable?.draw(canvas)
            } else {
                // Draw placeholder background
                canvas.drawRect(bounds, paint)
            }
        }

        override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
             super.setBounds(left, top, right, bottom)
             drawable?.setBounds(left, top, right, bottom)
        }

        fun setDrawable(drawable: Drawable) {
            this.drawable = drawable
            this.setBounds(0, 0, drawable.bounds.width(), drawable.bounds.height())
        }
    }
}
