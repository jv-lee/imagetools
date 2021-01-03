package com.imagetools.select.ui.activity

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.imagetools.select.R
import com.imagetools.select.ui.adapter.AlbumSelectAdapter
import com.imagetools.select.ui.adapter.ImageSelectAdapter
import com.imagetools.select.ui.adapter.base.BaseSelectAdapter
import com.imagetools.select.constant.Constants
import com.imagetools.select.ui.dialog.CompressProgresDialog
import com.imagetools.select.entity.Image
import com.imagetools.select.entity.LoadStatus
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.event.ImageEventBus
import com.imagetools.select.listener.ShakeItemClickListener
import com.imagetools.select.result.ActivityResultContracts
import com.imagetools.select.tools.Tools
import com.imagetools.select.viewmodel.ImageViewModel
import com.imagetools.select.ui.widget.ImageSelectBar
import kotlinx.android.synthetic.main.activity_image_select.*
import kotlinx.android.synthetic.main.layout_navigation.*

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description 图片选择视口
 */
internal class ImageSelectActivity : BaseActivity(R.layout.activity_image_select) {

    private val viewModel by viewModels<ImageViewModel>()
    private val selectConfig by lazy {
        intent.getParcelableExtra(Constants.CONFIG_KEY) ?: SelectConfig()
    }

    //共享元素动画使用 取消重复修改元素
    private var isReset = false
    private var animImage: Image? = null

    private var animator: ValueAnimator? = null

    private val loadingDialog by lazy { CompressProgresDialog(this) }

    private val mAlbumAdapter by lazy { AlbumSelectAdapter(this) }

    private val mImageAdapter by lazy {
        ImageSelectAdapter(
            this,
            isMultiple = selectConfig.isMultiple,
            selectLimit = selectConfig.selectLimit,
            columnCount = selectConfig.columnCount
        ).also {
            if (it.isMultiple) {
                it.setSelectCallback(object : BaseSelectAdapter.ItemSelectCallback {
                    override fun selectItem(item: Image) {
                        it.updateSelected(item)
                    }

                    override fun selectEnd(limit: Int) {
                        toast(getString(R.string.select_limit_description, limit))
                    }

                    override fun selectCall(count: Int) {
                        this@ImageSelectActivity.selectDoneCount(count)
                    }
                })
            }
        }
    }

    //单图裁剪后返回
    private val imageLaunch =
        registerForActivityResult(ActivityResultContracts.CropActivityResult()) {
            it ?: return@registerForActivityResult
            finishImagesResult(
                selectConfig,
                arrayListOf(it),
                cb_original.isChecked,
                loadingDialog
            )
        }

    private val eventObserver =
        Observer<ImageEventBus.ImageEvent> { it ->
            it ?: return@Observer
            if (it.isSelect) {
                val item = mImageAdapter.getItem(mImageAdapter.getPosition(it.image))
                mImageAdapter.selectList.remove(item)
                item.select = false
            } else {
                val item = mImageAdapter.getItem(mImageAdapter.getPosition(it.image))
                item.select = true
                mImageAdapter.selectList.add(item)
            }
            mImageAdapter.notifyDataSetChanged()
            selectDoneCount(mImageAdapter.selectList.size)
        }

    private val finishObserver = Observer<Boolean> {
        it ?: return@Observer
        if (mImageAdapter.selectList.isEmpty()) return@Observer
        loadingDialog.show()
        window.decorView.postDelayed({
            finishImagesResult(
                selectConfig,
                (mImageAdapter).selectList,
                cb_original.isChecked,
                loadingDialog
            )
        }, 300)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        bindView()
        bindListener()
        bindObservable()
        bindEvent()
    }

    private fun bindView() {
        const_navigation.visibility = if (selectConfig.isMultiple) View.VISIBLE else View.GONE

        streamer_view.setColumnCount(selectConfig.columnCount)

        lv_select.adapter = mAlbumAdapter

        gv_images.layoutAnimation = Tools.getItemOrderAnimator(this)

        //默认未选中状态
        checkNavigationView(false)
    }

    private fun bindListener() {
        tv_review.setOnClickListener {
            val position = mImageAdapter.getSelectFirstPosition()
            ImageDetailsActivity.startActivity(
                this,
                View(this),
                "",
                position,
                mImageAdapter.size,
                true,
                cb_original.isChecked,
                selectConfig.selectLimit,
                mImageAdapter.selectList,
                mImageAdapter.selectList
            )
        }
        tv_done.setOnClickListener {
            finishImagesResult(
                selectConfig,
                (mImageAdapter).selectList,
                cb_original.isChecked,
                loadingDialog
            )
        }
        mask.setOnClickListener {
            if (image_select_bar.isExpansion()) {
                image_select_bar.switch()
            }
        }
        image_select_bar.setAnimCallback(object : ImageSelectBar.AnimCallback {
            override fun animEnd() {
            }

            override fun animCall(enable: Boolean) {
                animator = Tools.selectViewTranslationAnimator(enable, lv_select, mask)
            }
        })
        lv_select.onItemClickListener = object : ShakeItemClickListener() {
            override fun onShakeClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = mAlbumAdapter.getItem(position)
                viewModel.albumId = item.id
                viewModel.albumName = item.name
                if (viewModel.isCurrentAlbum()) {
                    image_select_bar.switch()
                } else {
                    viewModel.getImages(LoadStatus.INIT)
                }
            }
        }
        gv_images.onItemClickListener = object : ShakeItemClickListener() {
            override fun onShakeClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (mImageAdapter.isMultiple) {
                    val imageView = view.findViewById<ImageView>(R.id.iv_image)
                    ImageDetailsActivity.startActivity(
                        this@ImageSelectActivity,
                        imageView,
                        mImageAdapter.getItem(position).path,
                        position,
                        mImageAdapter.size,
                        false,
                        cb_original.isChecked,
                        selectConfig.selectLimit,
                        arrayListOf<Image>().also { it.addAll(mImageAdapter.getData()) },
                        mImageAdapter.selectList
                    )
                } else {
                    imageLaunch.launch(mImageAdapter.getItem(position).also {
                        it.isSquare = selectConfig.isSquare
                        it.isCompress = selectConfig.isCompress
                    })
                }
            }

        }

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                //防止重复设置动画元素效果.
                if (!isReset) {
                    return
                }

                animImage?.let { image ->
                    isReset = false
                    val position = mImageAdapter.getPosition(image)
                    val itemView = gv_images.getChildAt(position - gv_images.firstVisiblePosition)
                    itemView?.let {
                        sharedElements.put(image.path, it.findViewById(R.id.iv_image))
                    }
                }

            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition.duration = 200
            window.sharedElementExitTransition.duration = 200
        }
    }

    private fun bindObservable() {
        viewModel.albumsLiveData.observe(this, Observer {
            mAlbumAdapter.addData(it)
            mAlbumAdapter.notifyDataSetChanged()
        })

        viewModel.imagesLiveData.observe(this, Observer {
            mImageAdapter.clearData()
            if (mImageAdapter.isMultiple) {
                mImageAdapter.selectList.clear()
                selectDoneCount(0)
            }

            mImageAdapter.addData(it)

            if (gv_images.adapter == null) {
                gv_images.adapter = mImageAdapter
                gv_images.numColumns = selectConfig.columnCount
                gv_images.visibility = View.VISIBLE
                streamer_view.loadCompleteDelay()
            } else {
                mImageAdapter.notifyDataSetChanged()
            }

            if (image_select_bar.isExpansion()) {
                image_select_bar.setSelectName(viewModel.albumName)
                image_select_bar.switch()
            }

        })

        viewModel.getAlbums()
        viewModel.getImages(LoadStatus.INIT)
    }

    private fun bindEvent() {
        if (selectConfig.isMultiple) {
            ImageEventBus.getInstance().eventLiveData.observeForever(eventObserver)
            ImageEventBus.getInstance().finishLiveData.observeForever(finishObserver)
        }
    }

    override fun onPause() {
        super.onPause()
        Tools.bindBottomFinishing(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        animator?.cancel()
        animator = null
        if (selectConfig.isMultiple) {
            ImageEventBus.getInstance().eventLiveData.value = null
            ImageEventBus.getInstance().eventLiveData.removeObserver(eventObserver)
            ImageEventBus.getInstance().finishLiveData.value = null
            ImageEventBus.getInstance().finishLiveData.removeObserver(finishObserver)
        }
    }

    /**
     * 共享元素回调设置
     * @param resultCode 返回code
     * @param data 返回数据 动态更改当前共享元素
     */
    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            data.extras?.let {
                isReset = true
                it.classLoader = Image::class.java.classLoader
                animImage = it.getParcelable(ImageDetailsActivity.KEY_IMAGE)
                cb_original.isChecked = it.getBoolean(ImageDetailsActivity.KEY_IS_ORIGINAL)
            }
        }
        super.onActivityReenter(resultCode, data)
    }

    /**
     * 图片选中后修改 Done按钮count数量
     * @param count 选中数量
     */
    private fun selectDoneCount(count: Int) {
        if (count == 0) {
            tv_done.setText(R.string.done_text)
        } else {
            tv_done.text = getString(R.string.done_format_text, count)
        }
        checkNavigationView(count > 0)
    }

    /**
     * 设置当前底部导航布局选中状态view修改.
     * @param enable  true选中图片状态 / false未选中图片状态
     */
    private fun checkNavigationView(enable: Boolean) {
        val textColor = ContextCompat.getColor(
            this,
            if (enable) R.color.colorText else R.color.colorTextPair
        )
        val textBackground = ContextCompat.getDrawable(
            this,
            if (enable) R.drawable.shape_button_press else R.drawable.shape_button_normal
        )
        tv_review.setTextColor(textColor)
        tv_review.isClickable = enable
        tv_done.setTextColor(textColor)
        tv_done.background = textBackground
        tv_done.isClickable = enable
    }

}