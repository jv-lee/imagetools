package com.imagetools.select.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.OverScroller
import android.widget.Scroller
import androidx.appcompat.widget.AppCompatImageView
import kotlinx.parcelize.Parcelize
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * @author jv.lee
 * @date 2021/2/4
 * @description
 */
open class TransformImageView : AppCompatImageView {

    companion object {
        private val TAG = TransformImageView::class.java.simpleName
        private const val MIN_ROTATE = 35
        private const val ANIM_DURING = 340
        private const val MAX_SCALE = 2.5f
    }

    private var mMinRotate = 0
    private var mAnimDuring = 0
    private var mMaxScale = 0f
    private var loadMaxSize = 0

    private var MAX_FLING_OVER_SCROLL = 0
    private var MAX_OVER_RESISTANCE = 0

    private val mBaseMatrix = Matrix()
    private val mAnimMatrix = Matrix()
    private val mSynthesisMatrix = Matrix()
    private val mTmpMatrix = Matrix()

    private var mRotateDetector: RotateGestureDetector? = null
    private var mDetector: GestureDetector? = null
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mClickListener: OnClickListener? = null

    private var mScaleType = ScaleType.FIT_CENTER

    private var hasMultiTouch = false
    private var hasDrawable = false
    private var isKnowSize = false
    private var hasOverTranslate = false
    private var isRotateEnable = false
    var isDispatch = true

    // 当前是否处于放大状态
    private var isZoomUp = false
    private var canRotate = false

    private var imgLargeWidth = false
    private var imgLargeHeight = false

    private var mRotateFlag = 0f
    private var mDegrees = 0f
    private var mScale = 1.0f
    private var mTranslateX = 0
    private var mTranslateY = 0

    private val mBaseRect = RectF()
    private var mCropRect = RectF()
    private val mImgRect = RectF()
    private val mTmpRect = RectF()
    private val mCommonRect = RectF()

    private val mScreenCenter = PointF()
    private val mScaleCenter = PointF()
    private val mRotateCenter = PointF()

    private val mTranslate: Transform = Transform()

    private var mClip: RectF? = null
    private var mCompleteCallBack: Runnable? = null

    private var mLongClick: OnLongClickListener? = null

    private var isDrawComplete = false


    private var aspectX = -1F
    private var aspectY = -1F
    private var cropMargin = 0

    private var baseScale = 0f

    private var onImageLoadListener: OnImageLoadListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    )

    init {
        super.setScaleType(ScaleType.MATRIX)
        mRotateDetector =
            RotateGestureDetector(
                object :
                    RotateGestureDetector.OnRotateListener {
                    override fun onRotate(degrees: Float, focusX: Float, focusY: Float) {
                        mRotateFlag += degrees
                        if (canRotate) {
                            mDegrees += degrees
                            mAnimMatrix.postRotate(degrees, focusX, focusY)
                        } else {
                            if (abs(mRotateFlag) >= mMinRotate) {
                                canRotate = true
                                mRotateFlag = 0f
                            }
                        }
                    }

                })
        mDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                mLongClick?.onLongClick(this@TransformImageView)
            }

            override fun onDown(e: MotionEvent): Boolean {
                hasOverTranslate = false
                hasMultiTouch = false
                canRotate = false
                removeCallbacks(mClickRunnable)
                return false
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (hasMultiTouch) return false
                if (!imgLargeWidth && !imgLargeHeight) return false
                if (mTranslate.isRunning) return false
                var vx = velocityX
                var vy = velocityY
                if (Math.round(mImgRect.left) >= mCropRect.left || Math.round(
                        mImgRect.right
                    ) <= mCropRect.right
                ) {
                    vx = 0f
                }
                if (Math.round(mImgRect.top) >= mCropRect.top || Math.round(
                        mImgRect.bottom
                    ) <= mCropRect.bottom
                ) {
                    vy = 0f
                }
                if (canRotate || mDegrees % 90 != 0f) {
                    var toDegrees = (mDegrees / 90).toInt() * 90.toFloat()
                    val remainder = mDegrees % 90
                    if (remainder > 45) toDegrees += 90f else if (remainder < -45) toDegrees -= 90f
                    mTranslate.withRotate(mDegrees.toInt(), toDegrees.toInt())
                    mDegrees = toDegrees
                }
                mTranslate.withFling(vx, vy)
                return super.onFling(e1, e2, velocityX, velocityY)
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                var distanceX = distanceX
                var distanceY = distanceY
                if (mTranslate.isRunning) {
                    mTranslate.stop()
                }
                if (canScrollHorizontallySelf(distanceX)) {
                    if (distanceX < 0 && mImgRect.left - distanceX > mCropRect.left) distanceX =
                        mImgRect.left
                    if (distanceX > 0 && mImgRect.right - distanceX < mCropRect.right) distanceX =
                        mImgRect.right - mCropRect.right
                    mAnimMatrix.postTranslate(-distanceX, 0f)
                    mTranslateX -= distanceX.toInt()
                } else if (imgLargeWidth || hasMultiTouch || hasOverTranslate) {
                    checkRect()
                    if (!hasMultiTouch) {
                        if (distanceX < 0 && mImgRect.left - distanceX > mCommonRect.left) distanceX =
                            resistanceScrollByX(mImgRect.left - mCommonRect.left, distanceX)
                        if (distanceX > 0 && mImgRect.right - distanceX < mCommonRect.right) distanceX =
                            resistanceScrollByX(mImgRect.right - mCommonRect.right, distanceX)
                    }
                    mTranslateX -= distanceX.toInt()
                    mAnimMatrix.postTranslate(-distanceX, 0f)
                    hasOverTranslate = true
                }
                if (canScrollVerticallySelf(distanceY)) {
                    if (distanceY < 0 && mImgRect.top - distanceY > mCropRect.top) distanceY =
                        mImgRect.top
                    if (distanceY > 0 && mImgRect.bottom - distanceY < mCropRect.bottom) distanceY =
                        mImgRect.bottom - mCropRect.bottom
                    mAnimMatrix.postTranslate(0f, -distanceY)
                    mTranslateY -= distanceY.toInt()
                } else if (imgLargeHeight || hasOverTranslate || hasMultiTouch) {
                    checkRect()
                    if (!hasMultiTouch) {
                        if (distanceY < 0 && mImgRect.top - distanceY > mCommonRect.top) distanceY =
                            resistanceScrollByY(mImgRect.top - mCommonRect.top, distanceY)
                        if (distanceY > 0 && mImgRect.bottom - distanceY < mCommonRect.bottom) distanceY =
                            resistanceScrollByY(mImgRect.bottom - mCommonRect.bottom, distanceY)
                    }
                    mAnimMatrix.postTranslate(0f, -distanceY)
                    mTranslateY -= distanceY.toInt()
                    hasOverTranslate = true
                }
                executeTranslate()
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                postDelayed(mClickRunnable, 250)
                return false
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                mTranslate.stop()
                val from: Float
                val to: Float
                val imageCenterX = mImgRect.left + mImgRect.width() / 2
                val imageCenterY = mImgRect.top + mImgRect.height() / 2
                mScaleCenter[imageCenterX] = imageCenterY
                mRotateCenter[imageCenterX] = imageCenterY
                mTranslateX = 0
                mTranslateY = 0
                if (mScale > 1) {
                    from = mScale
                    to = 1f
                } else {
                    from = mScale
                    to = mMaxScale
                    mScaleCenter[e.x] = e.y
                }
                mTmpMatrix.reset()
                mTmpMatrix.postTranslate(-mBaseRect.left, -mBaseRect.top)
                mTmpMatrix.postTranslate(mRotateCenter.x, mRotateCenter.y)
                mTmpMatrix.postTranslate(-mBaseRect.width() / 2, -mBaseRect.height() / 2)
                mTmpMatrix.postRotate(mDegrees, mRotateCenter.x, mRotateCenter.y)
                mTmpMatrix.postScale(to, to, mScaleCenter.x, mScaleCenter.y)
                mTmpMatrix.postTranslate(mTranslateX.toFloat(), mTranslateY.toFloat())
                mTmpMatrix.mapRect(mTmpRect, mBaseRect)
                doTranslateReset(mTmpRect)
                isZoomUp = !isZoomUp
                mTranslate.withScale(from, to)
                mTranslate.start()
                return false
            }
        })
        mScaleDetector = ScaleGestureDetector(context, object : OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                if (java.lang.Float.isNaN(scaleFactor) || java.lang.Float.isInfinite(scaleFactor)) return false
                if (mScale > mMaxScale) {
                    return true
                }
                mScale *= scaleFactor
                mScaleCenter[detector.focusX] = detector.focusY
                mAnimMatrix.postScale(
                    scaleFactor,
                    scaleFactor,
                    detector.focusX,
                    detector.focusY
                )
                executeTranslate()
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {}
        })

        val density = resources.displayMetrics.density
        MAX_FLING_OVER_SCROLL = (density * 30).toInt()
        MAX_OVER_RESISTANCE = (density * 140).toInt()

        mMinRotate =
            MIN_ROTATE
        mAnimDuring =
            ANIM_DURING
        mMaxScale =
            MAX_SCALE
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        mClickListener = l
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (isDrawComplete) {
            super.setScaleType(scaleType)
        } else {
            mScaleType = scaleType
            initBase()
        }
    }

    open fun setScaleValue(scale: Float) {
        mScale = scale
    }

    open fun getNewScaleType(): ScaleType? {
        return mScaleType
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        mLongClick = l
    }

    /**
     * 设置最大可以缩放的倍数
     */
    fun setMaxScale(maxScale: Float) {
        mMaxScale = maxScale
    }

    override fun setImageResource(resId: Int) {
        var drawable: Drawable? = null
        try {
            drawable = resources.getDrawable(resId)
        } catch (ignored: java.lang.Exception) {
        }
        setImageDrawable(drawable)
    }

    private var originalBitmap: Bitmap? = null

    fun getOriginalBitmap(): Bitmap? {
        return originalBitmap
    }

    override fun setImageBitmap(bm: Bitmap?) {
        var bm = bm
        if (bm == null || bm.width == 0 || bm.height == 0) {
            return
        }
        originalBitmap = bm
        if (loadMaxSize == 0) {
            loadMaxSize = Math.max(bm.width, bm.height)
        }
        val ratio = bm.width * 1.00f / bm.height * 1.00f
        if (bm.width > loadMaxSize) {
            bm = Bitmap.createScaledBitmap(
                bm,
                loadMaxSize,
                (loadMaxSize / ratio).toInt(),
                false
            )
        }
        if (bm!!.height > loadMaxSize) {
            bm = Bitmap.createScaledBitmap(
                bm,
                (loadMaxSize * ratio).toInt(),
                loadMaxSize,
                false
            )
        }
        super.setImageBitmap(bm)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable == null) {
            hasDrawable = false
            return
        }
        if (!hasSize(drawable)) {
            return
        }
        hasDrawable = true
        if (originalBitmap == null) {
            if (drawable is BitmapDrawable) {
                originalBitmap = drawable.bitmap
            } else if (drawable is AnimationDrawable) {
                val drawable2 = drawable.getFrame(0)
                if (drawable2 is BitmapDrawable) {
                    originalBitmap = drawable2.bitmap
                }
            }
        }
        if (onImageLoadListener != null) {
            onImageLoadListener!!.onImageLoaded(
                drawable.intrinsicWidth.toFloat(),
                drawable.intrinsicHeight.toFloat()
            )
            onImageLoadListener = null
        }
        restoreInfo?.also {
            mScaleType = it.scaleType
            mCropRect = it.mWidgetRect!!
            aspectX = it.mCropX
            aspectY = it.mCropY
            initBase()
            post { restoreCrop() }
        } ?: run {
            initBase()
        }
    }

    private var restoreInfo: Info? = null

    fun setRestoreInfo(restoreInfo: Info?) {
        this.restoreInfo = restoreInfo
    }

    /**
     * 恢复状态
     */
    private fun restoreCrop() {
        val info = restoreInfo
        mTranslateX = 0
        mTranslateY = 0

        info?.mImgRect?.let {
            val tcx = it.left + it.width() / 2
            val tcy = it.top + it.height() / 2
            mScaleCenter[mImgRect.left + mImgRect.width() / 2] =
                mImgRect.top + mImgRect.height() / 2
            mRotateCenter.set(mScaleCenter)

            // 将图片旋转回正常位置，用以计算
            mAnimMatrix.postRotate(-mDegrees, mScaleCenter.x, mScaleCenter.y)
            mAnimMatrix.mapRect(mImgRect, mBaseRect)

            // 缩放
            val scaleX = it.width() / mBaseRect.width()
            val scaleY = it.height() / mBaseRect.height()
            val scale = if (scaleX > scaleY) scaleX else scaleY
            mAnimMatrix.postRotate(mDegrees, mScaleCenter.x, mScaleCenter.y)
            mAnimMatrix.mapRect(mImgRect, mBaseRect)
            mDegrees %= 360
            mTranslate.withTranslate(
                0, 0,
                (tcx - mScaleCenter.x).toInt(),
                (tcy - mScaleCenter.y).toInt()
            )
            mTranslate.withScale(mScale, scale)
            mTranslate.withRotate(mDegrees.toInt(), info.mDegrees.toInt(), mAnimDuring * 2 / 3)
            mTranslate.start()
            restoreInfo = null
        }

    }

    interface OnImageLoadListener {
        fun onImageLoaded(w: Float, h: Float)
    }

    fun setOnImageLoadListener(listener: OnImageLoadListener) {
        this.onImageLoadListener = listener
    }

    private fun hasSize(d: Drawable): Boolean {
        return (d.intrinsicHeight > 0 && d.intrinsicWidth > 0
                || d.minimumWidth > 0 && d.minimumHeight > 0
                || d.bounds.width() > 0 && d.bounds.height() > 0)
    }

    private fun getDrawableWidth(d: Drawable): Int {
        var width = d.intrinsicWidth
        if (width <= 0) width = d.minimumWidth
        if (width <= 0) width = d.bounds.width()
        return width
    }

    private fun getDrawableHeight(d: Drawable): Int {
        var height = d.intrinsicHeight
        if (height <= 0) height = d.minimumHeight
        if (height <= 0) height = d.bounds.height()
        return height
    }

    fun initBase() {
        if (!hasDrawable) return
        if (!isKnowSize) return
        mBaseMatrix.reset()
        mAnimMatrix.reset()
        isZoomUp = false
        val img = drawable
        val w = width
        val h = height
        val drawableWidth = getDrawableWidth(img)
        val drawableHeight = getDrawableHeight(img)
        mBaseRect[0f, 0f, drawableWidth.toFloat()] = drawableHeight.toFloat()

        // 以图片中心点居中位移
        val tx = (w - drawableWidth) / 2
        val ty = (h - drawableHeight) / 2
        var sx = 1f
        var sy = 1f

        // 缩放，默认不超过屏幕大小
        if (drawableWidth > w) {
            sx = w.toFloat() / drawableWidth
        }
        if (drawableHeight > h) {
            sy = h.toFloat() / drawableHeight
        }
        baseScale = Math.min(sx, sy)
        mBaseMatrix.reset()
        mBaseMatrix.postTranslate(tx.toFloat(), ty.toFloat())
        mBaseMatrix.postScale(baseScale, baseScale, mScreenCenter.x, mScreenCenter.y)
        mBaseMatrix.mapRect(mBaseRect)
        mScaleCenter.set(mScreenCenter)
        mRotateCenter.set(mScaleCenter)
        executeTranslate()
        when (mScaleType) {
            ScaleType.CENTER -> initCenter()
            ScaleType.CENTER_CROP -> initCenterCrop()
            ScaleType.CENTER_INSIDE -> initCenterInside()
            ScaleType.FIT_CENTER -> initFitCenter()
            ScaleType.FIT_START -> initFitStart()
            ScaleType.FIT_END -> initFitEnd()
            ScaleType.FIT_XY -> initFitXY()
            else -> {}
        }
        drawMatrixComplete()
    }

    private fun initCenter() {
        mAnimMatrix.postScale(1f, 1f, mScreenCenter.x, mScreenCenter.y)
        executeTranslate()
        resetBase()
    }

    private fun initCenterCrop() {
        val widthScale = mCropRect.width() / mImgRect.width()
        val heightScale = mCropRect.height() / mImgRect.height()
        mScale = Math.max(widthScale, heightScale)
        mAnimMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y)
        executeTranslate()
        resetBase()
    }

    private fun initCenterInside() {
        //控件大于图片，即可完全显示图片，相当于Center，反之，相当于FitCenter
        if (mCropRect.width() > mImgRect.width()) {
            initCenter()
        } else {
            initFitCenter()
        }
        val widthScale = mCropRect.width() / mImgRect.width()
        if (widthScale > mMaxScale) {
            mMaxScale = widthScale
        }
    }

    private fun initFitCenter() {
        val widthScale = mCropRect.width() / mImgRect.width()
        val heightScale = mCropRect.height() / mImgRect.height()
        mScale = Math.min(widthScale, heightScale)
        mAnimMatrix.postScale(mScale, mScale, mScreenCenter.x, mScreenCenter.y)
        executeTranslate()
        resetBase()
        if (widthScale > mMaxScale) {
            mMaxScale = widthScale
        }
    }

    private fun initFitStart() {
        initFitCenter()
        val ty = -mImgRect.top
        mAnimMatrix.postTranslate(0f, ty)
        executeTranslate()
        resetBase()
        mTranslateY += ty.toInt()
    }

    private fun initFitEnd() {
        initFitCenter()
        val ty = mCropRect.bottom - mImgRect.bottom
        mTranslateY += ty.toInt()
        mAnimMatrix.postTranslate(0f, ty)
        executeTranslate()
        resetBase()
    }

    private fun initFitXY() {
        val widthScale = mCropRect.width() / mImgRect.width()
        val heightScale = mCropRect.height() / mImgRect.height()
        mAnimMatrix.postScale(widthScale, heightScale, mScreenCenter.x, mScreenCenter.y)
        executeTranslate()
        resetBase()
    }

    private fun resetBase() {
        val img = drawable
        mBaseRect[0f, 0f, getDrawableWidth(img).toFloat()] = getDrawableHeight(img).toFloat()
        mBaseMatrix.set(mSynthesisMatrix)
        mBaseMatrix.mapRect(mBaseRect)
        mScale = 1f
        mTranslateX = 0
        mTranslateY = 0
        mAnimMatrix.reset()
    }

    private fun executeTranslate() {
        mSynthesisMatrix.set(mBaseMatrix)
        mSynthesisMatrix.postConcat(mAnimMatrix)
        imageMatrix = mSynthesisMatrix
        mAnimMatrix.mapRect(mImgRect, mBaseRect)
        imgLargeWidth = mImgRect.width() >= mCropRect.width()
        imgLargeHeight = mImgRect.height() >= mCropRect.height()
    }

    fun setRotateEnable(rotateEnable: Boolean) {
        isRotateEnable = rotateEnable
    }

    fun setCropRatio(aspectX: Int, aspectY: Int) {
        var aspectX = aspectX
        var aspectY = aspectY
        if (cropAnim != null && cropAnim!!.isRunning) {
            cropAnim!!.cancel()
        }
        if (aspectX <= 0 || aspectY <= 0) {
            mCropRect[0f, 0f, width.toFloat()] = height.toFloat()
            mScaleType = ScaleType.CENTER_INSIDE
            initBase()
            invalidate()
            return
        }
        mScaleType = ScaleType.CENTER_CROP
        resetCropSize(width, height)
    }

    fun setCropMargin(cropMargin: Int) {
        this.cropMargin = cropMargin
    }

    fun getCropWidth(): Int {
        return mCropRect.width().toInt()
    }

    fun getCropHeight(): Int {
        return mCropRect.height().toInt()
    }

    private fun resetCropSize(w: Int, h: Int) {
        var left = 0f
        var top = 0f
        var right = w.toFloat()
        var bottom = h.toFloat()
        if (aspectY != -1F && aspectX != -1F) {
            val cropRatio: Float = aspectX * 1.00f / aspectY
            val viewRatio = w * 1.00f / h
            if (h > w) { //view的高>宽
                val top1: Float = (h - (w - cropMargin * 2) * 1.00f / cropRatio) * 1.00f / 2
                if (cropRatio >= 1) { //宽比例剪裁
                    left = cropMargin.toFloat()
                    right = w - left
                    top = top1
                    bottom = h - top
                } else if (cropRatio < 1) { //高比例剪裁
                    if (cropRatio > viewRatio) { //剪裁比例大于view宽高比，说明以宽充满，剪裁的高肯定不会超出view的高
                        left = cropMargin.toFloat()
                        right = w - left
                        top = top1
                        bottom = h - top
                    } else { //剪裁比例小于view宽高比,说明以高充满，宽度肯定不会超过view的宽度
                        top = cropMargin.toFloat()
                        bottom = h - top
                        left = (w - (h - cropMargin * 2) * cropRatio) / 2
                        right = w - left
                    }
                }
            }
            anim(left, top, right, bottom)
        } else {
            mCropRect[left, top, right] = bottom
            initBase()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        isKnowSize = true
        mScreenCenter[w / 2.0f] = h / 2.0f
        resetCropSize(w, h)
        setImageDrawable(drawable)
    }

    override fun onDraw(canvas: Canvas) {
        try {
            super.onDraw(canvas)
        } catch (ignored: java.lang.Exception) {
            loadMaxSize = (loadMaxSize * 0.8).toInt()
            setImageBitmap(originalBitmap)
        }
    }

    override fun draw(canvas: Canvas) {
        if (mClip != null) {
            canvas.clipRect(mClip!!)
            mClip = null
        }
        super.draw(canvas)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        //继承控件拦截事件消费
        if (!isDispatch) return super.dispatchTouchEvent(event)
        val action = event.actionMasked
        if (event.pointerCount >= 2) hasMultiTouch = true
        mDetector!!.onTouchEvent(event)
        if (isRotateEnable) {
            mRotateDetector!!.onTouchEvent(event)
        }
        mScaleDetector!!.onTouchEvent(event)
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            onUp()
        }
        return super.dispatchTouchEvent(event)
    }

    private fun onUp() {
        if (mTranslate.isRunning) return
        if (canRotate || mDegrees % 90 != 0f) {
            var toDegrees = (mDegrees / 90).toInt() * 90.toFloat()
            val remainder = mDegrees % 90
            if (remainder > 45) toDegrees += 90f else if (remainder < -45) toDegrees -= 90f
            mTranslate.withRotate(mDegrees.toInt(), toDegrees.toInt())
            mDegrees = toDegrees
        }
        val cx = mImgRect.left * 1.00f + mImgRect.width() / 2
        val cy = mImgRect.top * 1.00f + mImgRect.height() / 2
        mRotateCenter[cx] = cy
        if (mScale < 1) {
            mTranslate.withScale(mScale, 1f)
            mScale = 1f
        } else if (mScale > mMaxScale) {
            mTranslate.withScale(mScale, mMaxScale)
            mScale = mMaxScale
        }
        mScaleCenter[cx] = cy
        mTranslateX = 0
        mTranslateY = 0
        mTmpMatrix.reset()
        mTmpMatrix.postTranslate(-mBaseRect.left, -mBaseRect.top)
        mTmpMatrix.postTranslate(cx - mBaseRect.width() / 2, cy - mBaseRect.height() / 2)
        mTmpMatrix.postScale(mScale, mScale, mScaleCenter.x, mScaleCenter.y)
        mTmpMatrix.postRotate(mDegrees, cx, cy)
        mTmpMatrix.mapRect(mTmpRect, mBaseRect)
        doTranslateReset(mTmpRect)
        mTranslate.start()
    }

    private fun doTranslateReset(imgRect: RectF) {
        var tx = 0
        var ty = 0
        val width = mCropRect.width().toInt()
        val height = mCropRect.height().toInt()
        if (imgRect.width() <= width) {
            if (!isImageCenterWidth(imgRect)) {
                tx = if (aspectX > 0 && aspectY > 0) {
                    (imgRect.left - mCropRect.left).toInt()
                } else {
                    (-((mCropRect.width() - imgRect.width()) / 2 - imgRect.left)).toInt()
                }
            }
        } else {
            if (imgRect.left > mCropRect.left) {
                tx = (imgRect.left - mCropRect.left).toInt()
            } else if (imgRect.right < mCropRect.right) {
                tx = (imgRect.right - mCropRect.right).toInt()
            }
        }
        if (imgRect.height() <= height) {
            if (!isImageCenterHeight(imgRect)) ty = if (aspectX > 0 && aspectY > 0) {
                (imgRect.top - mCropRect.top).toInt()
            } else {
                (-((mCropRect.height() - imgRect.height()) / 2 - imgRect.top)).toInt()
            }
        } else {
            if (imgRect.top > mCropRect.top) {
                ty = (imgRect.top - mCropRect.top).toInt()
            } else if (imgRect.bottom < mCropRect.bottom) {
                ty = (imgRect.bottom - mCropRect.bottom).toInt()
            }
        }
        if (tx != 0 || ty != 0) {
            if (!mTranslate.mFlingScroller.isFinished) mTranslate.mFlingScroller.abortAnimation()
            mTranslate.withTranslate(mTranslateX, mTranslateY, -tx, -ty)
        }
    }

    private fun isImageCenterHeight(rect: RectF): Boolean {
        return Math.abs(Math.round(rect.top) - (mCropRect.height() - rect.height()) / 2) < 1
    }

    private fun isImageCenterWidth(rect: RectF): Boolean {
        return Math.abs(Math.round(rect.left) - (mCropRect.width() - rect.width()) / 2) < 1
    }

    private fun resistanceScrollByX(
        overScroll: Float,
        detalX: Float
    ): Float {
        return detalX * (Math.abs(Math.abs(overScroll) - MAX_OVER_RESISTANCE) / MAX_OVER_RESISTANCE.toFloat())
    }

    private fun resistanceScrollByY(
        overScroll: Float,
        detalY: Float
    ): Float {
        return detalY * (Math.abs(Math.abs(overScroll) - MAX_OVER_RESISTANCE) / MAX_OVER_RESISTANCE.toFloat())
    }

    /**
     * 匹配两个Rect的共同部分输出到out，若无共同部分则输出0，0，0，0
     */
    private fun mapRect(r1: RectF, r2: RectF, out: RectF) {
        val l: Float = if (r1.left > r2.left) r1.left else r2.left
        val r: Float = if (r1.right < r2.right) r1.right else r2.right
        if (l > r) {
            out[0f, 0f, 0f] = 0f
            return
        }
        val t: Float = if (r1.top > r2.top) r1.top else r2.top
        val b: Float = if (r1.bottom < r2.bottom) r1.bottom else r2.bottom
        if (t > b) {
            out[0f, 0f, 0f] = 0f
            return
        }
        out[l, t, r] = b
    }

    private fun checkRect() {
        if (!hasOverTranslate) {
            mapRect(mCropRect, mImgRect, mCommonRect)
        }
    }

    private val mClickRunnable = Runnable { mClickListener?.onClick(this@TransformImageView) }

    //绘制完成设置状态
    private fun drawMatrixComplete() {
        if (isDrawComplete) return
        postDelayed({ isDrawComplete = true }, 100)
    }

    private fun canScrollHorizontallySelf(direction: Float): Boolean {
        if (mImgRect.width() <= mCropRect.width()) return false
        if (direction < 0 && mImgRect.left.roundToInt() == mCropRect.left.roundToInt()) return false
        return !(direction > 0 && mImgRect.right.roundToInt() == mCropRect.right.roundToInt())
    }

    private fun canScrollVerticallySelf(direction: Float): Boolean {
        if (mImgRect.height() <= mCropRect.height()) return false
        if (direction < 0 && mImgRect.top.roundToInt() == mCropRect.top.roundToInt()) return false
        return !(direction > 0 && mImgRect.bottom.roundToInt() == mCropRect.bottom.roundToInt())
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return if (hasMultiTouch) true else canScrollHorizontallySelf(direction.toFloat())
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return if (hasMultiTouch) true else canScrollVerticallySelf(direction.toFloat())
    }

    fun getInfo(): Info? {
        return Info(
            mImgRect,
            mCropRect,
            mDegrees,
            mScaleType.name,
            aspectX,
            aspectY,
            getTranslateX(),
            getTranslateY(),
            getScale()
        )
    }

    interface ClipCalculate {
        fun calculateTop(): Float
    }

    fun rotate(degrees: Float) {
        mDegrees += degrees
        val centerX = (mCropRect.left + mCropRect.width() / 2).toInt()
        val centerY = (mCropRect.top + mCropRect.height() / 2).toInt()
        mAnimMatrix.postRotate(degrees, centerX.toFloat(), centerY.toFloat())
        executeTranslate()
    }

    fun generateCropBitmapFromView(backgroundColor: Int): Bitmap? {
        (context as Activity).runOnUiThread { invalidate() }
        var bitmap = getViewBitmap(this@TransformImageView)
        try {
            bitmap = Bitmap.createBitmap(
                bitmap!!, mCropRect.left.toInt(), mCropRect.top.toInt(),
                mCropRect.width().toInt(), mCropRect.height().toInt()
            )
        } catch (ignored: java.lang.Exception) {
        }
        return bitmap
    }

    /**
     * @return view的截图，在InVisible时也可以获取到bitmap
     */
    fun getViewBitmap(view: View): Bitmap? {
        view.measure(
            MeasureSpec.makeMeasureSpec(view.measuredWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(view.measuredHeight, MeasureSpec.EXACTLY)
        )
        view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache(true)
        return view.getDrawingCache(true)
    }

    /**
     * 生成剪裁图片
     *
     * @return bitmap
     */
    fun generateCropBitmap(): Bitmap? {
        if (originalBitmap == null) {
            return null
        }
        //水平平移像素点
        val x = Math.abs(getTranslateX())
        //垂直平移像素点
        val y = Math.abs(getTranslateY())
        //缩放比例
        val scale = mScale
        //原图宽度(Glide压缩过的，Glide默认加载会减小大图的宽高)
        val bw: Int = originalBitmap?.width!!
        //原图高度(Glide压缩过的)
        val bh: Int = originalBitmap?.height!!
        //图片宽高比
        val bRatio = bw * 1.00f / (bh * 1.00f)
        val endW: Float
        val endH: Float
        var endX: Float
        var endY: Float
        val cropWidth = mCropRect.width()
        val cropHeight = mCropRect.height()
        val cropRatio = cropWidth * 1.00f / (cropHeight * 1.00f)

        //图片比例小于剪裁比例，以宽填满，高自适应，计算高
        if (bRatio < cropRatio) {
            endW = bw / scale
            endH = endW / cropRatio
            endX = bw * x / (cropWidth * scale * 1.00f)
            endY = bw * y / (cropWidth * scale * 1.00f)
        } else {
            endH = bh / scale
            endW = cropRatio * endH
            endX = bh * x / (cropHeight * scale * 1.00f)
            endY = bh * y / (cropHeight * scale * 1.00f)
        }
        if (endX + endW > bw) {
            endX = bw - endW
            if (endX < 0) {
                endX = 0f
            }
        }
        if (endY + endH > bh) {
            endY = bh - endH
            if (endY < 0) {
                endY = 0f
            }
        }
        var bitmap1: Bitmap?
        try {
            bitmap1 = Bitmap.createBitmap(
                originalBitmap!!,
                endX.toInt(),
                endY.toInt(),
                endW.toInt(),
                endH.toInt()
            )
        } catch (ignored: Exception) {
            bitmap1 = generateCropBitmapFromView(Color.BLACK)
        }
        return bitmap1
    }

    fun getTranslateX(): Float {
        return mImgRect.left - mCropRect.left
    }

    fun getTranslateY(): Float {
        return mImgRect.top - mCropRect.top
    }

    fun getScale(): Float {
        return if (mScale <= 1F) {
            1F
        } else mScale
    }

    fun dp(dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5).toInt()
    }

    private var cropAnim: ValueAnimator? = null

    private fun anim(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val oldLeft = mCropRect.left
        val oldTop = mCropRect.top
        val oldRight = mCropRect.right
        val oldBottom = mCropRect.bottom
        if (oldRight == 0f || oldBottom == 0f || oldLeft == left && oldBottom == bottom && oldRight == right && oldTop == top
        ) {
            mCropRect[left, top, right] = bottom
            initBase()
            invalidate()
            return
        }
        if (cropAnim == null) {
            cropAnim = ObjectAnimator.ofFloat(0.0f, 1.0f).setDuration(400)
            cropAnim?.interpolator = DecelerateInterpolator()
        }
        cropAnim?.removeAllUpdateListeners()
        cropAnim?.removeAllListeners()
        cropAnim?.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            mCropRect.left = (left - oldLeft) * value + oldLeft
            mCropRect.top = (top - oldTop) * value + oldTop
            mCropRect.right = (right - oldRight) * value + oldRight
            mCropRect.bottom = (bottom - oldBottom) * value + oldBottom
            initBase()
            invalidate()
        }
        cropAnim?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                initBase()
                invalidate()
            }
        })
        cropAnim?.start()
    }

    fun changeSize(isAnim: Boolean, endWidth: Int, endHeight: Int) {
        if (isAnim) {
            val startWidth = width
            val startHeight = height
            val anim = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(200)
            anim.interpolator = DecelerateInterpolator()
            anim.addUpdateListener { animation ->
                val ratio = animation.animatedValue as Float
                val params = layoutParams
                params.width = ((endWidth - startWidth) * ratio + startWidth).toInt()
                params.height = ((endHeight - startHeight) * ratio + startHeight).toInt()
                layoutParams = params
                setImageDrawable(drawable)
            }
            anim.start()
        } else {
            val params = layoutParams
            params.width = endWidth
            params.height = endHeight
            layoutParams = params
        }
    }

    private inner class InterpolatorProxy : Interpolator {
        private var mTarget: Interpolator?
        fun setTargetInterpolator(interpolator: Interpolator?) {
            mTarget = interpolator
        }

        override fun getInterpolation(input: Float): Float {
            return if (mTarget != null) {
                mTarget!!.getInterpolation(input)
            } else input
        }

        init {
            mTarget = DecelerateInterpolator()
        }
    }

    private inner class Transform : Runnable {
        var isRunning = false
        var mTranslateScroller: OverScroller
        var mFlingScroller: OverScroller
        var mScaleScroller: Scroller
        var mClipScroller: Scroller
        var mRotateScroller: Scroller
        var C: ClipCalculate? = null
        var mLastFlingX = 0
        var mLastFlingY = 0
        var mLastTranslateX = 0
        var mLastTranslateY = 0
        var mClipRect = RectF()
        var mInterpolatorProxy: InterpolatorProxy = InterpolatorProxy()
        fun setInterpolator(interpolator: Interpolator?) {
            mInterpolatorProxy.setTargetInterpolator(interpolator)
        }

        fun withTranslate(startX: Int, startY: Int, deltaX: Int, deltaY: Int) {
            mLastTranslateX = 0
            mLastTranslateY = 0
            mTranslateScroller.startScroll(startX, startY, deltaX, deltaY, mAnimDuring)
        }

        fun withScale(form: Float, to: Float) {
            mScaleScroller.startScroll(
                (form * 10000).toInt(),
                0,
                ((to - form) * 10000).toInt(),
                0,
                mAnimDuring
            )
        }

        fun withRotate(fromDegrees: Int, toDegrees: Int) {
            mRotateScroller.startScroll(fromDegrees, 0, toDegrees - fromDegrees, 0, mAnimDuring)
        }

        fun withRotate(fromDegrees: Int, toDegrees: Int, during: Int) {
            mRotateScroller.startScroll(fromDegrees, 0, toDegrees - fromDegrees, 0, during)
        }

        fun withFling(velocityX: Float, velocityY: Float) {
            mLastFlingX = if (velocityX < 0) Int.MAX_VALUE else 0
            var distanceX =
                (if (velocityX > 0) Math.abs(mImgRect.left) else mImgRect.right - mCropRect.right).toInt()
            distanceX = if (velocityX < 0) Int.MAX_VALUE - distanceX else distanceX
            var minX = if (velocityX < 0) distanceX else 0
            var maxX = if (velocityX < 0) Int.MAX_VALUE else distanceX
            val overX = if (velocityX < 0) Int.MAX_VALUE - minX else distanceX
            mLastFlingY = if (velocityY < 0) Int.MAX_VALUE else 0
            var distanceY = (if (velocityY > 0) Math.abs(mImgRect.top - mCropRect.top) else mImgRect.bottom - mCropRect.bottom).toInt()
            distanceY = if (velocityY < 0) Int.MAX_VALUE - distanceY else distanceY
            var minY = if (velocityY < 0) distanceY else 0
            var maxY = if (velocityY < 0) Int.MAX_VALUE else distanceY
            val overY = if (velocityY < 0) Int.MAX_VALUE - minY else distanceY
            if (velocityX == 0f) {
                maxX = 0
                minX = 0
            }
            if (velocityY == 0f) {
                maxY = 0
                minY = 0
            }
            mFlingScroller.fling(
                mLastFlingX,
                mLastFlingY,
                velocityX.toInt(),
                velocityY.toInt(),
                minX,
                maxX,
                minY,
                maxY,
                if (Math.abs(overX) < MAX_FLING_OVER_SCROLL * 2) 0 else MAX_FLING_OVER_SCROLL,
                if (Math.abs(overY) < MAX_FLING_OVER_SCROLL * 2) 0 else MAX_FLING_OVER_SCROLL
            )
        }

        fun start() {
            isRunning = true
            postExecute()
        }

        fun stop() {
            removeCallbacks(this)
            mTranslateScroller.abortAnimation()
            mScaleScroller.abortAnimation()
            mFlingScroller.abortAnimation()
            mRotateScroller.abortAnimation()
            isRunning = false
        }

        override fun run() {
            if (!isRunning) return
            var endAnim = true
            if (mScaleScroller.computeScrollOffset()) {
                mScale = mScaleScroller.currX / 10000f
                endAnim = false
            }
            if (mTranslateScroller.computeScrollOffset()) {
                val tx = mTranslateScroller.currX - mLastTranslateX
                val ty = mTranslateScroller.currY - mLastTranslateY
                mTranslateX += tx
                mTranslateY += ty
                mLastTranslateX = mTranslateScroller.currX
                mLastTranslateY = mTranslateScroller.currY
                endAnim = false
            }
            if (mFlingScroller.computeScrollOffset()) {
                val x = mFlingScroller.currX - mLastFlingX
                val y = mFlingScroller.currY - mLastFlingY
                mLastFlingX = mFlingScroller.currX
                mLastFlingY = mFlingScroller.currY
                mTranslateX += x
                mTranslateY += y
                endAnim = false
            }
            if (mRotateScroller.computeScrollOffset()) {
                mDegrees = mRotateScroller.currX.toFloat()
                endAnim = false
            }
            if (mClipScroller.computeScrollOffset() || mClip != null) {
                val sx = mClipScroller.currX / 10000f
                val sy = mClipScroller.currY / 10000f
                mTmpMatrix.setScale(
                    sx,
                    sy,
                    (mImgRect.left + mImgRect.right) / 2,
                    C!!.calculateTop()
                )
                mTmpMatrix.mapRect(mClipRect, mImgRect)
                if (sx == 1f) {
                    mClipRect.left = mCropRect.left
                    mClipRect.right = mCropRect.right
                }
                if (sy == 1f) {
                    mClipRect.top = mCropRect.top
                    mClipRect.bottom = mCropRect.bottom
                }
                mClip = mClipRect
            }
            if (!endAnim) {
                applyAnim()
                postExecute()
            } else {
                isRunning = false
                if (aspectX > 0 && aspectY > 0) {
                    return
                }
                // 修复动画结束后边距有些空隙，
                var needFix = false
                if (imgLargeWidth) {
                    if (mImgRect.left > 0) {
                        mTranslateX -= mCropRect.left.toInt()
                    } else if (mImgRect.right < mCropRect.width()) {
                        mTranslateX -= (mCropRect.width() - mImgRect.right).toInt()
                    }
                    needFix = true
                }
                if (imgLargeHeight) {
                    if (mImgRect.top > 0) {
                        mTranslateY -= mCropRect.top.toInt()
                    } else if (mImgRect.bottom < mCropRect.height()) {
                        mTranslateY -= (mCropRect.height() - mImgRect.bottom).toInt()
                    }
                    needFix = true
                }
                if (needFix) {
                    applyAnim()
                }
                invalidate()
            }
            mCompleteCallBack?.let {
                it.run()
                mCompleteCallBack = null
            }
        }

        private fun applyAnim() {
            mAnimMatrix.reset()
            mAnimMatrix.postTranslate(-mBaseRect.left, -mBaseRect.top)
            mAnimMatrix.postTranslate(mRotateCenter.x, mRotateCenter.y)
            mAnimMatrix.postTranslate(-mBaseRect.width() / 2, -mBaseRect.height() / 2)
            mAnimMatrix.postRotate(mDegrees, mRotateCenter.x, mRotateCenter.y)
            mAnimMatrix.postScale(mScale, mScale, mScaleCenter.x, mScaleCenter.y)
            mAnimMatrix.postTranslate(mTranslateX.toFloat(), mTranslateY.toFloat())
            executeTranslate()
        }

        private fun postExecute() {
            if (isRunning) post(this)
        }

        init {
            val ctx: Context = getContext()
            mTranslateScroller = OverScroller(ctx, mInterpolatorProxy)
            mScaleScroller = Scroller(ctx, mInterpolatorProxy)
            mFlingScroller = OverScroller(ctx, mInterpolatorProxy)
            mClipScroller = Scroller(ctx, mInterpolatorProxy)
            mRotateScroller = Scroller(ctx, mInterpolatorProxy)
        }
    }

    class RotateGestureDetector(private val mListener: OnRotateListener) {
        private var mPrevSlope = 0f
        private var mCurrSlope = 0f
        private var x1 = 0f
        private var y1 = 0f
        private var x2 = 0f
        private var y2 = 0f

        fun onTouchEvent(event: MotionEvent) {
            val Action = event.actionMasked
            when (Action) {
                MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_POINTER_UP -> if (event.pointerCount == 2) mPrevSlope =
                    caculateSlope(event)
                MotionEvent.ACTION_MOVE -> if (event.pointerCount > 1) {
                    mCurrSlope = caculateSlope(event)
                    val currDegrees =
                        Math.toDegrees(Math.atan(mCurrSlope.toDouble()))
                    val prevDegrees =
                        Math.toDegrees(Math.atan(mPrevSlope.toDouble()))
                    val deltaSlope = currDegrees - prevDegrees
                    if (Math.abs(deltaSlope) <= MAX_DEGREES_STEP) {
                        mListener.onRotate(deltaSlope.toFloat(), (x2 + x1) / 2, (y2 + y1) / 2)
                    }
                    mPrevSlope = mCurrSlope
                }
                else -> {
                }
            }
        }

        private fun caculateSlope(event: MotionEvent): Float {
            x1 = event.getX(0)
            y1 = event.getY(0)
            x2 = event.getX(1)
            y2 = event.getY(1)
            return (y2 - y1) / (x2 - x1)
        }

        interface OnRotateListener {
            fun onRotate(
                degrees: Float,
                focusX: Float,
                focusY: Float
            )
        }

        companion object {
            private const val MAX_DEGREES_STEP = 120
        }

    }

    @Parcelize
    data class Info(
        var mImgRect: RectF? = RectF(),
        var mWidgetRect: RectF? = RectF(),
        var mDegrees: Float,
        var mScaleType: String?,
        var mCropX: Float,
        var mCropY: Float,
        var transitX: Float,
        var transitY: Float,
        var mScale: Float
    ) : Parcelable {
        // 控件在窗口的位置
        val scaleType: ScaleType get() = ScaleType.valueOf(mScaleType ?: "")
    }

}