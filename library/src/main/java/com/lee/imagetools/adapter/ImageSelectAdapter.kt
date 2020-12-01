package com.lee.imagetools.adapter

import android.view.View
import android.widget.ImageView
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
class ImageSelectAdapter(data: MutableList<Image>) : SelectAdapter<Image>(data) {
    override fun getItemLayoutId() = R.layout.item_image

    override fun convert(itemView: View, item: Image, position: Int) {
        val ivImage = itemView.findViewById<ImageView>(R.id.iv_image)

        val screenWidth = Tools.getScreenWidth(itemView.context)
        ivImage.layoutParams =
            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, screenWidth / 4)
        Glide.with(itemView).load(item.path).into(ivImage)
    }

}