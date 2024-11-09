package com.imagetools.select.event

import androidx.lifecycle.MutableLiveData
import com.imagetools.select.entity.Image

/**
 * @author jv.lee
 * @date 2020/12/31
 * @description
 */
class ImageEventBus {

    data class ImageEvent(val image: Image? = null, val isSelect: Boolean = false)

    val eventLiveData = MutableLiveData<ImageEvent>()
    val finishLiveData = MutableLiveData<Boolean>()

    companion object {
        @Volatile
        private var instance: ImageEventBus? = null

        @JvmStatic
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: ImageEventBus().also { instance = it }
        }
    }

}