package com.imagetools.select.intent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.imagetools.select.entity.Image
import com.soundclound.android.crop.Crop
import java.io.File

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
internal class CropActivityResult(private val isSquare: Boolean) :
    ActivityResultContract<Image, Image>() {

    private var tempPath: String? = null
    private var image: Image? = null

    override fun createIntent(context: Context, input: Image?): Intent {
        tempPath = context.cacheDir.absolutePath + File.separator +
                System.currentTimeMillis() + ".png"
        image = input
        val crop = Crop.of(input?.getImageUri(), Uri.fromFile(File(tempPath)))
        if (isSquare) {
            crop.asSquare()
        }
        return crop.getIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Image? {
        if (resultCode == Activity.RESULT_OK) {
            image?.let {
                return Image(it.id, tempPath ?: return null)
            }
        }
        return null
    }

}