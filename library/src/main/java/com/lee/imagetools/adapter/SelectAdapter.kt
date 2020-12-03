package com.lee.imagetools.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal abstract class SelectAdapter<T>(private val data: MutableList<T>) :
    RecyclerView.Adapter<SelectAdapter<T>.SelectViewHolder>() {

    private var mItemClickListener: ItemClickListener<T>? = null

    private var lastClickTime: Long = 0

    private val quickEventTimeSpan: Long = 1000

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectViewHolder {
        val viewHolder = SelectViewHolder(
            LayoutInflater.from(parent.context).inflate(getItemLayoutId(), parent, false)
        )
        bindItemListener(viewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SelectViewHolder, position: Int) {
        holder.bindView(data[position], position)
    }

    private fun bindItemListener(viewHolder: SelectViewHolder) {
        viewHolder.itemView.setOnClickListener {
            //设置防抖控制
            val timeSpan = System.currentTimeMillis() - lastClickTime
            if (timeSpan < quickEventTimeSpan) {
                return@setOnClickListener
            }
            lastClickTime = System.currentTimeMillis()
            mItemClickListener?.onClickItem(
                viewHolder.layoutPosition,
                data[viewHolder.layoutPosition]
            )
        }
    }

    abstract fun getItemLayoutId(): Int

    abstract fun convert(itemView: View, item: T, position: Int)

    inner class SelectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(item: T, position: Int) {
            convert(itemView, item, position)
        }
    }

    interface ItemClickListener<T> {
        fun onClickItem(position: Int, item: T)
    }

    fun setOnItemClickListener(onItemClickListener: ItemClickListener<T>) {
        this.mItemClickListener = onItemClickListener
    }

    fun getData() = data

    fun updateData(data: List<T>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

}