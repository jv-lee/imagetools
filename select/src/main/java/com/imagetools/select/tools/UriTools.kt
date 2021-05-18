package com.imagetools.select.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File


/**
 * @author jv.lee
 * @date 2020/12/10
 * @description
 */
object UriTools {

    fun getImageFilePath(context: Context): String {
        //设置文件路径创建文件对象
        val fileDir = File(context.filesDir.absolutePath, "images")
        if (!fileDir.exists()) fileDir.mkdir()
        val file = File(fileDir.absolutePath, "${System.currentTimeMillis()}.jpg")

        //文件创建操作
        if (!file.parentFile.exists()) file.parentFile.mkdir()

        return file.absolutePath
    }

    fun fileToUri(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FileProvider.getUriForFile(context, "${context.packageName}.select.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
    }

    fun pathToUri(context: Context, path: String): Uri {
        val file = File(path)
        return fileToUri(context, file)
    }

    fun uriToPath(context: Context, uri: Uri): String? {
        var imagePath: String?
        val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
        try {
            if (cursor == null) {
                imagePath = uri.path
            } else {
                cursor.moveToFirst()
                val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                imagePath = cursor.getString(index)
                cursor.close()
            }
        } catch (e: Exception) {
            imagePath = uri.path
        }
        return imagePath
    }

    fun uriToBitmap(context: Context, uri: Uri): Bitmap {
        return BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
    }

}