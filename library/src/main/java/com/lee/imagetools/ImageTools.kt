package com.lee.imagetools

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.lee.imagetools.activity.ImageSelectActivity

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
object ImageTools {

    fun intoImageSelect(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_DENIED
        ) {
            throw RuntimeException("Please apply for 'Manifest.permission.WRITE_EXTERNAL_STORAGE' permission first")
        }
        activity.startActivity(Intent(activity, ImageSelectActivity::class.java))
    }

}