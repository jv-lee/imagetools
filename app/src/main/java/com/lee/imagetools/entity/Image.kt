package com.lee.imagetools.entity

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
data class Image(val id: Long,val name: String, val path: String,val timestamp:Long) {
    fun getImageUri(): Uri {
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
    }
}