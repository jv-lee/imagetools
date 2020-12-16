package com.imagetools.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import com.imagetools.app.adapter.ImagePagerAdapter
import com.imagetools.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_image.*
import kotlinx.android.synthetic.main.activity_image_details.*

/**
 * @author jv.lee
 * @date 2020/12/14
 * @description
 */
class ImageDetailsActivity : BaseActivity(R.layout.activity_image_details) {

    companion object {
        const val TAG = "IMAGE_DETAILS"
    }

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

        vp_container.adapter = ImagePagerAdapter(
            arrayListOf(
                R.drawable.one,
                R.drawable.two,
                R.drawable.thre
            )
        )
        vp_container.setCurrentItem(intent.getIntExtra("position", 0), false)


        //设置回调共享元素通信
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                sharedElements.put(
                    vp_container.currentItem.toString(),
                    vp_container.findViewById(R.id.move_image)
                )
            }
        })
    }

}