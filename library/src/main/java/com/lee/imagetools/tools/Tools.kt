package com.lee.imagetools.tools

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import com.lee.imagetools.R

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal object Tools {

    fun viewTranslationHide(view: View) {
        view.translationY = (-view.height).toFloat()
    }

    fun selectViewTranslationAnimator(
        enable: Boolean,
        containerView: View,
        maskView: View
    ): ValueAnimator {
        val dimen = containerView.height.toFloat()
        val animator =
            if (enable) ValueAnimator.ofFloat(-dimen, 0F)
            else ValueAnimator.ofFloat(0F, -dimen)
        animator.addUpdateListener {
            maskView.alpha = (dimen - Math.abs(it.animatedValue as Float)) / dimen
            containerView.translationY = it.animatedValue as Float
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!enable) maskView.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                if (enable) maskView.visibility = View.VISIBLE
            }

        })
        animator.start()
        return animator
    }

    fun getItemOrderAnimator(context: Context): LayoutAnimationController {
        val animController =
            LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.item_alpha_in))
        animController.order = LayoutAnimationController.ORDER_NORMAL
        animController.delay = 0.2f
        return animController
    }

    fun bindBottomFinishing(activity: Activity) {
        if (activity.isFinishing) activity.overridePendingTransition(
            R.anim.default_in_out,
            R.anim.slide_bottom_out
        )
    }

    fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        val widthDp = px2dp(context, point.x)
        return dp2px(
            context,
            (widthDp - (context.resources.getDimension(R.dimen.item_padding) * 4)).toInt()
        ).toInt()
    }

    /**
     * dp转px
     *
     * @param context 上下文
     * @param dpValue dp值
     * @return px值
     */
    fun dp2px(context: Context, dpValue: Int): Float {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f)
    }

    /**
     * px转dp
     *
     * @param context 上下文
     * @param pxValue px值
     * @return dp值
     */
    fun px2dp(context: Context, pxValue: Int): Float {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f)
    }

    /**
     * sp转px
     *
     * @param context 上下文
     * @param spValue sp值
     * @return px值
     */
    fun sp2px(context: Context, spValue: Int): Float {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f)
    }
}