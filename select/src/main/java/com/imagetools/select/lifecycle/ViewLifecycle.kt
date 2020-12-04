package com.imagetools.select.lifecycle

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
internal interface ViewLifecycle : LifecycleObserver {

    fun bindLifecycle(context: Context?) {
        context ?: return
        if (context is LifecycleOwner) {
            (context as LifecycleOwner).lifecycle.addObserver(this)
        }
    }

    fun unBindLifecycle(context: Context?) {
        context ?: return
        if (context is LifecycleOwner) {
            (context as LifecycleOwner).lifecycle.removeObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onLifecycleCancel()

}