package com.imagetools.select.adapter2.base

import android.content.Context
import android.view.LayoutInflater
import android.widget.BaseAdapter
import com.imagetools.select.tools.Tools

/**
 * @author jv.lee
 * @date 2020/12/7
 * @description
 */
internal abstract class BaseAdapter2<T>(
    protected val context: Context,
    private val data: MutableList<T>
) : BaseAdapter() {

    protected val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount() = data.size

    override fun getItem(position: Int) = data[position]

    override fun getItemId(position: Int) = position.toLong()

    fun getData() = data

    fun updateData(data: List<T>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun clearData() {
        getData().clear()
        notifyDataSetChanged()
    }

}