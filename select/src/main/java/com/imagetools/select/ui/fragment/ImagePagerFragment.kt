package com.imagetools.select.ui.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.imagetools.select.R
import com.imagetools.select.listener.SimpleRequestListener
import com.imagetools.select.ui.activity.ImageDetailsActivity
import com.imagetools.select.ui.adapter.ImagePagerAdapter
import com.imagetools.select.ui.widget.DragImageView
import kotlinx.android.synthetic.main.activity_image_details.*
import kotlinx.android.synthetic.main.layout_navigation.*

/**
 * @author jv.lee
 * @date 2020/12/12
 * @description
 */
class ImagePagerFragment : BaseFragment(R.layout.fragment_image_pager) {

    private val params by lazy<ImageDetailsActivity.Companion.ImageDetailsParams> {
        requireActivity().intent.getParcelableExtra(ImageDetailsActivity.KEY_PARAMS)!!
    }

    private val adapter by lazy {
        ImagePagerAdapter(params.data).also {
            it.setDragCallback(object : DragImageView.Callback {
                override fun onClicked() {
                    //单击事件
                    requireActivity().supportFinishAfterTransition()
                }

                override fun onDragClose() {
                    //关闭当前activity 执行共享动画关闭
                    requireActivity().supportFinishAfterTransition()
                }

                override fun changeAlpha(alpha: Float) {
                    //根据下拉修改activity透明度
                    it.setBackgroundAlphaCompat(
                        requireActivity().window.decorView,
                        (255 * alpha).toInt()
                    )
                }

            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAnimation()
        initPager()
    }

    private fun initAnimation() {
        //暂时阻止共享元素过渡
        requireActivity().supportPostponeEnterTransition()

        ViewCompat.setTransitionName(iv_holder, params.transitionName)
        Glide.with(iv_holder)
            .load(params.transitionName)
            .override(params.size, params.size)
            .listener(object : SimpleRequestListener<Drawable>() {
                override fun call() {
                    //占位图加载完成后 开启共享元素共享动画
                    requireActivity().supportStartPostponedEnterTransition()
                }
            })
            .into(iv_holder)

        //设置回调共享元素通信
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                val position = vp_container.currentItem
                val view = vp_container.findViewById<View>(R.id.drag_image)
                view?.run {
                    sharedElements.put(adapter.data[position].path, this)
                }
            }
        })

        //设置共享元素执行时长
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requireActivity().window.sharedElementEnterTransition.duration = 200
            requireActivity().window.sharedElementExitTransition.duration = 200
        }
    }

    private fun initPager() {
        //初始化加载详情图Pager页面.
        vp_container.adapter = adapter
        vp_container.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //view绘制完成后 隐藏占位共享元素Image
                iv_holder.visibility = View.GONE
                vp_container.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }

        })
        //每次切换页面动态更改回调值
        vp_container.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                parseResult()
            }
        })
        //定位到选中位置
        vp_container.setCurrentItem(params.position, false)
    }

    private fun parseResult() {
        requireActivity().setResult(
            Activity.RESULT_OK, Intent()
                .putExtra(
                    ImageDetailsActivity.KEY_IMAGE,
                    params.data[vp_container.currentItem]
                )
        )
    }

}