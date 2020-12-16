package com.imagetools.app.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.imagetools.app.R
import com.imagetools.select.widget.DragImageView

/**
 * @author jv.lee
 * @date 2020/12/16
 * @description
 */
class ImagePagerAdapter(private val data: MutableList<Int>) :
    RecyclerView.Adapter<ImagePagerAdapter.ImagePagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePagerViewHolder {
        return ImagePagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_page_image, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ImagePagerViewHolder, position: Int) {
        holder.bindView(data[position])
    }

    class ImagePagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moveImage by lazy { itemView.findViewById<DragImageView>(R.id.move_image) }
        fun bindView(resId: Int) {
            ViewCompat.setTransitionName(
                moveImage,
                moveImage.context.getString(R.string.transitionName)
            )
            moveImage.setImageResource(resId)
            moveImage.setCallback(object : DragImageView.Callback {
                override fun onClose() {
                    if ((itemView.context is FragmentActivity)) {
                        (itemView.context as FragmentActivity).supportFinishAfterTransition()
                    }
                }

                override fun changeAlpha(alpha: Float) {
                    if (itemView.context is FragmentActivity) {
                        setBackgroundAlphaCompat(
                            (itemView.context as FragmentActivity).findViewById<ConstraintLayout>(
                                R.id.const_root
                            ), (255 * alpha).toInt()
                        )
                    }
                }

            })
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