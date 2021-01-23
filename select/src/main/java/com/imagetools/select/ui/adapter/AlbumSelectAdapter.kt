package com.imagetools.select.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.imagetools.select.R
import com.imagetools.select.ui.adapter.base.BaseAdapter
import com.imagetools.select.entity.Album

/**
 * @author jv.lee
 * @date 2020/12/7
 * @description
 */
internal class AlbumSelectAdapter(context: Context) : BaseAdapter<Album>(context, arrayListOf()) {

    override fun getView(position: Int, converView: View?, parent: ViewGroup?): View {
        val itemView: View
        val viewHolder: ViewHolder
        if (converView == null) {
            itemView = layoutInflater.inflate(R.layout.item_select_imagetools, parent, false)
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
        Glide.with(context).load(item.cover).into(viewHolder.ivCover)
        viewHolder.tvAlbumName.text = getData()[position].name

        return itemView
    }

    class ViewHolder(val ivCover: ImageView, val tvAlbumName: TextView)

}