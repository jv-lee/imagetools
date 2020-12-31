package com.imagetools.select.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * @author jv.lee
 * @date 2020/12/17
 * @description 取消设置透明度的ImageView .
 */
class NoAlphaImageView(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet) {

    override fun setAlpha(alpha: Float) {
    }

}