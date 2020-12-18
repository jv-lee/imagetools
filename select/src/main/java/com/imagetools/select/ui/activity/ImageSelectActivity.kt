package com.imagetools.select.ui.activity

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.imagetools.compress.CompressImageManager
import com.imagetools.compress.bean.Photo
import com.imagetools.compress.config.CompressConfig
import com.imagetools.compress.listener.CompressImage
import com.imagetools.select.R
import com.imagetools.select.adapter.AlbumSelectAdapter
import com.imagetools.select.adapter.ImageSelectAdapter
import com.imagetools.select.adapter.base.BaseSelectAdapter
import com.imagetools.select.constant.Constants
import com.imagetools.select.dialog.CompressProgresDialog
import com.imagetools.select.entity.Image
import com.imagetools.select.entity.LoadStatus
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.result.ActivityResultContracts
import com.imagetools.select.tools.Tools
import com.imagetools.select.viewmodel.ImageViewModel
import com.imagetools.select.widget.ImageSelectBar
import kotlinx.android.synthetic.main.activity_image_select.*

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
internal class ImageSelectActivity : BaseActivity(R.layout.activity_image_select) {

    private val viewModel by viewModels<ImageViewModel>()
    private val selectConfig by lazy {
        intent.getParcelableExtra(Constants.CONFIG_KEY) ?: SelectConfig()
    }

    //共享元素动画使用 取消重复修改元素
    private var isReset = false
    private var position = 0

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

    /**
     * 单图裁剪后返回
     */
    private val imageLaunch by lazy {
        registerForActivityResult(ActivityResultContracts.CropActivityResult()) {
            it ?: return@registerForActivityResult
            finishImagesResult(arrayListOf(it))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageLaunch
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            throw RuntimeException("Please apply for 'Manifest.permission.WRITE_EXTERNAL_STORAGE' permission first")
        }
        bindView()
        bindListener()
        bindObservable()
    }

    private fun bindView() {
        const_navigation.visibility = if (selectConfig.isMultiple) View.VISIBLE else View.GONE

        streamer_view.setColumnCount(selectConfig.columnCount)

        lv_select.adapter = mAlbumAdapter
        gv_images.layoutAnimation = Tools.getItemOrderAnimator(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindListener() {
        tv_done.setOnClickListener {
            finishImagesResult((mImageAdapter).selectList)
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
        lv_select.setOnItemClickListener { adapterView, view, position, id ->
            val item = mAlbumAdapter.getItem(position)
            viewModel.albumId = item.id
            viewModel.albumName = item.name
            if (viewModel.isCurrentAlbum()) {
                image_select_bar.switch()
            } else {
                viewModel.getImages(LoadStatus.INIT)
            }
        }
        gv_images.setOnItemClickListener { adapterView, view, position, id ->
            if (mImageAdapter.isMultiple) {
                ImageDetailsActivity.startActivity(
                    this,
                    position,
                    view.findViewById(R.id.iv_image),
                    arrayListOf<Image>().also { it.addAll(mImageAdapter.getData()) })
            } else {
                imageLaunch.launch(mImageAdapter.getItem(position).also {
                    it.isSquare = selectConfig.isSquare
                    it.isCompress = selectConfig.isCompress
                })
            }
        }
        gv_images.setOnScrollListener(mImageAdapter)
        Glide.with(gv_images).resumeRequests()

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                if (!isReset) return
                isReset = false
                val itemView = gv_images.getChildAt(position - gv_images.firstVisiblePosition)
                itemView?.let {
                    sharedElements.put(position.toString(), it.findViewById(R.id.iv_image))
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

    override fun onPause() {
        super.onPause()
        Tools.bindBottomFinishing(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        animator?.cancel()
        animator = null
    }

    private fun selectDoneCount(count: Int) {
        if (count == 0) {
            tv_done.setText(R.string.done_text)
        } else {
            tv_done.text = getString(R.string.done_format_text, count)
        }
        checkNavigationView(count > 0)
    }

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

    private fun finishImagesResult(images: ArrayList<Image>) {
        //不使用自带压缩
        if (!selectConfig.isCompress) {
            parseImageResult(images.also {
                for (image in it) {
                    image.isCompress = !cb_original.isChecked
                }
            })
            return
        }
        //使用自带压缩 且 使用原图模式 取消压缩方式
        if (selectConfig.isCompress && cb_original.isChecked) {
            parseImageResult(images)
            return
        }
        //使用自带压缩
        loadingDialog.show()
        CompressImageManager.build(
            this,
            CompressConfig.getDefaultConfig(),
            arrayListOf<Photo>().also {
                for (image in images) {
                    it.add(Photo(image.path))
                }
            },
            object : CompressImage.CompressListener {
                override fun onCompressSuccess(photos: ArrayList<Photo>?) {
                    photos?.let {
                        for ((index, image) in images.withIndex()) {
                            image.path = it[index].compressPath
                        }
                    }
                    parseImageResult(images)
                }

                override fun onCompressProgress(progress: Int) {
                    loadingDialog.setProgress(progress)
                }

                override fun onCompressFailed(images: ArrayList<Photo>?, error: String?) {
                    parseImageResult(arrayListOf())
                }

            }).compress()
    }

    private fun parseImageResult(images: ArrayList<Image>) {
        loadingDialog.dismiss()
        setResult(
            Constants.IMAGE_DATA_RESULT_CODE,
            Intent().putParcelableArrayListExtra(
                Constants.IMAGE_DATA_KEY,
                images
            )
        )
        finish()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            data.extras?.let {
                isReset = true
                position = it.getInt(ImageDetailsActivity.KEY_POSITION, 0)
            }
        }
        super.onActivityReenter(resultCode, data)
    }

}