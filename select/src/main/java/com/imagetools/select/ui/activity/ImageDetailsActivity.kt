package com.imagetools.select.ui.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.core.view.drawToBitmap
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.imagetools.select.R
import com.imagetools.select.adapter.ImagePagerAdapter
import com.imagetools.select.entity.Image
import com.imagetools.select.tools.SimpleRequestListener
import com.imagetools.select.tools.Tools
import kotlinx.android.synthetic.main.activity_image_details.*

/**
 * @author jv.lee
 * @date 2020/12/14
 * @description
 */
internal class ImageDetailsActivity : BaseActivity(R.layout.activity_image_details) {

    companion object {
        const val TAG = "IMAGE_DETAILS"
        const val KEY_POSITION = "position"
        const val KEY_DATA = "data"
        const val KEY_SIZE = "size"
        const val KEY_BITMAP = "bitmapBytes"

        fun startActivity(
            activity: FragmentActivity,
            position: Int,
            view: ImageView,
            data: ArrayList<Image>,
            size: Int
        ) {
            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    view,
                    position.toString()
                )
            activity.startActivity(
                Intent(activity, ImageDetailsActivity::class.java)
                    .putExtra(KEY_SIZE, size)
                    .putExtra(KEY_DATA, data)
                    .putExtra(KEY_BITMAP, Tools.bitmap2Bytes(view.drawToBitmap()))
                    .putExtra(KEY_POSITION, position), optionsCompat.toBundle()
            )
        }

    }

    private val size by lazy { intent.getIntExtra(KEY_SIZE, 0) }

    private val position by lazy { intent.getIntExtra(KEY_POSITION, 0) }

    private val data by lazy<ArrayList<Image>> {
        intent.getParcelableArrayListExtra(KEY_DATA) ?: arrayListOf()
    }

    private val bitmap by lazy {
        val bitmapBytes = intent.getByteArrayExtra(KEY_BITMAP)
        bitmapBytes?.let {
            return@let BitmapFactory.decodeByteArray(it, 0, it.size)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //暂时阻止共享元素过渡
        supportPostponeEnterTransition()

        ViewCompat.setTransitionName(iv_holder, position.toString())
        Glide.with(iv_holder)
            .load(data[position].path)
            .override(size, size)
            .listener(object : SimpleRequestListener() {
                override fun call() {
                    //占位图加载完成后 开启共享元素共享动画
                    supportStartPostponedEnterTransition()
                }
            })
            .into(iv_holder)

        //初始化加载详情图Pager页面.
        vp_container.adapter = ImagePagerAdapter(data)
        vp_container.setCurrentItem(position, false)
        vp_container.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //view绘制完成后 隐藏占位共享元素Image
                iv_holder.visibility = View.GONE
                vp_container.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }

        })

        //设置回调共享元素通信
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                val position = vp_container.currentItem
                val view = vp_container.findViewById<View>(R.id.move_image)
                view?.run {
                    sharedElements.put(position.toString(), this)
                }
            }
        })

        //设置共享元素执行时长
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition.duration = 200
            window.sharedElementExitTransition.duration = 200
        }
    }

}