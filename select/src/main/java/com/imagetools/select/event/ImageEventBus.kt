package com.imagetools.select.event

import androidx.lifecycle.MutableLiveData
import com.imagetools.select.entity.Image

/**
 * @author jv.lee
 * @date 2020/12/31
 * @description
 */
class ImageEventBus {

    data class ImageEvent(val image: Image, val isSelect: Boolean)

    val eventLiveData = MutableLiveData<ImageEvent>()

    companion object {
        @Volatile
        private var instance: ImageEventBus? = null

        @JvmStatic
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: ImageEventBus().also { instance = it }
        }
    }

}