package com.imagetools.select.adapter.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal abstract class BaseAdapter<T>(val context: Context, private val data: MutableList<T>) :
    RecyclerView.Adapter<BaseAdapter<T>.SelectViewHolder>() {

    private var mItemClickListener: ItemClickListener<T>? = null

    private var mAutoLoadMoreListener: AutoLoadMoreListener? = null

    private var lastClickTime: Long = 0

    private val quickEventTimeSpan: Long = 1000

    var hasLoadMore = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectViewHolder {
        val viewHolder = SelectViewHolder(
            LayoutInflater.from(parent.context).inflate(getItemLayoutId(), parent, false)
        )
        bindItemListener(viewHolder)
        bindListener(viewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SelectViewHolder, position: Int) {
        holder.bindView(data[position], position)
        if (holder.layoutPosition == 0 || data.size < 10) return
        if (holder.layoutPosition == data.size / 2 && hasLoadMore) {
            hasLoadMore = false
            //防止更新过快导致 RecyclerView 还处于锁定状态 就直接更新数据
            val value = ValueAnimator.ofInt(0, 1)
            value.duration = 50
            value.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mAutoLoadMoreListener?.loadMore()
                }
            })
            value.start()
        }
    }

    open fun bindListener(viewHolder: SelectViewHolder) {

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

    interface AutoLoadMoreListener {
        fun loadMore()
    }

    fun setAutoLoadMoreListener(autoLoadMoreListener: AutoLoadMoreListener) {
        this.mAutoLoadMoreListener = autoLoadMoreListener
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

    fun addData(data: List<T>) {
        val startIndex = getData().size
        this.data.addAll(data)
        notifyItemRangeInserted(startIndex, data.size)
    }

    fun clearData() {
        getData().clear()
        notifyDataSetChanged()
    }

}