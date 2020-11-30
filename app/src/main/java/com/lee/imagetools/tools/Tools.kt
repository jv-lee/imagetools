package com.lee.imagetools.tools

import android.animation.ValueAnimator
import android.view.View

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
object Tools {

    fun viewTranslationHide(view: View) {
        view.translationY = (-view.height).toFloat()
    }

    fun selectViewTranslationAnimator(enable: Boolean, containerView: View, maskView: View) {
        val dimen = containerView.height.toFloat()
        val animator =
            if (enable) ValueAnimator.ofFloat(-dimen, 0F)
            else ValueAnimator.ofFloat(0F, -dimen)
        animator.addUpdateListener {
            maskView.alpha = (dimen - Math.abs(it.animatedValue as Float)) / dimen
            containerView.translationY = it.animatedValue as Float
        }
        animator.start()
    }
}