package com.imagetools.select.intent

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.imagetools.select.activity.ImageSelectActivity
import com.imagetools.select.activity.ImageSelectActivity2
import com.imagetools.select.constant.Constants
import com.imagetools.select.entity.Image
import com.imagetools.select.entity.SelectConfig

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
internal class SelectActivityResult : ActivityResultContract<SelectConfig, ArrayList<Image>>() {
    override fun createIntent(context: Context, input: SelectConfig): Intent {
        return Intent(context, ImageSelectActivity2::class.java)
            .putExtra(Constants.CONFIG_KEY, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ArrayList<Image>? {
        if (resultCode == Constants.IMAGE_DATA_RESULT_CODE) {
            return intent?.getParcelableArrayListExtra<Image>(Constants.IMAGE_DATA_KEY)
        }
        return null
    }

}