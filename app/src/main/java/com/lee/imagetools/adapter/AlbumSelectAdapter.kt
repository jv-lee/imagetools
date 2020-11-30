package com.lee.imagetools.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.lee.imagetools.R
import com.lee.imagetools.entity.Album

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
class AlbumSelectAdapter(data: MutableList<Album>) :
    SelectAdapter<Album>(data) {

    override fun getItemLayoutId(): Int {
        return R.layout.item_select
    }

    override fun convert(itemView: View, item: Album, position: Int) {
        val ivCover = itemView.findViewById<ImageView>(R.id.iv_cover)
        val tvAlbumName = itemView.findViewById<TextView>(R.id.tv_album_name)

        Glide.with(itemView).load(item.cover).into(ivCover)
        tvAlbumName.text = item.name
    }


}