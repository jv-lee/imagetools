package com.imagetools.app

import android.net.Uri
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        iv_image_one.setImageURI(Uri.fromFile(File(ImagePagerFragment.imagePath1)))
        iv_image_two.setImageURI(Uri.fromFile(File(ImagePagerFragment.imagePath2)))
        iv_image_three.setImageURI(Uri.fromFile(File(ImagePagerFragment.imagePath3)))
        iv_image_one.setOnClickListener {
            ImagePagerActivity.startActivity(this, it, "transitionName")
        }

        iv_image_two.setOnClickListener {

        }

        iv_image_three.setOnClickListener {

        }
    }

}