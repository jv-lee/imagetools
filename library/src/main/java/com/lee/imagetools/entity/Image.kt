package com.lee.imagetools.entity

import android.content.ContentUris
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
    val name: String,
    val path: String,
    val timestamp: Long,
    var select: Boolean = false,
    var itemIndex: Int = 0
) : Parcelable {
    /**
     * 根据当前图片获取 ContentProvider 数据库ID
     */
    fun getImageId(): Long {
        return ContentUris.parseId(getImageUri())
    }

    /**
     * 获取当前图片Uri地址
     * 根据ID获取 Uri ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
     */
    fun getImageUri(): Uri {
        return Uri.fromFile(File(path))
    }
}