package com.lee.imagetools.intent

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.lee.imagetools.activity.ImageSelectActivity
import com.lee.imagetools.constant.Constants
import com.lee.imagetools.entity.Image

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
internal class SelectActivityResult : ActivityResultContract<Int, Image>() {
    override fun createIntent(context: Context, input: Int?): Intent {
        return Intent(context, ImageSelectActivity::class.java)
            .putExtra(Constants.IMAGE_KEY, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Image? {
        if (resultCode == Constants.IMAGE_CROP_RESULT_CODE) {
            return intent?.getParcelableExtra(Constants.IMAGE_KEY)
        }
        return null
    }

}