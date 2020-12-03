package com.lee.imagetools.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.lee.imagetools.R
import com.lee.imagetools.entity.Image
import com.lee.imagetools.tools.Tools

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal class ImageMultipleSelectAdapter(private val childWidth:Int,private val selectLimit: Int) :
    SelectAdapter<Image>(arrayListOf()) {

    private val selectList = arrayListOf<Image>()
    private var mSelectCallback: SelectCallback? = null

    override fun getItemLayoutId() = R.layout.item_image_multiple

    override fun convert(itemView: View, item: Image, position: Int) {
        val ivImage = itemView.findViewById<ImageView>(R.id.iv_image)
        val ivMask = itemView.findViewById<ImageView>(R.id.iv_mask)
        val tvSelectNumber = itemView.findViewById<TextView>(R.id.tv_select_number)

        item.itemIndex = position

        ivImage.layoutParams =
            ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, childWidth
            )
        Glide.with(itemView).load(item.path).into(ivImage)

        if (item.select) {
            ivMask.visibility = View.VISIBLE
            tvSelectNumber.text = selectList.indexOf(item).plus(1).toString()
            tvSelectNumber.setBackgroundResource(R.drawable.shape_select_number_press)
        } else {
            ivMask.visibility = View.GONE
            tvSelectNumber.text = ""
            tvSelectNumber.setBackgroundResource(R.drawable.shape_select_number_normal)
        }
    }

    fun getSelectList() = selectList

    fun updateSelected(item: Image) {
        if (!item.select && selectList.size == selectLimit) {
            mSelectCallback?.selectEnd(selectLimit)
            return
        }

        if (item.select) {
            item.select = false
            selectList.remove(item)
            notifyItemChanged(item.itemIndex)
        } else {
            item.select = true
            selectList.add(item)
        }

        for (image in selectList) {
            notifyItemChanged(image.itemIndex)
        }
        mSelectCallback?.selectCall(selectList.size)
    }

    interface SelectCallback {
        fun selectEnd(limit: Int)
        fun selectCall(count: Int)
    }

    fun setSelectCallback(selectCallback: SelectCallback) {
        mSelectCallback = selectCallback
    }

}