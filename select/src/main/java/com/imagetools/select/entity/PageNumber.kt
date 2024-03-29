@file:SuppressLint("SwitchIntDef", "WrongConstant")

package com.imagetools.select.entity

import android.annotation.SuppressLint

internal class PageNumber(var limit: Int = 0) {
    private var page = limit

    fun getPage(@LoadStatus status: Int): Int {
        return when (status) {
            LoadStatus.INIT, LoadStatus.REFRESH -> {
                page = limit
                page
            }
            LoadStatus.RELOAD -> {
                page
            }
            LoadStatus.LOAD_MORE -> {
                ++page
            }
            else -> page
        }
    }
}