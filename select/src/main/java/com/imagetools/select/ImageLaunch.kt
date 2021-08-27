package com.imagetools.select

import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.imagetools.select.entity.Image
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.entity.TakeConfig
import com.imagetools.select.result.ActivityResultContracts

/**
 * @author jv.lee
 * @date 2020/12/11
 * @description 图片选择器功能入口
 */
class ImageLaunch {

    constructor(fragmentActivity: FragmentActivity) {
        createLaunch(fragmentActivity)
    }

    constructor(fragment: Fragment) {
        createLaunch(fragment)
    }

    private var selectLaunch: ActivityResultLauncher<SelectConfig>? = null
    private var cropLaunch: ActivityResultLauncher<Image>? = null
    private var takeLaunch: ActivityResultLauncher<TakeConfig>? = null

    private var imageCall: ((image: Image) -> Unit)? = null
    private var imageFailedCall: (() -> Unit)? = null
    private var imagesCall: ((images: ArrayList<Image>) -> Unit)? = null
    private var imagesFailedCall: (() -> Unit)? = null

    private fun createLaunch(thisClass: Any) {
        val thisT = when (thisClass) {
            is FragmentActivity -> {
                thisClass
            }
            is Fragment -> {
                thisClass
            }
            else -> {
                throw ClassCastException("thisClass type not is FragmentActivity/Fragment")
            }
        }

        this.selectLaunch =
            thisT.registerForActivityResult(ActivityResultContracts.SelectActivityResult()) {
                if (it.isNullOrEmpty()) {
                    imagesFailedCall?.invoke()
                } else {
                    imagesCall?.invoke(it)
                }
            }
        this.cropLaunch =
            thisT.registerForActivityResult(ActivityResultContracts.CropActivityResult()) {
                if (it == null) {
                    imageFailedCall?.invoke()
                } else {
                    imageCall?.invoke(it)
                }
            }
        val takePicture = ActivityResultContracts.TakePicture()
        this.takeLaunch =
            thisT.registerForActivityResult(takePicture) {
                takePicture.getTakeConfig()?.let { takeConfig ->
                    it?.let { image ->
                        image.isCompress = takeConfig.isCompress
                        image.isSquare = takeConfig.isSquare
                    }
                }
                if (takePicture.getTakeConfig()?.isCrop == true) {
                    cropLaunch?.launch(it)
                    return@registerForActivityResult
                }

                if (it == null) {
                    imageFailedCall?.invoke()
                } else {
                    imageCall?.invoke(it)
                }
            }

        //设置声明周期监听
        thisT.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun destroy() {
                thisT.lifecycle.removeObserver(this)
                selectLaunch?.unregister()
                cropLaunch?.unregister()
                takeLaunch?.unregister()
            }
        })
    }

    /**
     * 启动图片选择
     * @param config 图片选择配置
     * @param call 图片选择后回调
     */
    fun select(config: SelectConfig, call: (image: ArrayList<Image>) -> Unit) {
        imagesCall = call
        selectLaunch?.launch(config)
    }

    /**
     * 启动相机拍摄图像
     * @param config 拍摄配置
     * @param call 拍摄成功后返回图像回调
     */
    fun take(config: TakeConfig, call: (image: Image) -> Unit) {
        imageCall = call
        takeLaunch?.launch(config)
    }

    /**
     * 启动图片裁剪
     * @param image 图像实体
     * @param call 成功回调
     */
    fun crop(image: Image, call: (image: Image) -> Unit) {
        imageCall = call
        cropLaunch?.launch(image)
    }

}