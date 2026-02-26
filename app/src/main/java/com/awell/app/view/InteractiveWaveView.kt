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

class InteractiveWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val qBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePath = Path()
    private val particles = mutableListOf<Particle>()
    private val particlePaint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val random = java.util.Random(16)
        // 对应音效的柱子数量
    private val APS_COUNT = 8
    // 初始坡度点的高度 (dp)
    private var slopeHeightsDp = floatArrayOf(100f, 100f, 100f, 100f, 100f, 100f, 100f, 100f)
    // 在 InteractiveWaveView 类定义处添加
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AAAAAA") // 文字颜色
        textSize = dpToPx(10f)              // 文字大小
        textAlign = Paint.Align.RIGHT       // 左侧文字右对齐，方便贴合线条
    }
    private var COLUMN_WIDTH_DP = 0f // 每一格的固定宽度
    private var mPendingUpdate = false
    // 定义留白的宽度
    private val TOP_OFFSET = dpToPx(0f)
    private val paddingLeft = dpToPx(0f)   // 给左侧 Y 轴文字留空间
    private val paddingBottom = dpToPx(0f) // 给底部 X 轴文字留空间
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
            color = Color.parseColor("#888888")
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(1.5f)
            strokeCap = Paint.Cap.ROUND
        }
        gridPaint.apply {
            color = Color.parseColor("#E0E0E0")
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(0.5f)
        }
        qBgPaint.apply {
            color = Color.parseColor("#181717")
            style = Paint.Style.FILL
        }
        COLUMN_WIDTH_DP = (context.resources.displayMetrics.widthPixels/APS_COUNT).toFloat()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!needIntercept) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true) // 禁止父布局拦截滑动
                updateWaveHeight(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP -> {
//                spawnParticles(event.x, event.y)
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
        val gridW = dpToPx(COLUMN_WIDTH_DP) * (mDataList.size - 1)
        val segmentWidth = gridW / (mDataList.size - 1)
        // 修正：减去左侧留白，得到相对于网格起点(0,0)的坐标
        val relativeX = touchX - paddingLeft
        // 1. 找到当前手指最接近的点
        val targetIndex = (relativeX / segmentWidth).roundToInt().coerceIn(0, mDataList.size - 1)
        // 这样可以防止在两个格子中间滑动时，数值跳变太剧烈
        val dist = abs(relativeX - targetIndex * segmentWidth)
        if (dist < segmentWidth * 0.8f) {
            val h = height.toFloat()
            val newHeightPx = (h - paddingBottom - touchY + TOP_OFFSET).coerceIn(minHeightPx, maxHeightPx)
            slopeHeightsDp[targetIndex] = newHeightPx / context.resources.displayMetrics.density
            invalidate()
            val gain = (slopeHeightsDp[targetIndex] / maxHeightPx * maxGain).roundToInt()
            onGainChange?.invoke(targetIndex, gain)
        }
    }

    @SuppressLint("DrawAllocation", "UseKtx")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 重新计算当前的网格尺寸，确保它不随 View 宽度拉伸
        val gridW = dpToPx(COLUMN_WIDTH_DP) * (mDataList.size - 1)
        val gridH = height.toFloat() - paddingBottom - TOP_OFFSET

        canvas.save()
        canvas.translate(paddingLeft, TOP_OFFSET)

        // 1. 画格子（列数直接用数组长度 - 1）
        drawGrid(canvas, gridW, gridH, 2, mDataList.size - 1)

        // 2. 画渐变
        val gradient = LinearGradient(0f, 0f, gridW, 0f,
            intArrayOf(Color.parseColor("#0036FF"),
                Color.parseColor("#00FF5A"),
                Color.parseColor("#C5CF20")), null, Shader.TileMode.CLAMP)
        fillPaint.shader = gradient

        // 3. 计算波浪点间距
        val segmentWidth = gridW / (APS_COUNT - 1)

        buildWavePath(gridW, gridH, segmentWidth)
        canvas.drawPath(wavePath, fillPaint)

        drawTopOutline(canvas, gridH, segmentWidth)
        canvas.restore()
        drawParticles(canvas)
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
        // 根据 X 轴数组长度计算总网格宽度，应该取mDataList长度，但此时还未传参
        val gridW = dpToPx(COLUMN_WIDTH_DP) * (APS_COUNT - 1)
        // 总宽度 = 网格宽 + 左留白 + 右侧余量
        val totalWidth = (gridW + paddingLeft + dpToPx(20f)).toInt()

        val totalHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(totalWidth, totalHeight)

        maxHeightPx = totalHeight.toFloat() - paddingBottom - TOP_OFFSET
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mPendingUpdate) {
            mPendingUpdate = false
            drawView()
        }
    }

    fun reset() {
        // 默认
        for (i in slopeHeightsDp.indices) {
            slopeHeightsDp[i] = maxHeightPx/2
        }
        invalidate()
    }

    private fun drawGrid(canvas: Canvas, gridWidth: Float, gridHeight: Float, rows: Int, cols: Int) {
        val columnWidth = gridWidth / cols
        val rowHeight = gridHeight / rows
        val qPaddingH = dpToPx(7f)
        val qPaddingV = dpToPx(4f)
        // 1. 绘制垂直线及底部文字
//        textPaint.textAlign = Paint.Align.CENTER
        for (i in 0..cols) {
            val x = i * columnWidth
            // 画线
            canvas.drawLine(x, 0f, x, gridHeight, gridPaint)
//            // 测量文字宽度
//            val textWidth = dpToPx(20f)
//            // 2. 获取文字的高度信息（包括上行间距和下行间距）
//            val fontMetrics = textPaint.fontMetrics
//            // 在格子外面绘制 X 轴文字
//            if (i > 0 && i < mDataList.size) {
//                // gridHeight + 偏移量，使文字出现在线条下方
//                val hz = mDataList[i].apsFreq
//                val text = if (hz < 1000) {
//                    hz.toString()
//                } else {
//                    (hz / 1000f).toString() + "k"
//                }
//                canvas.drawText(text, x, gridHeight + dpToPx(18f), textPaint)
//                // 绘制q值背景
//                val left = x - (textWidth/2) - qPaddingH
//                val right = x + (textWidth/2) + qPaddingH
//                val top = gridHeight + dpToPx(40f) + fontMetrics.ascent - qPaddingV
//                val bottom = gridHeight + dpToPx(40f) + qPaddingV + fontMetrics.descent
//                // 4. 绘制圆角矩形背景
//                val cornerRadius = dpToPx(10f) // 圆角半径
//                canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, qBgPaint)
//                canvas.drawText(mDataList[i].apsQ.toString(), x, gridHeight + dpToPx(40f), textPaint)
//            }
        }

        // 2. 绘制水平线及左侧文字
//        textPaint.textAlign = Paint.Align.RIGHT
        for (i in 0..rows) {
            val y = i * rowHeight
            // 画线
            canvas.drawLine(0f, y, gridWidth, y, gridPaint)

            // 在格子外面绘制 Y 轴文字
//            if (i < yAxisValues.size) {
//                // 0 - 偏移量，使文字出现在线条左侧
//                canvas.drawText(yAxisValues[i], -dpToPx(8f), y + textPaint.textSize / 3f, textPaint)
//            }
        }
    }

    private fun buildWavePath(w: Float, h: Float, segmentWidth: Float) {
        wavePath.reset()
        wavePath.moveTo(0f, h)
        val startY = h - dpToPx(slopeHeightsDp[0])
        wavePath.lineTo(0f, startY)

        for (i in 0 until slopeHeightsDp.size - 1) {
            val x1 = i * segmentWidth
            val y1 = h - dpToPx(slopeHeightsDp[i])
            val x2 = (i + 1) * segmentWidth
            val y2 = h - dpToPx(slopeHeightsDp[i + 1])

            // 三次贝塞尔保持平滑
            wavePath.cubicTo(x1 + segmentWidth / 2f, y1, x2 - segmentWidth / 2f, y2, x2, y2)
        }
        wavePath.lineTo(w, h)
        wavePath.close()
    }

    // 添加一个触发粒子爆炸的方法
    private fun spawnParticles(centerX: Float, centerY: Float) {
        repeat(200) { // 150个点足以产生铺满感
            // 在点击点周围的矩形区域内随机选点
            val offsetX = (random.nextGaussian() * dpToPx(8f)).toFloat() // X轴 扩散略大
            val offsetY = (random.nextGaussian() * dpToPx(20f)).toFloat() // Y轴 扩散略小，形成长条

            // 速度给得很小，让它们只是微颤
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
        mDataList.clear()
        mDataList.addAll(list)
        if (measuredWidth > 0){
            drawView()
        } else {
            // 还没测量，设置一个标记位
            mPendingUpdate = true
        }
    }

    private fun drawView() {
        if (mDataList.isNotEmpty()) {
            for (i in 0 until mDataList.size) {
                val apsModel = mDataList[i]
                val high = BigDecimal(apsModel).divide(BigDecimal(maxGain), 2, RoundingMode.HALF_DOWN)
                    .multiply(BigDecimal(maxHeightPx.toInt()))
                    .toFloat()
                slopeHeightsDp[i] = high
            }
            postInvalidate()
        } else {
            reset()
        }
    }

    private fun drawTopOutline(canvas: Canvas, h: Float, segmentWidth: Float) {
        val topPath = Path()
        topPath.moveTo(0f, h - dpToPx(slopeHeightsDp[0]))
        for (i in 0 until slopeHeightsDp.size - 1) {
            val x1 = i * segmentWidth
            val y1 = h - dpToPx(slopeHeightsDp[i])
            val x2 = (i + 1) * segmentWidth
            val y2 = h - dpToPx(slopeHeightsDp[i + 1])
            topPath.cubicTo(x1 + segmentWidth / 2f, y1, x2 - segmentWidth / 2f, y2, x2, y2)
        }
        canvas.drawPath(topPath, borderPaint)
    }

    private fun dpToPx(dp: Float): Float = dp * context.resources.displayMetrics.density

}