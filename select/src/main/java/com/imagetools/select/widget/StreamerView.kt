package com.imagetools.select.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.imagetools.select.R
import com.imagetools.select.tools.Tools


/**
 * @author jv.lee
 * @date 2020/12/7
 * @description
 */
class StreamerView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var columnCount: Int
    private var rowCount = 0

    private var mWidth: Float = 0f
    private var mHeight: Float = 0f
    private var size: Float = 0f
    private var itemPadding: Float = 0F
    private var streamerValue: Float = 0f

    private var itemLineColor: Int
    private var itemColor: Int
    private var streamerColor: Int
    private var mode: Int = 0

    private var completeFlag = false

    init {
        context.obtainStyledAttributes(attributeSet, R.styleable.StreamerView).run {
            columnCount = getInt(R.styleable.StreamerView_streamer_columnCount, 4)
            itemLineColor = getColor(
                R.styleable.StreamerView_streamer_itemLineColor,
                Color.parseColor("#ffffff")
            )
            itemColor =
                getColor(R.styleable.StreamerView_streamer_itemColor, Color.parseColor("#4C4C4C"))
            streamerColor = getColor(
                R.styleable.StreamerView_streamer_streamerColor,
                Color.parseColor("#323232")
            )

            recycle()
        }
        itemPadding = context.resources.getDimension(R.dimen.item_padding)

        mPaint.style = Paint.Style.FILL
        mPaint.color = itemColor
        mPaint.strokeWidth = itemPadding

        mLinePaint.style = Paint.Style.STROKE
        mLinePaint.color = itemLineColor
        mLinePaint.strokeWidth = itemPadding
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = width.toFloat()
        mHeight = height.toFloat()
//        size = (mWidth - (itemPadding * columnCount.plus(1))) / columnCount
        size = Tools.getImageSize(context, columnCount).toFloat()
        rowCount = (mHeight / size).toInt()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mode == 0) {
            drawChildView(canvas)
            return
        }

        drawGrid(canvas)
        drawLine(canvas)
        if (!completeFlag) {
            invalidate()
        }
    }

    private fun drawGrid(canvas: Canvas) {
        //绘制填充
        mPaint.shader = buildGradientStreamer(0f, 0f, mWidth, mHeight)
        canvas.drawRect(0f, 0f, itemPadding * columnCount.plus(1) + size * columnCount, mHeight, mPaint)
    }

    private fun drawLine(canvas: Canvas) {
        //绘制水平间距
        for (rowIndex in 1..rowCount) {
            canvas.drawLine(
                0f,
                (itemPadding * rowIndex) + (size * rowIndex),
                mWidth,
                (itemPadding * rowIndex) + (size * rowIndex)
                , mLinePaint
            )
        }

        //绘制垂直间距
        for (columnIndex in 0..columnCount) {
            canvas.drawLine(
                (itemPadding * columnIndex) + (size * columnIndex) + (itemPadding / 2),
                0F,
                (itemPadding * columnIndex) + (size * columnIndex) + (itemPadding / 2),
                mHeight
                , mLinePaint
            )
        }
    }

    private fun drawChildView(canvas: Canvas) {
        setBackgroundColor(itemLineColor)
        for (rowIndex in 0..rowCount) {
            for (columnIndex in 0 until columnCount) {
                canvas.drawRect(
                    itemPadding + (columnIndex * itemPadding) + (columnIndex * size),
                    (rowIndex * itemPadding) + (rowIndex * size),
                    size + itemPadding + (columnIndex * itemPadding) + (columnIndex * size),
                    size + (rowIndex * itemPadding) + (rowIndex * size),
                    mPaint
                )
            }
        }
    }

    private fun buildGradientStreamer(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ): LinearGradient {
        val linearGradient =
            LinearGradient(
                left,
                top,
                -(right / 2),
                top,
                intArrayOf(itemColor, streamerColor, itemColor),
                floatArrayOf(0.1f, 0.3f, 0.6f),
                Shader.TileMode.CLAMP
            )
        val matrix = Matrix()
        streamerValue += (right * 0.025).toInt()
        if (streamerValue >= right * 1.5) {
            streamerValue = left
        }
        matrix.setTranslate(streamerValue, 0f)
        linearGradient.setLocalMatrix(matrix)
        return linearGradient
    }

    fun setColumnCount(count: Int) {
        this.columnCount = count
    }

    fun loadComplete() {
        if (completeFlag) return
        completeFlag = true
        visibility = GONE
    }

    fun loadCompleteDelay() {
        if (completeFlag) return
        postDelayed({
            completeFlag = true
            visibility = GONE
        }, 50)
    }

}