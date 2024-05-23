package com.example.morphing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class PixelDissolveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var bitmap1: Bitmap? = null
    private var bitmap2: Bitmap? = null
    private var transitionBitmap: Bitmap? = null
    private var progress: Float = 0f
    private val paint = Paint()
    private val pixelPositions = mutableListOf<Pair<Int, Int>>()
    private var scaledBitmap1: Bitmap? = null
    private var scaledBitmap2: Bitmap? = null

    fun setImages(image1: Bitmap, image2: Bitmap) {
        bitmap1 = image1
        bitmap2 = image2
        precomputePixelPositions()
        initializeTransitionBitmap()
        invalidate()
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        updateTransitionBitmap()
        invalidate()
    }

    private fun precomputePixelPositions() {
        pixelPositions.clear()
        val random = Random.Default
        val sideLength = minOf(width, height)

        val positions = List(sideLength * sideLength) { Pair(it % sideLength, it / sideLength) }.shuffled(random)
        pixelPositions.addAll(positions)
    }

    private fun initializeTransitionBitmap() {
        val sideLength = minOf(width, height)
        transitionBitmap = Bitmap.createBitmap(sideLength, sideLength, Bitmap.Config.ARGB_8888)
        scaledBitmap1 = bitmap1?.let { Bitmap.createScaledBitmap(it, sideLength, sideLength, true) }
        scaledBitmap2 = bitmap2?.let { Bitmap.createScaledBitmap(it, sideLength, sideLength, true) }
    }

    private fun updateTransitionBitmap() {
        transitionBitmap?.let { transitionBmp ->
            val sideLength = minOf(width, height)
            val pixelCount = (pixelPositions.size * progress).toInt()

            if (scaledBitmap2 != null) {
                for (i in 0 until pixelCount) {
                    val (x, y) = pixelPositions[i]
                    val pixel = scaledBitmap2!!.getPixel(x, y)
                    transitionBmp.setPixel(x, y, pixel)
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val sideLength = minOf(viewWidth, viewHeight)
        val leftPadding = (viewWidth - sideLength) / 2
        val topPadding = (viewHeight - sideLength) / 2

        scaledBitmap1?.let { bmp1 ->
            canvas.drawBitmap(bmp1, leftPadding, topPadding, paint)
            transitionBitmap?.let { transitionBmp ->
                canvas.drawBitmap(transitionBmp, leftPadding, topPadding, paint)
            }
        }
    }
}