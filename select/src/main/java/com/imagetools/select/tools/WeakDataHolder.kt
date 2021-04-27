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
     * @param data
     */
    fun <T> saveData(key: String, data: T?) {
        dataMap[key] = WeakReference(data)
    }

    /**
     * 获取数据
     * @param id
     * @return data
     */
    fun <T> getData(key: String): T? {
        val weakReference: WeakReference<T?> = dataMap[key] as WeakReference<T?>
        return weakReference.get()
    }
}