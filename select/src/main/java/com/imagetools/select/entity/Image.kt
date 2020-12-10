package com.imagetools.select.entity

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import kotlinx.android.parcel.Parcelize
import java.io.File

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
@Parcelize
data class Image(
    val id: Long,
    var path: String,
    var select: Boolean = false,
    var isCompress: Boolean = false // 是否需要压缩， 不使用选择器压缩方式 ，在上传时根据该字段压缩图片 提高用户体验
) : Parcelable {

    fun getImageUri(): Uri {
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
    }

    fun getFileUri(): Uri {
        return Uri.fromFile(File(path))
    }


}