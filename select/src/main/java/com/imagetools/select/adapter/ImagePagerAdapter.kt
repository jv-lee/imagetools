package com.imagetools.select.adapter

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.imagetools.select.R
import com.imagetools.select.entity.Image
import com.imagetools.select.widget.DragImageView

/**
 * @author jv.lee
 * @date 2020/12/16
 * @description
 */
internal class ImagePagerAdapter(
    private val data: MutableList<Image>,
    val toPosition: Int,
    var bitmap: Bitmap?
) :
    RecyclerView.Adapter<ImagePagerAdapter.ImagePagerViewHolder>() {

    private var firstLoad = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePagerViewHolder {
        return ImagePagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_page_image, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ImagePagerViewHolder, position: Int) {
        holder.bindView(data[position], position)
    }

    internal inner class ImagePagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moveImage by lazy { itemView.findViewById<DragImageView>(R.id.move_image) }
        fun bindView(item: Image, position: Int) {
            //预加载
            if (firstLoad && position == toPosition) {
                firstLoad = false
                moveImage.setImageBitmap(bitmap)
            }
            Glide.with(moveImage).load(item.path).listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    bitmap?.recycle()
                    bitmap = null
                    (itemView.context as FragmentActivity).supportStartPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    bitmap?.recycle()
                    bitmap = null
                    (itemView.context as FragmentActivity).supportStartPostponedEnterTransition()
                    return false
                }
            }).into(moveImage)
            moveImage.setCallback(object : DragImageView.Callback {
                override fun onClose() {
                    if ((itemView.context is FragmentActivity)) {
                        //设置选中坐标 修改回调时共享元素坐标
                        (itemView.context as FragmentActivity).setResult(
                            RESULT_OK,
                            Intent().putExtra("position", position)
                        )
                        (itemView.context as FragmentActivity).supportFinishAfterTransition()
                    }
                }

                override fun changeAlpha(alpha: Float) {
                    if (itemView.context is FragmentActivity) {
                        setBackgroundAlphaCompat(
                            (itemView.context as FragmentActivity).window.decorView,
                            (255 * alpha).toInt()
                        )
                    }
                }

            })

//            ViewCompat.setTransitionName(moveImage, position.toString())
        }

        fun setBackgroundAlphaCompat(view: View?, alpha: Int) {
            view ?: return
            val mutate = view.background.mutate()
            if (mutate != null) {
                mutate.alpha = alpha
            } else {
                view.background.alpha = alpha
            }
        }
    }
}