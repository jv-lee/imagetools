package com.imagetools.select.tools

import java.lang.ref.WeakReference

/**
 * @author jv.lee
 * @date 2021/4/27
 * @description 大量数据临时缓存（解决intent无法携带超过1MB数据跳转问题）
 */
class WeakDataHolder {
    companion object {
        val instance by lazy { WeakDataHolder() }
    }

    private val dataMap = hashMapOf<String, Any>()

    /**
     * 数据存储
     * @param key
     * @param object
     */
    fun saveData(key: String, `object`: Any?) {
        dataMap[key] = WeakReference(`object`)
    }

    /**
     * 获取数据
     * @param id
     * @return
     */
    fun <T> getData(key: String): T? {
        val weakReference: WeakReference<Any?> = dataMap[key] as WeakReference<Any?>
        return weakReference.get() as T?
    }
}