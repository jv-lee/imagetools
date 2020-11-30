package com.lee.imagetools.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.lee.imagetools.R

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
class ImageSelectBar constructor(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    private var constSelect: ConstraintLayout
    private var ivIcon: ImageView
    private var enable = false
    private var switchTag = false
    private var mAnimCallback: AnimCallback? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_image_select_bar, this, true)
        findViewById<ImageView>(R.id.iv_close).setOnClickListener { (context as Activity).finish() }
        ivIcon = findViewById(R.id.iv_icon)
        constSelect = findViewById(R.id.const_select)
        constSelect.setOnClickListener { switch() }
    }

    fun getEnable() = enable

    fun switch() {
        if (switchTag) return
        val animator =
            if (enable) ValueAnimator.ofFloat(180f, 360f) else ValueAnimator.ofFloat(0f, 180f)
        animator.addUpdateListener {
            ivIcon.rotation = it.animatedValue as Float
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                switchTag = false
                constSelect.isClickable = true
            }

            override fun onAnimationCancel(animation: Animator?) {
                ivIcon.rotation = if (enable) 180f else 0f
                constSelect.isClickable = true
            }

            override fun onAnimationStart(animation: Animator?) {
                switchTag = true
                enable = !enable
                mAnimCallback?.animCall(enable)
                constSelect.isClickable = false
            }

        })
        animator.start()
    }

    interface AnimCallback {
        fun animCall(enable: Boolean)
    }

    fun setAnimCallback(animatorCallback: AnimCallback) {
        this.mAnimCallback = animatorCallback
    }

}