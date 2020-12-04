package com.imagetools.select.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author jv.lee
 * @date 2020/12/2
 * @description
 */
@Parcelize
data class SelectConfig(
    val isMultiple: Boolean = false,
    val selectCount: Int = 9,
    val isCompress: Boolean = false, //是否使用自带压缩
    val isSquare: Boolean = false
) : Parcelable