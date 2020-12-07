package com.imagetools.select.adapter.base

import android.content.Context
import android.view.LayoutInflater
import android.widget.BaseAdapter

/**
 * @author jv.lee
 * @date 2020/12/7
 * @description
 */
internal abstract class BaseAdapter<T>(
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
    }

    fun addData(data: List<T>) {
        this.data.addAll(data)
    }

    fun clearData() {
        getData().clear()
        notifyDataSetChanged()
    }

}