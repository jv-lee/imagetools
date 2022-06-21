package com.imagetools.select.entity

import android.os.Binder

/**
 *
 * @author jv.lee
 * @date 2022/6/20
 */
class DataBinder<T>(val data: T) : Binder()