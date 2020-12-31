package com.imagetools.select.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.imagetools.select.R
import com.imagetools.select.adapter.ImagePagerAdapter
import com.imagetools.select.entity.Image
import com.imagetools.select.event.ImageEventBus
import com.imagetools.select.listener.SimpleRequestListener
import com.imagetools.select.widget.DragImageView
import kotlinx.android.synthetic.main.activity_image_details.*
import kotlinx.android.synthetic.main.layout_edit_top.*
import kotlinx.android.synthetic.main.layout_navigation.*

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
        const val KEY_SELECT_DATA = "select_data"
        const val KEY_SIZE = "size"
        const val KEY_IS_REVIEW = "review"
        const val KEY_IS_ORIGINAL = "original"
        const val KEY_TRANSITION_NAME = "transitionName"
        const val KEY_IMAGE = "image"

        fun startActivity(
            activity: FragmentActivity,
            transitionName: String,
            position: Int,
            view: View,
            data: ArrayList<Image>,
            selectData: ArrayList<Image>,
            size: Int,
            isReview: Boolean = false,
            isOriginal: Boolean = false
        ) {
            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, transitionName)
            activity.startActivity(
                Intent(activity, ImageDetailsActivity::class.java)
                    .putExtra(KEY_SIZE, size)
                    .putExtra(KEY_DATA, data)
                    .putExtra(KEY_SELECT_DATA, selectData)
                    .putExtra(KEY_IS_REVIEW, isReview)
                    .putExtra(KEY_IS_ORIGINAL, isOriginal)
                    .putExtra(KEY_TRANSITION_NAME, transitionName)
                    .putExtra(KEY_POSITION, position), optionsCompat.toBundle()
            )
        }

    }

    private val transitionName by lazy { intent.getStringExtra(KEY_TRANSITION_NAME) ?: "" }

    private val isReview by lazy { intent.getBooleanExtra(KEY_IS_REVIEW, false) }

    private val isOriginal by lazy { intent.getBooleanExtra(KEY_IS_ORIGINAL, false) }

    private val size by lazy { intent.getIntExtra(KEY_SIZE, 0) }

    private val position by lazy { intent.getIntExtra(KEY_POSITION, 0) }

    private val data by lazy<ArrayList<Image>> {
        intent.getParcelableArrayListExtra(KEY_DATA) ?: arrayListOf()
    }

    private val selectData by lazy<ArrayList<Image>> {
        intent.getParcelableArrayListExtra(KEY_SELECT_DATA) ?: arrayListOf()
    }

    private val adapter by lazy {
        ImagePagerAdapter(data).also {
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

        ViewCompat.setTransitionName(iv_holder, transitionName)
        Glide.with(iv_holder)
            .load(transitionName)
            .override(size, size)
            .listener(object : SimpleRequestListener() {
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
                setSelectView(position)
                parseResult()
            }
        })
        //是否是预览模式 设置页面position
        if (!isReview) vp_container.setCurrentItem(position, false)
    }

    private fun initEditLayout() {
        tv_review.text = getString(R.string.edit_text)
        tv_review.visibility = View.GONE
        cb_original.isChecked = isOriginal

        cb_original.setOnCheckedChangeListener { buttonView, isChecked ->
            parseResult()
        }
        iv_back.setOnClickListener { supportFinishAfterTransition() }
        frame_select.setOnClickListener { clickSelect() }
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
        val position = vp_container.currentItem
        val item = data[position]
        //发送事件通知上层页面刷新
        ImageEventBus.getInstance().eventLiveData.value =
            ImageEventBus.ImageEvent(item, item.select)
        if (item.select) {
            selectData.remove(item)
            item.select = false
        } else {
            item.select = true
            selectData.add(item)
        }
        setSelectView(position)
    }

    private fun setSelectView(position: Int) {
        val item = data[position]
        if (selectData.contains(item)) {
            val index = selectData.indexOf(item)
            tv_select_number.text = index.plus(1).toString()
            tv_select_number.visibility = View.VISIBLE
            iv_check.visibility = View.GONE
        } else {
            tv_select_number.visibility = View.GONE
            iv_check.visibility = View.VISIBLE
        }
    }

    private fun parseResult() {
        setResult(
            Activity.RESULT_OK, Intent()
                .putExtra(KEY_IMAGE, data[vp_container.currentItem])
                .putExtra(KEY_IS_ORIGINAL, cb_original.isChecked)

        )
    }

}