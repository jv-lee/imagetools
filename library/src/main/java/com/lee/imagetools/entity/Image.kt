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
    fun getImageUri(): Uri {
//        return Uri.fromFile(File(path))
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
    }
}