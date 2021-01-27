package com.imagetools.select.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.transition.addListener
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.imagetools.select.R
import com.imagetools.select.entity.Image
import com.imagetools.select.event.ImageEventBus
import com.imagetools.select.listener.SimpleRequestListener
import com.imagetools.select.ui.adapter.ImagePagerAdapter
import com.imagetools.select.ui.widget.DragImageView
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_image_details_imagetools.*
import kotlinx.android.synthetic.main.layout_edit_top_imagetools.*
import kotlinx.android.synthetic.main.layout_navigation_imagetools.*

/**
 * @author jv.lee
 * @date 2020/12/14
 * @description 图片详情pager页面
 */
internal class ImageDetailsActivity : BaseActivity(R.layout.activity_image_details_imagetools) {

    companion object {
        const val TAG = "ImageDetailsActivity"
        const val KEY_IS_ORIGINAL = "original"
        const val KEY_IMAGE = "image"
        const val KEY_PARAMS = "params"

        @Parcelize
        data class ImageDetailsParams(
            val transitionName: String = "",
            val position: Int,
            val size: Int,
            val isReview: Boolean = false,
            val isOriginal: Boolean = false,
            val selectLimit: Int = 9,
            val data: ArrayList<Image>,
            val selectData: ArrayList<Image>
        ) : Parcelable

        fun startActivity(
            activity: FragmentActivity,
            view: View,
            transitionName: String,
            position: Int,
            size: Int,
            isReview: Boolean = false,
            isOriginal: Boolean = false,
            selectLimit: Int = 9,
            data: ArrayList<Image>,
            selectData: ArrayList<Image>
        ) {
            val params = ImageDetailsParams(
                transitionName,
                position,
                size,
                isReview,
                isOriginal,
                selectLimit,
                data,
                selectData
            )
            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    view,
                    params.transitionName
                )
            activity.startActivity(
                Intent(activity, ImageDetailsActivity::class.java)
                    .putExtra(KEY_PARAMS, params), optionsCompat.toBundle()
            )
        }

    }

    private val params by lazy<ImageDetailsParams> { intent.getParcelableExtra(KEY_PARAMS)!! }

    private val adapter by lazy {
        ImagePagerAdapter(params.data).also {
            it.setDragCallback(object : DragImageView.Callback {
                override fun onClicked() {
                    //单击事件
                    switchEditLayoutVisible()
                }

                override fun onDragClose() {
                    //关闭当前activity 执行共享动画关闭
                    supportFinishAfterTransition()
                }

                override fun changeAlpha(alpha: Float) {
                    //根据下拉修改activity透明度
                    it.setBackgroundAlphaCompat(window.decorView, (255 * alpha).toInt())
                    setEditLayoutVisible(alpha == 1.0F)
                }

            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAnimation()
        initPager()
        initEditLayout()
    }

    private fun initAnimation() {
        //暂时阻止共享元素过渡
        supportPostponeEnterTransition()

        ViewCompat.setTransitionName(iv_holder, params.transitionName)
        Glide.with(iv_holder)
            .load(params.transitionName)
            .format(DecodeFormat.PREFER_RGB_565)
            .override(params.size, params.size)
            .listener(object : SimpleRequestListener<Drawable>() {
                override fun call() {
                    //占位图加载完成后 开启共享元素共享动画
                    supportStartPostponedEnterTransition()
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
            window.sharedElementEnterTransition.duration = 200
            window.sharedElementExitTransition.duration = 200
            window.sharedElementEnterTransition.addListener(
                onEnd = {
                    iv_holder.postDelayed({
                        iv_holder.visibility = View.GONE
                        Glide.with(iv_holder).clear(iv_holder)
                    }, 10)
                })
        } else {
            iv_holder.visibility = View.GONE
            Glide.with(iv_holder).clear(iv_holder)
        }
    }

    private fun initPager() {
        //初始化加载详情图Pager页面.
        vp_container.adapter = adapter
        //每次切换页面动态更改回调值
        vp_container.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setSelectView(position)
                parseResult()
            }
        })
        //是否是预览模式 设置页面position
        if (!params.isReview) vp_container.setCurrentItem(params.position, false)
    }

    private fun initEditLayout() {
        tv_review.text = getString(R.string.edit_text)
        tv_review.visibility = View.GONE
        cb_original.isChecked = params.isOriginal

        cb_original.setOnCheckedChangeListener { buttonView, isChecked ->
            parseResult()
        }
        tv_done.setOnClickListener {
            finishImageData()
        }
        iv_back.setOnClickListener { supportFinishAfterTransition() }
        frame_select.setOnClickListener { clickSelect() }
        setDoneCount()
    }

    private fun switchEditLayoutVisible() {
        const_navigation.visibility =
            if (const_navigation.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        const_navigation_top.visibility =
            if (const_navigation_top.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun setEditLayoutVisible(visible: Boolean) {
        const_navigation.visibility = if (visible) View.VISIBLE else View.GONE
        const_navigation_top.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun clickSelect() {
        //检验最大选择数
        if (params.selectData.size >= params.selectLimit) {
            toast(getString(R.string.select_limit_description, params.selectLimit))
            return
        }
        //设置选择
        val position = vp_container.currentItem
        val item = params.data[position]
        //发送事件通知上层页面刷新
        ImageEventBus.getInstance().eventLiveData.value =
            ImageEventBus.ImageEvent(item, item.select)
        if (item.select) {
            params.selectData.remove(item)
            item.select = false
        } else {
            item.select = true
            params.selectData.add(item)
        }
        setSelectView(position)
    }

    private fun setSelectView(position: Int) {
        val item = params.data[position]
        if (params.selectData.contains(item)) {
            val index = params.selectData.indexOf(item)
            tv_select_number.text = index.plus(1).toString()
            tv_select_number.visibility = View.VISIBLE
            iv_check.visibility = View.GONE
        } else {
            tv_select_number.visibility = View.GONE
            iv_check.visibility = View.VISIBLE
        }
        setDoneCount()
    }

    private fun setDoneCount() {
        if (params.selectData.isEmpty()) {
            tv_done.setText(R.string.done_text)
        } else {
            tv_done.text = getString(R.string.done_format_text, params.selectData.size)
        }
    }

    private fun finishImageData() {
        if (params.selectData.isEmpty()) {
            val image = params.data[vp_container.currentItem]
            ImageEventBus.getInstance().eventLiveData.value = ImageEventBus.ImageEvent(image, false)
        }

        finish()
        ImageEventBus.getInstance().finishLiveData.value = true
    }

    private fun parseResult() {
        setResult(
            Activity.RESULT_OK, Intent()
                .putExtra(KEY_IMAGE, params.data[vp_container.currentItem])
                .putExtra(KEY_IS_ORIGINAL, cb_original.isChecked)

        )
    }

}