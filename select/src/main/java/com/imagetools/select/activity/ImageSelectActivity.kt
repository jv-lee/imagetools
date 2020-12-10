package com.imagetools.select.activity

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.imagetools.compress.CompressImageManager
import com.imagetools.compress.bean.Photo
import com.imagetools.compress.config.CompressConfig
import com.imagetools.compress.listener.CompressImage
import com.imagetools.select.R
import com.imagetools.select.adapter.AlbumSelectAdapter
import com.imagetools.select.adapter.ImageSelectAdapter
import com.imagetools.select.adapter.base.BaseSelectAdapter
import com.imagetools.select.constant.Constants
import com.imagetools.select.dialog.LoadingDialog
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
    private var animator: ValueAnimator? = null

    private val loadingDialog by lazy { LoadingDialog(this) }

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
        registerForActivityResult(ActivityResultContracts.CropActivityResult(selectConfig.isSquare)) {
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
            if (image_select_bar.getEnable()) {
                image_select_bar.switch()
            }
        }
        lv_select.setOnTouchListener { view, motionEvent ->
            if ((view as ListView).childCount != 0 &&
                motionEvent.y > (view.getChildAt(0).height * mAlbumAdapter.count) &&
                image_select_bar.getEnable()
            ) {
                image_select_bar.switch()
            }
            super.onTouchEvent(motionEvent)
        }
        image_select_bar.setAnimCallback(object : ImageSelectBar.AnimCallback {
            override fun animEnd() {
            }

            override fun animCall(enable: Boolean) {
                animator = Tools.selectViewTranslationAnimator(enable, lv_select, mask)
            }
        })
        lv_select.setOnItemClickListener { adapterView, view, position, id ->
            mImageAdapter.clearData()
            if (mImageAdapter.isMultiple) {
                mImageAdapter.selectList.clear()
                selectDoneCount(0)
            }
            val item = mAlbumAdapter.getItem(position)
            image_select_bar.setSelectName(item.name)
            image_select_bar.switch()
            viewModel.albumId = item.id
            viewModel.getImages(LoadStatus.INIT)
        }
        gv_images.setOnItemClickListener { adapterView, view, position, id ->
            if (mImageAdapter.isMultiple) {
                toast("item click")
            } else {
                imageLaunch.launch(mImageAdapter.getItem(position))
            }
        }
    }

    private fun bindObservable() {
        viewModel.albumsLiveData.observe(this, Observer {
            mAlbumAdapter.addData(it)
            mAlbumAdapter.notifyDataSetChanged()
            Tools.viewTranslationHide(lv_select)
        })

        viewModel.imagesLiveData.observe(this, Observer {
            mImageAdapter.addData(it)

            if (gv_images.adapter == null) {
                gv_images.adapter = mImageAdapter
                gv_images.numColumns = selectConfig.columnCount
                gv_images.visibility = View.VISIBLE
//                streamer_view.loadComplete()
                streamer_view.loadCompleteDelay()
            } else {
                mImageAdapter.notifyDataSetChanged()
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