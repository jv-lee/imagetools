package com.imagetools.select.ui.activity

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.GridView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.imagetools.select.R
import com.imagetools.select.constant.Constants
import com.imagetools.select.entity.Image
import com.imagetools.select.entity.LoadStatus
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.event.ImageEventBus
import com.imagetools.select.listener.ShakeItemClickListener
import com.imagetools.select.result.ActivityResultContracts
import com.imagetools.select.tools.SharedElementTools.clearTransitionState
import com.imagetools.select.tools.Tools
import com.imagetools.select.ui.adapter.AlbumSelectAdapter
import com.imagetools.select.ui.adapter.ImageSelectAdapter
import com.imagetools.select.ui.adapter.base.BaseSelectAdapter
import com.imagetools.select.ui.dialog.CompressProgressDialog
import com.imagetools.select.ui.widget.ImageSelectBar
import com.imagetools.select.ui.widget.StreamerView
import com.imagetools.select.viewmodel.ImageViewModel


/**
 * @author jv.lee
 * @date 2020/12/1
 * @description 图片选择视口
 */
internal class ImageSelectActivity : BaseActivity(R.layout.activity_image_select_imagetools),
    OnClickListener,
    BaseSelectAdapter.ItemSelectCallback {

    private val viewModel by viewModels<ImageViewModel>()

    // 共享元素动画使用 取消重复修改元素
    private var isReset = false
    private var animImage: Image? = null
    private var animator: ValueAnimator? = null
    private var shareCallback: SharedElementCallback? = null

    // 事件观察者
    private var eventObserver: Observer<ImageEventBus.ImageEvent>? = null
    private var finishObserver: Observer<Boolean>? = null

    private val selectConfig by lazy {
        intent.getParcelableExtra(Constants.CONFIG_KEY) ?: SelectConfig()
    }

    private val mImageAdapter by lazy {
        ImageSelectAdapter(
            isMultiple = selectConfig.isMultiple,
            selectLimit = selectConfig.selectLimit,
            columnCount = selectConfig.columnCount
        )
    }

    private val mAlbumAdapter by lazy { AlbumSelectAdapter() }

    private val loadingDialog by lazy { CompressProgressDialog(this) }

    // 单图裁剪后返回
    private val imageLaunch =
        registerForActivityResult(ActivityResultContracts.CropActivityResult()) {
            finishImagesResult(
                selectConfig,
                arrayListOf(it),
                cbOriginal.isChecked,
                loadingDialog
            )
        }

    private val constNavigation: ConstraintLayout by lazy { findViewById(R.id.const_navigation) }
    private val streamerView: StreamerView by lazy { findViewById(R.id.streamer_view) }
    private val cbOriginal: CheckBox by lazy { findViewById(R.id.cb_original) }
    private val lvSelect: ListView by lazy { findViewById(R.id.lv_select) }
    private val gvImages: GridView by lazy { findViewById(R.id.gv_images) }
    private val imageSelectBar: ImageSelectBar by lazy { findViewById(R.id.image_select_bar) }
    private val tvReview: TextView by lazy { findViewById(R.id.tv_review) }
    private val tvDone: TextView by lazy { findViewById(R.id.tv_done) }
    private val mask: View by lazy { findViewById(R.id.mask) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        bindView()
        bindListener()
        bindObservable()
        bindEvent()
    }

    private fun bindView() {
        constNavigation.visibility = if (selectConfig.isMultiple) View.VISIBLE else View.GONE

        streamerView.setColumnCount(selectConfig.columnCount)

        lvSelect.adapter = mAlbumAdapter

        gvImages.layoutAnimation = Tools.getItemOrderAnimator(this)

        if (mImageAdapter.isMultiple) {
            mImageAdapter.setSelectCallback(this)
        }

        // 默认未选中状态
        checkNavigationView(false)
    }

    private fun bindListener() {
        tvReview.setOnClickListener(this)
        tvDone.setOnClickListener(this)
        mask.setOnClickListener(this)
        imageSelectBar.setAnimCallback(object : ImageSelectBar.AnimCallback {
            override fun animEnd() {
            }

            override fun animCall(enable: Boolean) {
                animator = Tools.selectViewTranslationAnimator(enable, lvSelect, mask)
            }
        })
        lvSelect.onItemClickListener = object : ShakeItemClickListener() {
            override fun onShakeClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val item = mAlbumAdapter.getItem(position)
                viewModel.albumId = item.id
                viewModel.albumName = item.name ?: ""
                if (viewModel.isCurrentAlbum()) {
                    imageSelectBar.switch()
                } else {
                    viewModel.getImages(LoadStatus.INIT)
                }
            }
        }
        gvImages.onItemClickListener = object : ShakeItemClickListener() {
            override fun onShakeClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (mImageAdapter.isMultiple) {
                    val imageView = view.findViewById<ImageView>(R.id.iv_image)
                    ImageDetailsActivity.startActivity(
                        this@ImageSelectActivity,
                        imageView,
                        arrayListOf<Image>().also { it.addAll(mImageAdapter.getData()) },
                        ImageDetailsActivity.Companion.ImageDetailsParams(
                            mImageAdapter.getItem(position).uri.path ?: "",
                            position,
                            mImageAdapter.size,
                            false,
                            cbOriginal.isChecked,
                            selectConfig.selectLimit,
                            mImageAdapter.selectList
                        )
                    )
                } else {
                    imageLaunch.launch(mImageAdapter.getItem(position).also {
                        it.isSquare = selectConfig.isSquare
                        it.isCompress = selectConfig.isCompress
                    })
                }
            }

        }

        shareCallback ?: kotlin.run {
            shareCallback = object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: MutableList<String>,
                    sharedElements: MutableMap<String, View>
                ) {
                    // 防止重复设置动画元素效果.
                    if (!isReset) {
                        return
                    }

                    animImage?.let { image ->
                        isReset = false
                        val position = mImageAdapter.getPosition(image)
                        val itemView = gvImages.getChildAt(position - gvImages.firstVisiblePosition)
                        itemView?.let {
                            sharedElements.put(image.uri.path ?: "", it.findViewById(R.id.iv_image))
                        }
                    }

                }
            }
        }
        shareCallback?.run(this::setExitSharedElementCallback)
        window.sharedElementEnterTransition.duration = 200
        window.sharedElementExitTransition.duration = 200
    }

    private fun bindObservable() {
        viewModel.albumsLiveData.observe(this) {
            mAlbumAdapter.addData(it)
            mAlbumAdapter.notifyDataSetChanged()
        }

        viewModel.imagesLiveData.observe(this) {
            mImageAdapter.clearData()
            if (mImageAdapter.isMultiple) {
                mImageAdapter.selectList.clear()
                selectDoneCount(0)
            }

            mImageAdapter.addData(it)

            if (gvImages.adapter == null) {
                gvImages.adapter = mImageAdapter
                gvImages.numColumns = selectConfig.columnCount
                gvImages.visibility = View.VISIBLE
                streamerView.loadCompleteDelay()
            } else {
                mImageAdapter.notifyDataSetChanged()
            }

            if (imageSelectBar.isExpansion()) {
                imageSelectBar.setSelectName(viewModel.albumName)
                imageSelectBar.switch()
            }

        }

        viewModel.getAlbums()
        viewModel.getImages(LoadStatus.INIT)
    }

    private fun bindEvent() {
        if (selectConfig.isMultiple) {
            eventObserver ?: kotlin.run {
                eventObserver = Observer { event ->
                    event.image ?: return@Observer
                    if (event.isSelect) {
                        val item = mImageAdapter.getItem(mImageAdapter.getPosition(event.image))
                        item.select = true
                        mImageAdapter.selectList.add(item)
                    } else {
                        val item = mImageAdapter.getItem(mImageAdapter.getPosition(event.image))
                        mImageAdapter.selectList.remove(item)
                        item.select = false
                    }
                    mImageAdapter.notifyDataSetChanged()
                    selectDoneCount(mImageAdapter.selectList.size)
                }
            }
            finishObserver ?: kotlin.run {
                finishObserver = Observer { isFinishing ->
                    if (!isFinishing) return@Observer
                    if (mImageAdapter.selectList.isEmpty()) return@Observer
                    loadingDialog.show()
                    window.decorView.postDelayed({
                        finishImagesResult(
                            selectConfig,
                            (mImageAdapter).selectList,
                            cbOriginal.isChecked,
                            loadingDialog
                        )
                    }, 300)
                }
            }
            eventObserver?.run(ImageEventBus.getInstance().eventLiveData::observeForever)
            finishObserver?.run(ImageEventBus.getInstance().finishLiveData::observeForever)
        }
    }

    /**
     * 图片选中后修改 Done按钮count数量
     * @param count 选中数量
     */
    private fun selectDoneCount(count: Int) {
        if (count == 0) {
            tvDone.setText(R.string.done_text)
        } else {
            tvDone.text = getString(R.string.done_format_text, count)
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
        tvReview.setTextColor(textColor)
        tvReview.isClickable = enable
        tvDone.setTextColor(textColor)
        tvDone.background = textBackground
        tvDone.isClickable = enable
    }


    override fun onClick(v: View) {
        when (v) {
            tvReview -> {
                val position = mImageAdapter.getSelectFirstPosition()
                ImageDetailsActivity.startActivity(
                    this,
                    View(this),
                    mImageAdapter.selectList,
                    ImageDetailsActivity.Companion.ImageDetailsParams(
                        "",
                        position,
                        mImageAdapter.size,
                        true,
                        cbOriginal.isChecked,
                        selectConfig.selectLimit,
                        mImageAdapter.selectList
                    )
                )
            }

            tvDone -> {
                finishImagesResult(
                    selectConfig,
                    (mImageAdapter).selectList,
                    cbOriginal.isChecked,
                    loadingDialog
                )
            }

            mask -> {
                if (imageSelectBar.isExpansion()) {
                    imageSelectBar.switch()
                }
            }
        }

    }

    override fun selectItem(item: Image) {
        mImageAdapter.updateSelected(item)
    }

    override fun selectEnd(limit: Int) {
        toast(getString(R.string.select_limit_description, limit))
    }

    override fun selectCall(count: Int) {
        selectDoneCount(count)
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
                cbOriginal.isChecked = it.getBoolean(ImageDetailsActivity.KEY_IS_ORIGINAL)
            }
        }
        super.onActivityReenter(resultCode, data)
    }

    override fun onPause() {
        if (isFinishing) overridePendingTransition(R.anim.default_in_out, R.anim.slide_bottom_out)
        super.onPause()
    }

    override fun onStop() {
        clearTransitionState()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        animator?.cancel()
        animator = null
        mImageAdapter.clearData()
        mAlbumAdapter.clearData()
        gvImages.adapter = null
        lvSelect.adapter = null
        if (selectConfig.isMultiple) {
            ImageEventBus.getInstance().eventLiveData.value = ImageEventBus.ImageEvent()
            eventObserver?.run(ImageEventBus.getInstance().eventLiveData::removeObserver)
            eventObserver = null
            ImageEventBus.getInstance().finishLiveData.value = false
            finishObserver?.run(ImageEventBus.getInstance().finishLiveData::removeObserver)
            finishObserver = null
        }
    }

}