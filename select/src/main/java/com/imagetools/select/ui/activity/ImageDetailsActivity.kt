package com.imagetools.select.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.FragmentActivity
import com.imagetools.select.R
import com.imagetools.select.adapter.ImagePagerAdapter
import com.imagetools.select.entity.Image
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

        fun startActivity(
            activity: FragmentActivity,
            position: Int,
            view: View,
            data: ArrayList<Image>
        ) {
            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    view,
                    position.toString()
                )
            activity.startActivity(
                Intent(activity, ImageDetailsActivity::class.java)
                    .putExtra(KEY_POSITION, position)
                    .putExtra(KEY_DATA, data), optionsCompat.toBundle()
            )
        }

    }

    //    private val data by lazy { intent.getParcelableArrayListExtra(KEY_DATA)?: arrayListOf() }
    private val position by lazy { intent.getIntExtra(KEY_POSITION, 0) }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //暂时阻止共享元素过渡
        supportPostponeEnterTransition()

        vp_container.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                vp_container.viewTreeObserver.removeOnGlobalLayoutListener(this)
                //view绘制完成后 打开共享元素 达到更平滑的动画效果.
                supportStartPostponedEnterTransition()
            }

        })

        vp_container.adapter =
            ImagePagerAdapter(intent.getParcelableArrayListExtra(KEY_DATA) ?: arrayListOf())
        vp_container.setCurrentItem(position, false)

        //设置回调共享元素通信
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                val position = vp_container.currentItem
                sharedElements.put(position.toString(), vp_container.findViewById(R.id.move_image))
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition.duration = 200
            window.sharedElementExitTransition.duration = 200
        }
    }

}