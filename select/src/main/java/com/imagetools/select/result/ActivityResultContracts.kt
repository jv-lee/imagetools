package com.imagetools.select.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import com.imagetools.select.constant.Constants
import com.imagetools.select.entity.Image
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.entity.TakeConfig
import com.imagetools.select.tools.UriTools
import com.imagetools.select.ui.activity.ImageSelectActivity
import com.soundclound.android.crop.Crop

/**
 * @author jv.lee
 * @date 2020/12/10
 * @description
 */
internal class ActivityResultContracts {

    internal class CropActivityResult :
        ActivityResultContract<Image, Image>() {

        private var saveUri: Uri? = null
        private var image: Image? = null

        override fun createIntent(context: Context, input: Image?): Intent {
            saveUri = UriTools.pathToUri(context, UriTools.getImageFilePath(context))
            image = input
            val uri = image?.uri
            val crop = Crop.of(uri, saveUri)
            if (input?.isSquare == true) {
                crop.asSquare()
            }
            if (input?.isCompress == true) {
                crop.asCompress()
            }
            return crop.getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Image? {
            if (resultCode == Activity.RESULT_OK) {
                image?.let {
                    return Image(it.id, saveUri ?: return null)
                }
            }
            return null
        }

    }

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

    internal class TakePicture : ActivityResultContract<TakeConfig, Image?>() {

        private var takeConfig: TakeConfig? = null

        private var image: Image? = null

        override fun createIntent(context: Context, input: TakeConfig): Intent {
            val uri = UriTools.pathToUri(context, UriTools.getImageFilePath(context))
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            takeConfig = input
            image = Image(
                Constants.DEFAULT_ID,
                uri,
                isCompress = input.isCompress,
                isSquare = input.isSquare
            )

            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Image? {
            return image
        }

        fun getTakeConfig(): TakeConfig? {
            return takeConfig
        }

    }

}