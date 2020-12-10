package com.imagetools.select

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.imagetools.select.entity.Image
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.entity.TakeConfig
import com.imagetools.select.result.ActivityResultContracts

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
        return activity.registerForActivityResult(ActivityResultContracts.SelectActivityResult()) {
            it?.let(call)
        }
    }

    fun selectLaunch(
        fragment: Fragment,
        call: (item: ArrayList<Image>) -> Unit
    ): ActivityResultLauncher<SelectConfig> {
        return fragment.registerForActivityResult(ActivityResultContracts.SelectActivityResult()) {
            it?.let(call)
        }
    }

    fun takeLaunch(
        activity: FragmentActivity,
        call: (image: Image) -> Unit
    ): ActivityResultLauncher<TakeConfig> {
        val takePicture = ActivityResultContracts.TakePicture()
        val cropLauncher = activity.registerForActivityResult(
            ActivityResultContracts.CropActivityResult(
                takePicture.getTakeConfig()?.isSquare ?: true
            )
        ) {
            it?.let(call)
        }
        return activity.registerForActivityResult(takePicture) {
            if (takePicture.getTakeConfig()?.isCrop == true) {
                cropLauncher.launch(it)
            } else {
                it?.let(call)
            }
        }
    }

//    fun takeLaunch(
//        activity: Fragment,
//        call: (uri: Uri) -> Unit
//    ): ActivityResultLauncher<TakeConfig> {
//        return activity.registerForActivityResult(ActivityResultContracts.TakePicture()) {
//            it?.let(call)
//        }
//    }

}