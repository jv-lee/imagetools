package com.imagetools.select.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import com.imagetools.select.tools.Tools
import com.imagetools.select.tools.Tools.getStatusBarHeight

/**
 * @author jv.lee
 * @date 2020/12/29
 * @description
 */
class StatusPaddingFrameLayout constructor(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    private val statusPadding: Int = getStatusBarHeight(context)

    init {
        setPadding(0, statusPadding, 0, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            ViewGroup.LayoutParams.MATCH_PARENT,
            Tools.dp2px(context, 56).toInt() + statusPadding
        )
    }

}