package com.imagetools.select.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.imagetools.select.R
import com.imagetools.select.entity.Album
import com.imagetools.select.ui.adapter.base.BaseAdapter

/**
 * @author jv.lee
 * @date 2020/12/7
 * @description
 */
internal class AlbumSelectAdapter : BaseAdapter<Album>(arrayListOf()) {

    override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
        val itemView: View
        val viewHolder: ViewHolder
        if (converView == null) {
            itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_select_imagetools, parent, false)
            viewHolder = ViewHolder(
                itemView.findViewById(R.id.iv_cover),
                itemView.findViewById(R.id.tv_album_name)
            )
            itemView.tag = viewHolder
        } else {
            itemView = converView
            viewHolder = itemView.tag as ViewHolder
        }

        val item = getItem(position)
        Glide.with(parent.context).load(item.coverUri).into(viewHolder.ivCover)
        viewHolder.tvAlbumName.text = getData()[position].name ?: ""

        return itemView
    }

    class ViewHolder(val ivCover: ImageView, val tvAlbumName: TextView)

}