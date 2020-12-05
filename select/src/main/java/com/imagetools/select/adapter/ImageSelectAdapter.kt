package com.imagetools.select.adapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.imagetools.select.R
import com.imagetools.select.adapter.base.BaseSelectAdapter
import com.imagetools.select.entity.Image

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal class ImageSelectAdapter(
    context: Context,
    isMultiple: Boolean = false,
    selectLimit: Int = 9,
    columnCount: Int = 4
) :
    BaseSelectAdapter<Image>(context, arrayListOf(), isMultiple, selectLimit, columnCount) {


    override fun getItemLayoutId() = R.layout.item_image

    override fun convert(itemView: View, item: Image, position: Int) {
        val ivImage = itemView.findViewById<ImageView>(R.id.iv_image)
        val ivMask = itemView.findViewById<ImageView>(R.id.iv_mask)
        val frameSelect = itemView.findViewById<FrameLayout>(R.id.frame_select)
        val tvSelectNumber = itemView.findViewById<TextView>(R.id.tv_select_number)

        item.itemIndex = position
        frameSelect.visibility = View.GONE

        ivImage.layoutParams = ConstraintLayout.LayoutParams(size, size)
        Glide.with(context).load(item.path)
            .placeholder(ColorDrawable(ContextCompat.getColor(context, R.color.colorSelect)))
            .dontAnimate()
            .override(size, size)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    ivImage.setImageDrawable(resource)
                    if (isMultiple) {
                        frameSelect.visibility = View.VISIBLE
                    }
                    return true
                }

            })
            .into(ivImage)

        if (isMultiple) {
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
    }

    override fun bindListener(viewHolder: SelectViewHolder) {
        if (!isMultiple) return
        viewHolder.itemView.findViewById<FrameLayout>(R.id.frame_select).setOnClickListener {
            mSelectCallback?.selectItem(getData()[viewHolder.layoutPosition])
        }
    }

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

}