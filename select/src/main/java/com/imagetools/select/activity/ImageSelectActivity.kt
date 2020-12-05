package com.imagetools.select.activity

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.imagetools.compress.CompressImageManager
import com.imagetools.compress.bean.Photo
import com.imagetools.compress.config.CompressConfig
import com.imagetools.compress.listener.CompressImage
import com.imagetools.select.R
import com.imagetools.select.adapter.AlbumSelectAdapter
import com.imagetools.select.adapter.ImageMultipleSelectAdapter
import com.imagetools.select.adapter.ImageSingleSelectAdapter
import com.imagetools.select.adapter.SelectAdapter
import com.imagetools.select.constant.Constants
import com.imagetools.select.dialog.LoadingDialog
import com.imagetools.select.entity.Album
import com.imagetools.select.entity.Image
import com.imagetools.select.entity.LoadStatus
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.intent.CropActivityResult
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
        intent.getParcelableExtra<SelectConfig>(Constants.CONFIG_KEY) ?: SelectConfig()
    }
    private var animator: ValueAnimator? = null

    private val loadingDialog by lazy { LoadingDialog(this) }

    private val mSelectAdapter by lazy {
        AlbumSelectAdapter().also {
            it.setOnItemClickListener(object : SelectAdapter.ItemClickListener<Album> {
                override fun onClickItem(position: Int, item: Album) {
                    mImagesAdapter.clearData()
                    if (mImagesAdapter is ImageMultipleSelectAdapter) {
                        (mImagesAdapter as ImageMultipleSelectAdapter).getSelectList().clear()
                        selectDoneCount(0)
                    }
                    image_select_bar.setSelectName(item.name)
                    image_select_bar.switch()
                    viewModel.albumId = item.id
                    viewModel.getImages(LoadStatus.INIT)
                }
            })
        }
    }

    private val mImagesAdapter by lazy {
        if (selectConfig.isMultiple)
            ImageMultipleSelectAdapter(
                Tools.getImageSize(this) / 4,
                selectConfig.selectCount
            ).also {
                it.setOnItemClickListener(object : SelectAdapter.ItemClickListener<Image> {
                    override fun onClickItem(position: Int, item: Image) {
                    }
                })
                it.setAutoLoadMoreListener(object : SelectAdapter.AutoLoadMoreListener {
                    override fun loadMore() {
                        viewModel.getImages(LoadStatus.LOAD_MORE)
                    }
                })
                it.setSelectCallback(object : ImageMultipleSelectAdapter.MultipleSelectCallback {
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
        else
            ImageSingleSelectAdapter(Tools.getImageSize(this) / 4).also {
                it.setOnItemClickListener(object : SelectAdapter.ItemClickListener<Image> {
                    override fun onClickItem(position: Int, item: Image) {
                        //裁剪请求
                        imageLaunch.launch(item)
                    }
                })
                it.setAutoLoadMoreListener(object : SelectAdapter.AutoLoadMoreListener {
                    override fun loadMore() {
                        viewModel.getImages(LoadStatus.LOAD_MORE)
                    }
                })
            }
    }

    /**
     * 单图裁剪后返回
     */
    private val imageLaunch by lazy {
        registerForActivityResult(CropActivityResult(selectConfig.isSquare)) {
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

        rv_select.layoutManager = LinearLayoutManager(this)
        rv_select.adapter = mSelectAdapter

        rv_images.layoutManager = GridLayoutManager(this, 4)
        rv_images.layoutAnimation = Tools.getItemOrderAnimator(this)
        (rv_images.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rv_images.adapter = mImagesAdapter
    }

    private fun bindListener() {
        tv_done.setOnClickListener {
            finishImagesResult((mImagesAdapter as ImageMultipleSelectAdapter).getSelectList())
        }
        mask.setOnClickListener {
            if (image_select_bar.getEnable()) {
                image_select_bar.switch()
            }
        }
        rv_select.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val view = rv.findChildViewUnder(e.x, e.y)
                if (view == null && image_select_bar.getEnable()) {
                    image_select_bar.switch()
                }
                return super.onInterceptTouchEvent(rv, e)
            }
        })
        image_select_bar.setAnimCallback(object : ImageSelectBar.AnimCallback {
            override fun animEnd() {
            }

            override fun animCall(enable: Boolean) {
                animator = Tools.selectViewTranslationAnimator(enable, rv_select, mask)
            }
        })
    }

    private fun bindObservable() {
        viewModel.albumsLiveData.observe(this, Observer {
            mSelectAdapter.updateData(it)
            Tools.viewTranslationHide(rv_select)
        })

        viewModel.imagesLiveData.observe(this, Observer {
            if (it.isNotEmpty()) mImagesAdapter.hasLoadMore = true
            mImagesAdapter.addData(it)
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

                override fun onCompressFailed(images: ArrayList<Photo>?, error: String?) {

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

}