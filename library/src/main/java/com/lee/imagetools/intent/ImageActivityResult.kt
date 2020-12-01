package com.lee.imagetools.intent

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.lee.imagetools.constant.Constants
import com.lee.imagetools.entity.Image

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
internal class ImageActivityResult(private val cls: Class<*>) : ActivityResultContract<Image, Image>() {
    override fun createIntent(context: Context, input: Image?): Intent {
        return Intent(context, cls)
            .putExtra(Constants.IMAGE_KEY, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Image? {
        if (resultCode == Constants.IMAGE_CROP_RESULT_CODE) {
            return intent?.getParcelableExtra(Constants.IMAGE_KEY)
        }
        return null
    }

}