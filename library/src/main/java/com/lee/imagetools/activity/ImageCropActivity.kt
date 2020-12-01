package com.lee.imagetools.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lee.imagetools.R
import com.lee.imagetools.constant.Constants
import com.lee.imagetools.entity.Image

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
internal class ImageCropActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)
        setResult(
            Constants.IMAGE_CROP_RESULT_CODE,
            Intent().putExtra(
                Constants.IMAGE_KEY,
                intent.getParcelableExtra<Image>(Constants.IMAGE_KEY)
            )
        )
    }
}