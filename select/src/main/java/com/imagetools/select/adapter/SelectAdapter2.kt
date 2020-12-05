package com.imagetools.select.adapter

import android.content.Context
import android.view.LayoutInflater
import android.widget.BaseAdapter
import com.imagetools.select.tools.Tools

/**
 * @author jv.lee
 * @date 2020/12/5
 * @description
 */
internal abstract class SelectAdapter2<T>(
    protected val context: Context,
    private val data: MutableList<T>
) : BaseAdapter() {

    protected val size: Int = Tools.getImageSize(context) / 4

    protected val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount() = data.size

    override fun getItem(position: Int) = data[position]

    override fun getItemId(position: Int) = position.toLong()

    fun getData() = data

}