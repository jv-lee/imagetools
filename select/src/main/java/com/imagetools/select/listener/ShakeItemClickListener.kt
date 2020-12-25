package com.imagetools.select.listener

import android.view.View
import android.widget.AdapterView

/**
 * @author jv.lee
 * @date 2020/12/25
 * @description
 */
abstract class ShakeItemClickListener : AdapterView.OnItemClickListener {

    private val mInterval = 1000
    private var mLastMillis: Long = 0

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val millis = System.currentTimeMillis()
        if (millis - mLastMillis > mInterval) {
            onShakeClick(parent, view, position, id)
        }
        mLastMillis = millis
    }

    abstract fun onShakeClick(parent: AdapterView<*>, view: View, position: Int, id: Long)

}