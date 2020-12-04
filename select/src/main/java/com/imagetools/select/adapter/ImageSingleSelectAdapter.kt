package com.imagetools.select.adapter

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.imagetools.select.R
import com.imagetools.select.entity.Image

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal class ImageSingleSelectAdapter(private val childWidth: Int) :
    SelectAdapter<Image>(arrayListOf()) {
    override fun getItemLayoutId() = R.layout.item_image_single

    override fun convert(itemView: View, item: Image, position: Int) {
        val ivImage = itemView.findViewById<ImageView>(R.id.iv_image)

        ivImage.layoutParams =
            ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                childWidth
            )
        Glide.with(itemView).load(item.path)
            .placeholder(ColorDrawable(ContextCompat.getColor(itemView.context, R.color.colorItem)))
            .dontAnimate()
            .into(ivImage)
    }

}