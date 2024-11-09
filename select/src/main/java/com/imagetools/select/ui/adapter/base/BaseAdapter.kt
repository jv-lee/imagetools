package com.imagetools.select.ui.adapter.base

import android.widget.BaseAdapter

/**
 * @author jv.lee
 * @date 2020/12/7
 * @description
 */
internal abstract class BaseAdapter<T>(private val data: MutableList<T>) : BaseAdapter() {

    override fun getCount() = data.size

    override fun getItem(position: Int) = data[position]

    override fun getItemId(position: Int) = position.toLong()

    fun getData() = data

    fun addData(data: List<T>) {
        this.data.addAll(data)
    }

    fun clearData() {
        getData().clear()
    }

}