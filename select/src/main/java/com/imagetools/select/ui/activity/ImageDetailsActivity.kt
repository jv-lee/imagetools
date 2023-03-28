package com.imagetools.select.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.imagetools.select.R
import com.imagetools.select.entity.Image
import com.imagetools.select.event.ImageEventBus
import com.imagetools.select.tools.SharedElementTools.onStopClearInstanceState
import com.imagetools.select.tools.WeakDataHolder
import com.imagetools.select.ui.adapter.ImagePagerAdapter
import com.imagetools.select.ui.widget.DragImageView
import com.imagetools.select.ui.widget.StatusPaddingFrameLayout
import kotlinx.parcelize.Parcelize

/**
 * @author jv.lee
 * @date 2020/12/14
 * @description 图片详情pager页面
 */
internal class ImageDetailsActivity : BaseActivity(R.layout.activity_image_details_imagetools) {

    companion object {
        const val KEY_IS_ORIGINAL = "original"
        const val KEY_IMAGE = "image"
        const val KEY_PARAMS = "params"
        const val KEY_DATA = "data"

        @Parcelize
        data class ImageDetailsParams(
            val transitionName: String = "",
            val position: Int,
            val size: Int,
            val isReview: Boolean = false,
            val isOriginal: Boolean = false,
            val selectLimit: Int = 9,
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
                selectData
            )
            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    view,
                    params.transitionName
                )
            // api 19 使用binder传输 intent.putExtra bundle.putBinder 可突破intent1M限制
            WeakDataHolder.instance.saveData(KEY_DATA, data)
            activity.startActivity(
                Intent(activity, ImageDetailsActivity::class.java)
                    .putExtra(KEY_PARAMS, params), optionsCompat.toBundle()
            )
        }

    }

    private val params by lazy<ImageDetailsParams> { intent.getParcelableExtra(KEY_PARAMS)!! }
    private val data by lazy { WeakDataHolder.instance.getData(KEY_DATA) ?: arrayListOf<Image>() }

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
                    it.setBackgroundAlphaCompat(constRoot, (255 * alpha).toInt())
                    setEditLayoutVisible(alpha == 1.0F)
                }

            })
        }
    }

    private val constRoot: ConstraintLayout by lazy { findViewById(R.id.const_root) }
    private val vpContainer: ViewPager2 by lazy { findViewById(R.id.vp_container) }
    private val cbOriginal: CheckBox by lazy { findViewById(R.id.cb_original) }
    private val tvReview: TextView by lazy { findViewById(R.id.tv_review) }
    private val tvDone: TextView by lazy { findViewById(R.id.tv_done) }
    private val ivBack: ImageView by lazy { findViewById(R.id.iv_back) }
    private val frameSelect: FrameLayout by lazy { findViewById(R.id.frame_select) }
    private val constNavigation: ConstraintLayout by lazy { findViewById(R.id.const_navigation) }
    private val constNavigationTop: StatusPaddingFrameLayout by lazy { findViewById(R.id.const_navigation_top) }
    private val tvSelectNumber: TextView by lazy { findViewById(R.id.tv_select_number) }
    private val ivCheck: ImageView by lazy { findViewById(R.id.iv_check) }

    private val shareCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(
            names: MutableList<String>,
            sharedElements: MutableMap<String, View>
        ) {
            val position = vpContainer.currentItem
            val view = vpContainer.findViewById<View>(R.id.drag_image)
            view?.run {
                sharedElements.put(adapter.data[position].uri.path ?: "", this)
            }
        }
    }

    private val pageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            setSelectView(position)
            parseResult()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAnimation()
        initPager()
        initEditLayout()
    }

    private fun initAnimation() {
//        //暂时阻止共享元素过渡
//        supportPostponeEnterTransition()
//        //占位图加载完成后 开启共享元素共享动画
//        supportStartPostponedEnterTransition()


        //设置回调共享元素通信
        setEnterSharedElementCallback(shareCallback)

        //设置共享元素执行时长
        window.sharedElementEnterTransition.duration = 200
        window.sharedElementExitTransition.duration = 200
    }

    private fun initPager() {
        //初始化加载详情图Pager页面.
        vpContainer.adapter = adapter
        //每次切换页面动态更改回调值
        vpContainer.registerOnPageChangeCallback(pageCallback)

        //是否是预览模式 设置页面position
        if (!params.isReview) vpContainer.setCurrentItem(params.position, false)
    }

    private fun initEditLayout() {
        tvReview.text = getString(R.string.edit_text)
        tvReview.visibility = View.GONE
        cbOriginal.isChecked = params.isOriginal

        cbOriginal.setOnCheckedChangeListener { _, _ ->
            parseResult()
        }
        tvDone.setOnClickListener {
            finishImageData()
        }
        ivBack.setOnClickListener { supportFinishAfterTransition() }
        frameSelect.setOnClickListener { clickSelect() }
        setDoneCount()
    }

    private fun switchEditLayoutVisible() {
        constNavigation.visibility =
            if (constNavigation.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        constNavigationTop.visibility =
            if (constNavigationTop.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun setEditLayoutVisible(visible: Boolean) {
        constNavigation.visibility = if (visible) View.VISIBLE else View.GONE
        constNavigationTop.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun clickSelect() {
        //检验最大选择数
        if (params.selectData.size >= params.selectLimit) {
            toast(getString(R.string.select_limit_description, params.selectLimit))
            return
        }
        //设置选择
        val position = vpContainer.currentItem
        val item = data[position]
        if (item.select) {
            params.selectData.remove(item)
            item.select = false
        } else {
            item.select = true
            params.selectData.add(item)
        }
        //发送事件通知上层页面刷新
        ImageEventBus.getInstance().eventLiveData.value =
            ImageEventBus.ImageEvent(item, item.select)
        setSelectView(position)
    }

    private fun setSelectView(position: Int) {
        val item = data[position]
        if (params.selectData.contains(item)) {
            val index = params.selectData.indexOf(item)
            tvSelectNumber.text = index.plus(1).toString()
            tvSelectNumber.visibility = View.VISIBLE
            ivCheck.visibility = View.GONE
        } else {
            tvSelectNumber.visibility = View.GONE
            ivCheck.visibility = View.VISIBLE
        }
        setDoneCount()
    }

    private fun setDoneCount() {
        if (params.selectData.isEmpty()) {
            tvDone.setText(R.string.done_text)
        } else {
            tvDone.text = getString(R.string.done_format_text, params.selectData.size)
        }
    }

    private fun finishImageData() {
        if (params.selectData.isEmpty()) {
            val image = data[vpContainer.currentItem]
            ImageEventBus.getInstance().eventLiveData.value = ImageEventBus.ImageEvent(image, false)
        }

        finish()
        ImageEventBus.getInstance().finishLiveData.value = true
    }

    private fun parseResult() {
        setResult(
            Activity.RESULT_OK, Intent()
                .putExtra(KEY_IMAGE, data[vpContainer.currentItem])
                .putExtra(KEY_IS_ORIGINAL, cbOriginal.isChecked)

        )
    }

    override fun onStop() {
        onStopClearInstanceState()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        vpContainer.unregisterOnPageChangeCallback(pageCallback)
    }

}