package com.imagetools.select.ui.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.imagetools.select.R
import com.imagetools.select.lifecycle.ViewLifecycle
import com.imagetools.select.tools.Tools
import com.imagetools.select.tools.Tools.getStatusBarHeight

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal class ImageSelectBar constructor(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet),
    ViewLifecycle {

    private var linearSelect: LinearLayout
    private var ivIcon: ImageView
    private var tvAlbumName: TextView
    private var expansion = false
    private var switchTag = false
    private var mAnimCallback: AnimCallback? = null
    private val animatorSet = HashSet<ValueAnimator>()

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_image_select_bar_imagetools, this, true)
        setPadding(0, getStatusBarHeight(context), 0, 0)
        findViewById<ImageView>(R.id.iv_close).setOnClickListener { (context as? Activity)?.finish() }
        tvAlbumName = findViewById(R.id.tv_album_name)
        ivIcon = findViewById(R.id.iv_icon)
        linearSelect = findViewById(R.id.linear_select)
        linearSelect.setOnClickListener { switch() }

        bindLifecycle(context)
    }

    fun isExpansion() = expansion

    fun setSelectName(text: String) {
        tvAlbumName.text = text
        val rect = Rect()
        tvAlbumName.paint.getTextBounds(text, 0, text.length, rect)

        val animator =
            ValueAnimator.ofInt(tvAlbumName.width, rect.width() + Tools.dp2px(context, 6).toInt())
        animatorSet.add(animator)
        animator.duration = 200
        animator.addUpdateListener {
            tvAlbumName.layoutParams =
                LinearLayout.LayoutParams(it.animatedValue as Int, tvAlbumName.height)
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                mAnimCallback?.animEnd()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                tvAlbumName.text = text
            }

        })
        animator.start()
    }

    fun switch() {
        if (switchTag) return
        val animator =
            if (expansion) ValueAnimator.ofFloat(180f, 360f) else ValueAnimator.ofFloat(0f, 180f)
        animatorSet.add(animator)
        animator.duration = 200
        animator.addUpdateListener {
            ivIcon.rotation = it.animatedValue as Float
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                switchTag = false
                linearSelect.isClickable = true
            }

            override fun onAnimationCancel(animation: Animator?) {
                ivIcon.rotation = if (expansion) 180f else 0f
                linearSelect.isClickable = true
            }

            override fun onAnimationStart(animation: Animator?) {
                switchTag = true
                expansion = !expansion
                mAnimCallback?.animCall(expansion)
                linearSelect.isClickable = false
            }

        })
        animator.start()
    }

    interface AnimCallback {
        fun animEnd()
        fun animCall(enable: Boolean)
    }

    fun setAnimCallback(animatorCallback: AnimCallback) {
        this.mAnimCallback = animatorCallback
    }

    override fun onLifecycleCancel() {
        unBindLifecycle(context)
        for (valueAnimator in animatorSet) {
            valueAnimator.cancel()
        }
        animatorSet.clear()
    }

}