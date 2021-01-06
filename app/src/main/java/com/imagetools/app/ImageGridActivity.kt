package com.imagetools.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.SharedElementCallback
import com.imagetools.app.base.BaseActivity
import com.imagetools.select.ui.fragment.ImagePagerFragment
import kotlinx.android.synthetic.main.activity_image_grid.*
import java.io.File

/**
 * @author jv.lee
 * @date 2021/1/5
 * @description
 */
class ImageGridActivity : BaseActivity(R.layout.activity_image_grid) {

    private var position = 0
    private val imagePaths = arrayListOf(
        "/storage/emulated/0/dreame/imagesTemp/b6d95861aec95234f175439e63b6545d.jpeg",
        "/storage/emulated/0/dreame/imagesTemp/0fd74f6b934c11746b7aae81ef0b184f.jpeg",
        "/storage/emulated/0/dreame/imagesTemp/1180db1daa346583c557b2d4b0e18bdd.jpeg"
    )
    private val views by lazy { arrayOf(iv_image_one, iv_image_two, iv_image_three) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        views[0].setImageURI(Uri.fromFile(File(imagePaths[0])))
        views[1].setImageURI(Uri.fromFile(File(imagePaths[1])))
        views[2].setImageURI(Uri.fromFile(File(imagePaths[2])))
        views[0].setOnClickListener {
            ImagePagerActivity.startActivity(this, iv_image_one, 0,imagePaths[0],100, imagePaths)
        }

        views[1].setOnClickListener {
            ImagePagerActivity.startActivity(this, iv_image_two, 1,imagePaths[1],100, imagePaths)
        }

        views[2].setOnClickListener {
            ImagePagerActivity.startActivity(this, iv_image_three, 2,imagePaths[2],100, imagePaths)
        }

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                sharedElements.put(imagePaths[position], views[position])
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition.duration = 200
            window.sharedElementExitTransition.duration = 200
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
                position = data.getIntExtra(ImagePagerFragment.KEY_POSITION, 0)
            }
        }
        super.onActivityReenter(resultCode, data)
    }


}