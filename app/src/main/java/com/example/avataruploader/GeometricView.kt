package com.example.avataruploader

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class GeometricView : View {
    private var offset = 0f
    private val paint = Paint()
    private var image: Bitmap? = null
    private val path = Path()
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun setOffset(offset: Float) {
        this.offset = offset
        invalidate() // 通知 View 重新绘制
    }

    fun setImage(bitmap: Bitmap) {
        this.image = bitmap
        invalidate() // 通知 View 重新绘制
    }

    override fun onDraw(canvas: Canvas) {
        val centerX = 110f + offset
        val centerY = 140f
        val radius = 100f

        // 创建圆形路径
        path.reset()
        path.addCircle(centerX, centerY, radius, Path.Direction.CW)
        canvas.clipPath(path)

        // 绘制圆形背景
        paint.color = Color.YELLOW
        canvas.drawCircle(centerX, centerY, radius, paint)

        // 绘制图像
        image?.let {
            val scaledBitmap = Bitmap.createScaledBitmap(it, (2 * radius).toInt(), (2 * radius).toInt(), true)
            val shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            bitmapPaint.shader = shader
            canvas.drawCircle(centerX, centerY, radius, bitmapPaint)
        }
    }
}
