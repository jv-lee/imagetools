package com.imagetools.select.entity

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author jv.lee
 * @date 2020/11/30
 * @description
 */
@Parcelize
data class Image(
    val id: Long,
    var uri: Uri,
    var select: Boolean = false,
    var isCompress: Boolean = false, // 是否需要压缩， 不使用选择器压缩方式 ，在上传时根据该字段压缩图片 提高用户体验
    var isSquare: Boolean = false // 是否固定裁剪
) : Parcelable