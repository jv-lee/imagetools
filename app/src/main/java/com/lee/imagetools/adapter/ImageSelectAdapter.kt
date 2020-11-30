package com.lee.imagetools.adapter

import android.view.View
import com.lee.imagetools.entity.Image

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
class ImageSelectAdapter(data: MutableList<Image>) : SelectAdapter<Image>(data){
    override fun getItemLayoutId(): Int {
        return 0
    }

    override fun convert(itemView: View, item: Image, position: Int) {

    }

}