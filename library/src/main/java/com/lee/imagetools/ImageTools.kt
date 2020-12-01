package com.lee.imagetools

import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.lee.imagetools.entity.Image
import com.lee.imagetools.intent.SelectActivityResult

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
object ImageTools {

    fun singleSelectLaunch(
        activity: AppCompatActivity,
        call: (item: Image) -> Unit
    ): ActivityResultLauncher<Int> {
        return activity.registerForActivityResult(SelectActivityResult()) {
            it?.let(call)
        }
    }

}