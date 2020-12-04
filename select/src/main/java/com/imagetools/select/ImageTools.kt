package com.imagetools.select

import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
        activity: FragmentActivity,
        call: (item: ArrayList<Image>) -> Unit
    ): ActivityResultLauncher<SelectConfig> {
        return activity.registerForActivityResult(SelectActivityResult()) {
            it?.let(call)
        }
    }

    fun selectLaunch(
        fragment: Fragment,
        call: (item: ArrayList<Image>) -> Unit
    ): ActivityResultLauncher<SelectConfig> {
        return fragment.registerForActivityResult(SelectActivityResult()) {
            it?.let(call)
        }
    }

}