package com.awell.app.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.awell.app.model.Particle
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.core.graphics.toColorInt
import com.awell.app.utils.LogUtil

class InteractiveWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val qBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePath = Path()
    private var freqArray: IntArray? = null
    private val particles = mutableListOf<Particle>()
    private val particlePaint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val random = java.util.Random(16)
    // 初始坡度点的高度 (dp)
    private var slopeHeightsDp = floatArrayOf(100f, 100f, 100f, 100f, 100f, 100f, 100f, 100f, 100f, 100f, 100f, 100f)
    // 在 InteractiveWaveView 类定义处添加
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#AAAAAA".toColorInt() // 文字颜色
        textSize = dpToPx(22f)              // 文字大小
        textAlign = Paint.Align.RIGHT       // 左侧文字右对齐，方便贴合线条
    }
    private var COLUMN_WIDTH_DP = 0f // 每一格的固定宽度
    private var mPendingUpdate = false
    // 定义留白的宽度
    var topOffset = dpToPx(0f)
    private val paddingLeft = dpToPx(0f)   // 给左侧 Y 轴文字留空间
    // 左右两侧的物理安全留白（留出空间给最左和最右的文字）
    private val paddingLeftRight = dpToPx(28f)
    var paddingBottom = dpToPx(0f) // 给底部 X 轴文字留空间
    // 是否绘制横线
    var showHorizontalLine = false
    // 是否绘制竖线
    var showVerticalLine = false
    // 是否显示顶部的值
    var showTopValue = false
    var maxGain = 0
    //
    private val yAxisValues = arrayOf("+15", "+10", "+5", "0", "-5", "-10", "-15") // 对应 6 行
    private var mDataList = mutableListOf<Int>()
    private val minHeightPx = dpToPx(1f)
    private var maxHeightPx = 0f

    // 是否需要拦截触摸事件
    var needIntercept = false
    var onGainChange: ((index: Int, gain: Int) -> Unit)? = null

    init {
        borderPaint.apply {
            color = "#888888".toColorInt()
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(1.5f)
            strokeCap = Paint.Cap.ROUND
        }
        gridPaint.apply {
            color = Color.TRANSPARENT
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(0.5f)
        }
        qBgPaint.apply {
            color = "#181717".toColorInt()
            style = Paint.Style.FILL
        }
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!needIntercept) {
            return false
        }
//        if (event.action == MotionEvent.ACTION_DOWN && event.y < topOffset) {
//            return false
//        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true) // 禁止父布局拦截滑动
                updateWaveHeight(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP -> {
                spawnParticles(event.x, event.y)
                parent.requestDisallowInterceptTouchEvent(false) // 恢复拦截
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false) // 恢复拦截
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateWaveHeight(touchX: Float, touchY: Float) {
        val topLimit = if (showTopValue) topOffset else 0f
        val clampedY = touchY.coerceAtLeast(topLimit)

        val gridW = width.toFloat()
        val segmentWidth = gridW / (mDataList.size - 1)
        val relativeX = touchX - paddingLeft
        val targetIndex = (relativeX / segmentWidth).roundToInt().coerceIn(0, mDataList.size - 1)
        val dist = abs(relativeX - targetIndex * segmentWidth)
        if (dist < segmentWidth * 0.8f) {
            val h = height.toFloat()
            val newHeightPx = (h - paddingBottom - clampedY + topLimit).coerceIn(minHeightPx, maxHeightPx)
            slopeHeightsDp[targetIndex] = newHeightPx
            val gain = (slopeHeightsDp[targetIndex] / maxHeightPx * maxGain).roundToInt()
            mDataList[targetIndex] = gain
            onGainChange?.invoke(targetIndex, gain)
            invalidate()
        }
    }

    @SuppressLint("DrawAllocation", "UseKtx")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val gridW = width.toFloat()
        // 网格的实际物理高度
        val gridH = height.toFloat() - paddingBottom - topOffset

        canvas.save()
        // X 轴平移留白，Y 轴在绘制内部通过 topOffset 统一处理，不再全局平移 Y
        canvas.translate(paddingLeft, 0f)

        val gridWidth = if (showVerticalLine) gridW - (paddingLeftRight * 2) else gridW
        drawGrid(canvas, gridWidth, gridH, 2, mDataList.size - 1)

        // 绘制波纹
        canvas.save()
        // 限制绘制区间：从 TOP_OFFSET 到 View 底部，物理上绝对禁止波纹冲到 TOP_OFFSET 上方
        canvas.clipRect(0f, topOffset, width.toFloat(), height.toFloat() - paddingBottom)

        // 绘制填充面
        buildWavePath(gridW, gridH)
        canvas.drawPath(wavePath, fillPaint)
        // 绘制顶部边框线
        drawTopOutline(canvas, gridH)
        // 释放波纹的裁剪锁
        canvas.restore()
        // 3. 绘制粒子
        drawParticles(canvas)
    }

    /**
     * 供外界动态修改填充渐变色的接口
     */
    fun setWaveFillGradient(colors: IntArray, heightPx: Float, widthPx: Float) {
        fillPaint.shader = LinearGradient(
            0f, 0f, widthPx, heightPx,
            colors, null, Shader.TileMode.CLAMP
        )
        invalidate()
    }

    private fun drawParticles(canvas: Canvas) {
        if (particles.isEmpty()) return

        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (p.isAlive()) {
                particlePaint.color = p.color
                particlePaint.alpha = p.alpha
                canvas.drawCircle(p.x, p.y, p.size, particlePaint)
                p.update()
            } else {
                iterator.remove()
            }
        }
        postInvalidateOnAnimation()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        // 根据 X 轴数组长度计算总网格宽度，应该取mDataList长度，但此时还未传参
//        val gridW = dpToPx(COLUMN_WIDTH_DP) * (APS_COUNT - 1)
//        // 总宽度 = 网格宽 + 左留白 + 右侧余量
//        val totalWidth = (gridW + paddingLeft + dpToPx(20f)).toInt()
        val totalWidth = MeasureSpec.getSize(widthMeasureSpec)
        val totalHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(totalWidth, totalHeight)

        maxHeightPx = totalHeight.toFloat() - paddingBottom - topOffset
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mPendingUpdate) {
            mPendingUpdate = false
            drawView()
        }
    }

    private fun reset() {
        // 默认
        for (i in slopeHeightsDp.indices) {
            slopeHeightsDp[i] = maxHeightPx/2
        }
        invalidate()
    }

    private fun drawGrid(canvas: Canvas, gridWidth: Float, gridHeight: Float, rows: Int, cols: Int) {
        val columnWidth = gridWidth / cols
        val rowHeight = gridHeight / rows
        // 1. 绘制垂直线及底部文字
        textPaint.textAlign = Paint.Align.CENTER
        for (i in 0..cols) {
            val x = (i * columnWidth) + paddingLeftRight
            // 在格子外面绘制 X 轴文字
            if (i < (freqArray?.size ?: 0)) {
                // gridHeight + 偏移量，使文字出现在线条下方
                val hz = freqArray!![i]
                LogUtil.i("hz = $hz k")
                val text = if (hz < 1000) {
                    hz.toString()
                } else {
                    (hz / 1000f).toString() + "k"
                }
                val textY = dpToPx(20f)
                if (showTopValue) {
                    val value = if (mDataList[i] > maxGain/2) {
                        "+${mDataList[i] - (maxGain/2)}"
                    } else if (mDataList[i] < maxGain/2) {
                        "${mDataList[i] - (maxGain/2)}"
                    } else {
                        "0"
                    }
                    canvas.drawText(value, x, textY, textPaint)
                }
                canvas.drawText(text, x, gridHeight + topOffset + textY, textPaint)
            }
            // 画线
            if (showVerticalLine) {
                canvas.drawLine(x, if (showTopValue) topOffset else 0f, x, gridHeight + topOffset, gridPaint)
            }
        }

        // 2. 绘制水平线及左侧文字
//        textPaint.textAlign = Paint.Align.RIGHT
        for (i in 0..rows) {
            val y = i * rowHeight
            // 画线
            if (showHorizontalLine) {
                canvas.drawLine(0f, y, gridWidth, y, gridPaint)
            }
            // 在格子外面绘制 Y 轴文字
//            if (i < yAxisValues.size) {
//                // 0 - 偏移量，使文字出现在线条左侧
//                canvas.drawText(yAxisValues[i], -dpToPx(8f), y + textPaint.textSize / 3f, textPaint)
//            }
        }
    }

    private fun buildWavePath(w: Float, h: Float) {
        wavePath.reset()

        // 确定网格真正的左右物理边界
        val startX = paddingLeftRight
        val endX = w - paddingLeftRight
        // 重新计算精确的格子宽度
        val validSegmentWidth = (endX - startX) / (slopeHeightsDp.size - 1)
        val bottomY = h + topOffset

        // 起点：在第一条垂直线（7.5k左边的起点）的底部扎底
        wavePath.moveTo(startX, bottomY)

        val startY = h - dpToPx(slopeHeightsDp[0]) + topOffset
        wavePath.lineTo(startX, startY)

        // 通过贝塞尔曲线绘制到最后一个数据点
        for (i in 0 until slopeHeightsDp.size - 1) {
            val x1 = startX + i * validSegmentWidth
            val y1 = h - dpToPx(slopeHeightsDp[i]) + topOffset
            val x2 = startX + (i + 1) * validSegmentWidth
            val y2 = h - dpToPx(slopeHeightsDp[i + 1]) + topOffset

            // 三次贝塞尔保持平滑
            wavePath.cubicTo(x1 + validSegmentWidth / 2f, y1, x2 - validSegmentWidth / 2f, y2, x2, y2)
        }

        wavePath.lineTo(endX, bottomY)
        wavePath.close()
    }

    // 添加一个触发粒子爆炸的方法
    private fun spawnParticles(centerX: Float, centerY: Float) {
        repeat(300) { // 150个点足以产生铺满感
            // 在点击点周围的矩形区域内随机选点
            val offsetX = (random.nextGaussian() * dpToPx(12f)).toFloat() // X轴 扩散略大
            val offsetY = (random.nextGaussian() * dpToPx(25f)).toFloat() // Y轴 扩散略小，形成长条

            val vx = (Math.random().toFloat() - 0.5f) * 2f
            val vy = (Math.random().toFloat() - 0.5f) * 2f

            particles.add(Particle(
                x = centerX + offsetX,
                y = centerY + offsetY,
                vx = vx,
                vy = vy,
                size = (Math.random() * 2f).toFloat(), // 大小不一
                color = Color.WHITE
            ))
        }
        invalidate()
    }

    fun updateList(list: List<Int>) {
        if (mDataList.isEmpty() && topOffset > 0) {
            maxHeightPx -= topOffset
        }
        mDataList.clear()
        mDataList.addAll(list)
        COLUMN_WIDTH_DP = (width/list.size).toFloat()
        if (measuredWidth > 0){
            drawView()
        } else {
            // 还没测量，设置一个标记位
            mPendingUpdate = true
        }
    }

    fun updateFreq(dataArray: IntArray) {
        if (freqArray == null) {
            freqArray = dataArray
            invalidate()
        }
    }

    private fun drawView() {
        if (mDataList.isNotEmpty()) {
            for (i in 0 until mDataList.size) {
                val apsModel = mDataList[i]
                val high = BigDecimal(apsModel).divide(BigDecimal(maxGain), 2, RoundingMode.HALF_DOWN)
                    .multiply(BigDecimal(maxHeightPx.toInt()))
                    .toFloat()
                slopeHeightsDp[i] = if (high < maxHeightPx) high else maxHeightPx
            }
            postInvalidate()
        } else {
            reset()
        }
    }

    private fun drawTopOutline(canvas: Canvas, h: Float) {
        val topPath = Path()
        val startX = paddingLeftRight
        val endX = canvas.width.toFloat() - paddingLeftRight
        val validSegmentWidth = (endX - startX) / (slopeHeightsDp.size - 1)

        val startY = h - dpToPx(slopeHeightsDp[0]) + topOffset
        topPath.moveTo(startX, startY)

        for (i in 0 until slopeHeightsDp.size - 1) {
            val x1 = startX + i * validSegmentWidth
            val y1 = h - dpToPx(slopeHeightsDp[i]) + topOffset
            val x2 = startX + (i + 1) * validSegmentWidth
            val y2 = h - dpToPx(slopeHeightsDp[i + 1]) + topOffset
            topPath.cubicTo(x1 + validSegmentWidth / 2f, y1, x2 - validSegmentWidth / 2f, y2, x2, y2)
        }
        canvas.drawPath(topPath, borderPaint)
    }

    private fun dpToPx(dp: Float): Float = dp * context.resources.displayMetrics.density

}