package com.imagetools.select.ui.adapter.base

import android.content.Context
import com.imagetools.select.entity.Image
import com.imagetools.select.tools.Tools

/**
 * @author jv.lee
 * @date 2020/12/7
 * @description
 */
internal abstract class BaseSelectAdapter<T>(
    context: Context,
    data: MutableList<T>,
    val isMultiple: Boolean = false,
    val selectLimit: Int = 9,
    columnCount: Int = 4
) :
    BaseAdapter<T>(context, data) {

    val size = Tools.getImageSize(context, columnCount)
    val selectList = arrayListOf<Image>()

    protected var hasLoadMore: Boolean = true
    protected var mAutoLoadCallback: AutoLoadCallback? = null
    protected var mSelectCallback: ItemSelectCallback? = null

    interface AutoLoadCallback {
        fun loadMore()
    }

    interface ItemSelectCallback {
        fun selectItem(item: Image)
        fun selectEnd(limit: Int)
        fun selectCall(count: Int)
    }

    fun setAutoLoadCallback(autoLoadCallback: AutoLoadCallback) {
        this.mAutoLoadCallback = autoLoadCallback
    }

    fun setSelectCallback(selectCallback: ItemSelectCallback) {
        mSelectCallback = selectCallback
    }

    fun openLoadMore() {
        hasLoadMore = true
    }

    fun closeLoadMore() {
        hasLoadMore = false
    }

}