package com.imagetools.select.tools

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

/**
 * @author jv.lee
 * @date 2020/12/10
 * @description
 */
internal object UriTools {

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
            FileProvider.getUriForFile(context, "com.imagetools.select.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
    }

    fun pathToUri(context: Context, path: String): Uri {
        val file = File(path)
        return fileToUri(context, file)
    }
}