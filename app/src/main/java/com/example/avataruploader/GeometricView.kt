package com.example.avataruploader

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class GeometricView : View {
    private val paint = Paint() //建立一個畫圓的畫筆
    private val path = Path() //建立一個"路徑對象"(人話:初始化圓型的輪廓)
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG) //繪製圖像的畫筆(Paint.ANTI_ALIAS_FLAG抗鋸齒效果，使圖的邊緣更平滑)
    private var image: Bitmap? = null //儲存大頭貼的容器

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    /**
     * 設置要顯示的圖像，並通知View重新繪製
     * @param bitmap 要顯示的大頭貼
     */
    fun setImage(bitmap: Bitmap) {
        this.image = bitmap
        invalidate() // 通知 View 重新繪製
    }

    override fun onDraw(canvas: Canvas) {
        val centerX = width / 2f  //找出view的中心點-X軸
        val centerY = height / 2f //找出view的中心點-Y軸
        val radius = minOf(width, height) / 2f //半徑為view的寬度與高度中取最小值的1/2

        path.reset()//拿一開始的空白輪廓
        path.addCircle(centerX, centerY, radius, Path.Direction.CW)//將輪廓加入圓型的概念(中心點﹑半徑﹑順時針方向)
        canvas.clipPath(path)//剪裁畫布，讓繪製的行為只限制在圓形內部

        // 繪製圓形背景
        paint.color = Color.YELLOW //因為預覽沒有照片，為了顯示出圓形大小所以將黃色指派給筆刷
        canvas.drawCircle(centerX, centerY, radius, paint) //畫個圓圓(中心點,半徑,筆刷)

        // 绘制图像
        image?.let {
            //CreateBitmap(縮放對象,目標寬度,目標高度,是否使用過濾器進行平滑縮放(重新調整圖像邊緣，降低鋸齒度))
            val scaledBitmap = Bitmap.createScaledBitmap(it, (2 * radius).toInt(), (2 * radius).toInt(), true)
            //BitmapShader(繪製區域,水平重複方式,垂直重複方式(Shader.TileMode.CLAMP為"以裁剪或延展代替重複"))可以將Bitmap作為texture來繪製圖案，只是這裡我的畫布只有一格圓形
            val shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            bitmapPaint.shader = shader//將規劃好的藍圖(目標縮放﹑繪製方式)都透過shader(著色器)跟筆刷綁定
            canvas.drawCircle(centerX, centerY, radius, bitmapPaint)//畫個圓圓(中心點,半徑,筆刷)
        }
    }
}

