package com.imagetools.select

import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.imagetools.select.entity.Image
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.intent.SelectActivityResult

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
object ImageTools {

    fun selectLaunch(
        activity: AppCompatActivity,
        call: (item: ArrayList<Image>) -> Unit
    ): ActivityResultLauncher<SelectConfig> {
        return activity.registerForActivityResult(SelectActivityResult()) {
            it?.let(call)
        }
    }

}