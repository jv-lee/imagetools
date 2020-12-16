package com.imagetools.select.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.appcompat.widget.AppCompatImageView
import com.imagetools.select.lifecycle.ViewLifecycle

/**
 * @author jv.lee
 * @date 2020/12/16
 * @description 可拖拽的ImageView
 * 当前实现为向下拖拽进入拖拽模式 ， 横向 向上不进入拖拽模式.
 */
class DragImageView constructor(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet), ViewLifecycle {

    private var startY = 0f
    private var startX = 0f

    private var mEndX = 0
    private var mEndY = 0

    /**
     * 记录HorizontalView是否拖拽的标记
     */
    private var isParentTouch = false
    private var isChildTouch = false

    private val reIndexAnimation = ReIndexAnimation()

    init {
        bindLifecycle(context)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录手指按下的位置
                startY = ev.y
                startX = ev.x
                // 初始化标记
                isParentTouch = false
                isChildTouch = false
            }
            MotionEvent.ACTION_MOVE -> {
                // 父容器为可拖动 子View为不可拖动 直接返回false 父容器消费事件
                if (isParentTouch && !isChildTouch) {
                    return false
                }
                // 获取当前手指位置
                val endY: Float = ev.y
                val endX: Float = ev.x
                val distanceX: Float = Math.abs(endX - startX)
                val distanceY: Float = Math.abs(endY - startY)
                // 当前子view不可消费事件 且为横向拖动 则返回false 子view不处理 直接返回父容器处理事件
                if (!isChildTouch && distanceX > distanceY) {
                    isParentTouch = true
                    return false
                }
                // 当前子view不可消费事件 且滑动为向上滑动 则返回false 直接返回父亲容器处理事件
                if (!isChildTouch && endY < startY) {
                    isParentTouch = true
                    return false
                }
                // 滑动为向下滑动 关闭父容器处理事件 打开子容器处理事件
                if (endY > startY) {
                    isChildTouch = true
                    isParentTouch = false
                    parent.requestDisallowInterceptTouchEvent(true)
                    return super.dispatchTouchEvent(ev)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                //复原所有属性修改 设置为基础值
                parent.requestDisallowInterceptTouchEvent(false)
                isParentTouch = false
                isChildTouch = false
            }
        }
        // 如果是Y轴位移大于X轴，事件交给swipeRefreshLayout处理。
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                //计算距离上次移动了多远
                val currX: Int = x - mEndX
                val currY: Int = y - mEndY
                //设置当前偏移量实现拖动
                this.translationX = this.translationX + currX
                this.translationY = this.translationY + currY
                if (currX != 0 && currY != 0) {
                    isClickable = false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isClickable = true
                onReIndex()
            }
        }
        mEndX = x
        mEndY = y
        return true
    }

    /**
     * 位置重置
     */
    private fun onReIndex() {
        //平移回到该view水平方向的初始点
        reIndexAnimation.setTranslationDimensions(translationX, translationY)
        reIndexAnimation.duration = 100
        startAnimation(reIndexAnimation)
    }

    private inner class ReIndexAnimation : Animation() {

        private var targetTranslationX = 0F
        private var targetTranslationY = 0F
        private var currentTranslationX = 0F
        private var currentTranslationY = 0F
        private var translationXChange = 0F
        private var translationYChange = 0F

        fun setTranslationDimensions(currentTranslationX: Float, currentTranslationY: Float) {
            this.currentTranslationX = currentTranslationX
            this.currentTranslationY = currentTranslationY
            translationXChange = targetTranslationX - currentTranslationX
            translationYChange = targetTranslationY - currentTranslationY
        }


        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolatedTime >= 1) {
                translationX = targetTranslationX
                translationY = targetTranslationY
            } else {
                val stepX = (translationXChange * interpolatedTime)
                val stepY = (translationYChange * interpolatedTime)

                translationX = currentTranslationX + stepX
                translationY = currentTranslationY + stepY
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    override fun onLifecycleCancel() {
        unBindLifecycle(context)
        clearAnimation()
    }

}