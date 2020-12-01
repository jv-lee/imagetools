package com.lee.imagetools

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lee.imagetools.adapter.AlbumSelectAdapter
import com.lee.imagetools.adapter.ImageSelectAdapter
import com.lee.imagetools.adapter.SelectAdapter
import com.lee.imagetools.constant.Constants
import com.lee.imagetools.entity.Album
import com.lee.imagetools.tools.Tools
import com.lee.imagetools.viewmodel.ImageViewModel
import com.lee.imagetools.widget.ImageSelectBar

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val viewModel by viewModels<ImageViewModel>()
    private val mSelectAdapter by lazy { AlbumSelectAdapter(arrayListOf()) }
    private val mImagesAdapter by lazy { ImageSelectAdapter(arrayListOf()) }

    private val imageSelectBar by lazy { findViewById<ImageSelectBar>(R.id.image_select_bar) }
    private val viewMask by lazy { findViewById<View>(R.id.mask) }
    private val rvSelect by lazy { findViewById<RecyclerView>(R.id.rv_select) }
    private val rvImages by lazy { findViewById<RecyclerView>(R.id.rv_images) }

    private var animator: ValueAnimator? = null

    private fun AppCompatActivity.requestPermission(
        permission: String,
        successCall: () -> Unit,
        failedCall: (String) -> Unit = {}
    ) {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { it ->
            if (it) successCall() else failedCall(permission)
        }.launch(permission)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindView()
        bindListener()
        bindObservable()
    }

    private fun bindView() {
        rvSelect.layoutManager = LinearLayoutManager(this)
        rvSelect.adapter = mSelectAdapter

        rvImages.layoutManager = GridLayoutManager(this, 4)
        rvImages.layoutAnimation = Tools.getItemOrderAnimator(this)
        rvImages.adapter = mImagesAdapter
    }

    private fun bindListener() {
        viewMask.setOnClickListener {
            if (imageSelectBar.getEnable()) {
                imageSelectBar.switch()
            }
        }
        rvSelect.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val view = rv.findChildViewUnder(e.x, e.y)
                if (view == null && imageSelectBar.getEnable()) {
                    imageSelectBar.switch()
                }
                return super.onInterceptTouchEvent(rv, e)
            }
        })
        mSelectAdapter.setOnItemClickListener(object : SelectAdapter.ItemClickListener<Album> {
            override fun onClickItem(position: Int, item: Album) {
                viewModel.getImagesByAlbumId(item.id)
                imageSelectBar.setSelectName(item.name)
                imageSelectBar.switch()
            }
        })

        imageSelectBar.setAnimCallback(object : ImageSelectBar.AnimCallback {
            override fun animCall(enable: Boolean) {
                animator = Tools.selectViewTranslationAnimator(enable, rvSelect, viewMask)
            }
        })
        (imageSelectBar as LifecycleObserver)
    }

    private fun bindObservable() {
        viewModel.albumsLiveData.observe(this, Observer {
            mSelectAdapter.updateData(it)
            Tools.viewTranslationHide(rvSelect)
        })

        viewModel.imagesLiveData.observe(this, Observer {
            mImagesAdapter.updateData(it)
        })

        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, {
            viewModel.getAlbums()
            viewModel.getImagesByAlbumId(Constants.DEFAULT_ALBUM_ID)
        })
    }

    private fun startImageSelected(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_DENIED
        ) {
            throw RuntimeException("Please apply for 'Manifest.permission.WRITE_EXTERNAL_STORAGE' permission first")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        animator?.cancel()
        animator = null
    }

}