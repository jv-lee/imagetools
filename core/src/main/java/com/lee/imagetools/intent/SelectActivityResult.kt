package com.lee.imagetools.intent

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.lee.imagetools.activity.ImageSelectActivity
import com.lee.imagetools.constant.Constants
import com.lee.imagetools.entity.Image
import com.lee.imagetools.entity.SelectConfig

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
internal class SelectActivityResult : ActivityResultContract<SelectConfig, ArrayList<Image>>() {
    override fun createIntent(context: Context, input: SelectConfig): Intent {
        return Intent(context, ImageSelectActivity::class.java)
            .putExtra(Constants.CONFIG_KEY, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ArrayList<Image>? {
        if (resultCode == Constants.IMAGE_DATA_RESULT_CODE) {
            return intent?.getParcelableArrayListExtra<Image>(Constants.IMAGE_DATA_KEY)
        }
        return null
    }

}