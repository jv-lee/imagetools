package com.imagetools.select.entity

import android.net.Uri

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
internal data class Album(
    val id: Long,
    val name: String?,
    val coverUri: Uri
)