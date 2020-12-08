package com.imagetools.select.entity

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal data class Album(val id: Long, val name: String, val cover: String){
    fun getImageUri(): Uri {
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
    }
}