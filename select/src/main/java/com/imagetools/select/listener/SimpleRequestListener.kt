package com.imagetools.select.listener

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/**
 * @author jv.lee
 * @date 2020/12/22
 * @description
 */
abstract class SimpleRequestListener<T> : RequestListener<T> {

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<T>,
        isFirstResource: Boolean
    ): Boolean {
        return false
    }

    override fun onResourceReady(
        resource: T & Any,
        model: Any,
        target: Target<T>?,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        return false
    }

    open fun call() {}

}