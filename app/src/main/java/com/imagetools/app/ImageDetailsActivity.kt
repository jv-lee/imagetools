package com.imagetools.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import com.imagetools.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_image.*

/**
 * @author jv.lee
 * @date 2020/12/14
 * @description
 */
class ImageDetailsActivity : BaseActivity(R.layout.activity_image_details) {

    companion object {
        const val TAG = "IMAGE_DETAILS"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        iv_image.setOnTouchListener { v, event ->
            event.x
            true
        }
    }
}