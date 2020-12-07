package com.imagetools.select.adapter2.base

import android.content.Context
import com.imagetools.select.entity.Image
import com.imagetools.select.tools.Tools

/**
 * @author jv.lee
 * @date 2020/12/7
 * @description
 */
internal abstract class BaseSelectAdapter2<T>(
    context: Context,
    data: MutableList<T>,
    val isMultiple: Boolean = false,
    val selectLimit: Int = 9,
    columnCount: Int = 4
) :
    BaseAdapter2<T>(context, data) {

    val size = Tools.getImageSize(context, columnCount)
    val selectList = arrayListOf<Image>()

    protected var mSelectCallback: MultipleSelectCallback? = null

    interface MultipleSelectCallback {
        fun selectItem(item: Image)
        fun selectEnd(limit: Int)
        fun selectCall(count: Int)
    }

    fun setSelectCallback(selectCallback: MultipleSelectCallback) {
        mSelectCallback = selectCallback
    }

}