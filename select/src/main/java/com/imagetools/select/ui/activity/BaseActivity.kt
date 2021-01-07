package com.imagetools.select.ui.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.imagetools.compress.CompressImageManager
import com.imagetools.compress.bean.Photo
import com.imagetools.compress.config.CompressConfig
import com.imagetools.compress.listener.CompressImage
import com.imagetools.select.constant.Constants
import com.imagetools.select.entity.Image
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.ui.dialog.CompressProgresDialog

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
internal abstract class BaseActivity(@LayoutRes layoutId: Int) : AppCompatActivity(layoutId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        statusBar(window, false)
        setLightStatusIcon(this)
        super.onCreate(savedInstanceState)
    }

    fun FragmentActivity.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun FragmentActivity.checkPermission(permission: String) {
        if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED
        ) {
            throw RuntimeException("Please apply for '$permission' permission first")
        }
    }

    /**
     * 设置沉浸式状态栏
     *
     * @param window                   引用
     * @param navigationBarTranslucent 导航栏是否设置为透明
     */
    private fun statusBar(
        window: Window,
        navigationBarTranslucent: Boolean
    ) {
        //5.0以设置沉浸式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            //设置状态栏颜色调整
            window.statusBarColor = Color.TRANSPARENT
            var visibility = window.decorView.systemUiVisibility
            //布局内容全屏展示
            visibility = visibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            //隐藏虚拟导航栏
//            visibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            //设置沉浸式 导航栏
            if (navigationBarTranslucent) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            }
            //防止内容区域大小发生变化
            visibility = visibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = visibility
            //4.0设置
        } else {
            //设置沉浸式 状态栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                //设置沉浸式 导航栏
                if (navigationBarTranslucent) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                }
            }
        }
    }


    /**
     * 保持原有flag 设置深色状态栏颜色
     *
     * @param activity
     */
    private fun setDarkStatusIcon(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val originFlag = activity.window.decorView.systemUiVisibility
            activity.window.decorView.systemUiVisibility =
                originFlag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    /**
     * 保留原有flag 清除深色状态栏颜色
     *
     * @param activity
     */
    private fun setLightStatusIcon(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val originFlag = activity.window.decorView.systemUiVisibility
            //使用异或清除SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity.window.decorView.systemUiVisibility =
                originFlag and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }

    /**
     * 根据压缩配置 启动图片压缩.
     * @param images 未压缩图片列表
     */
    fun FragmentActivity.finishImagesResult(
        config: SelectConfig,
        images: ArrayList<Image>,
        isOriginal: Boolean,
        loadingDialog: CompressProgresDialog
    ) {
        //不使用自带压缩
        if (!config.isCompress) {
            parseImageResult(images.also {
                for (image in it) {
                    image.isCompress = !isOriginal
                }
            }, loadingDialog)
            return
        }
        //使用自带压缩 且 使用原图模式 取消压缩方式
        if (config.isCompress && isOriginal) {
            parseImageResult(images, loadingDialog)
            return
        }
        //使用自带压缩
        loadingDialog.show()
        CompressImageManager.build(
            applicationContext,
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
                    parseImageResult(images, loadingDialog)
                }

                override fun onCompressProgress(progress: Int) {
                    loadingDialog.setProgress(progress)
                }

                override fun onCompressFailed(images: ArrayList<Photo>?, error: String?) {
                    parseImageResult(arrayListOf(), loadingDialog)
                }

            }).compress()
    }

    /**
     * 图片列表设置到result中.
     * @param images 需要返回的图片集合
     */
    fun FragmentActivity.parseImageResult(
        images: ArrayList<Image>,
        loadingDialog: CompressProgresDialog
    ) {
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