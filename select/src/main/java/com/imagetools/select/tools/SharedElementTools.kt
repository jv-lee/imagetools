package com.imagetools.select.tools

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.FragmentActivity
import com.imagetools.select.constant.SharedConstants

/**
 * @author jv.lee
 * @date 2021/1/7
 * @description
 */
object SharedElementTools {

    private var position = 0
    private var isReset = false

    fun bindExitSharedCallback(
        activity: FragmentActivity,
        notifyElements: (MutableMap<String, View>, Int) -> Unit
    ) {
        position = 0
        isReset = false
        activity.setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                //防止重复设置动画元素效果.
                if (!isReset) {
                    return
                }
                isReset = false
                notifyElements(sharedElements, position)
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.sharedElementEnterTransition.duration = 200
            activity.window.sharedElementExitTransition.duration = 200
        }
    }

    /**
     * 共享元素回调设置
     * @param resultCode 返回code
     * @param data 返回数据 动态更改当前共享元素
     */
    fun bindActivityReenter(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            data.extras?.let {
                isReset = true
                position = data.getIntExtra(SharedConstants.KEY_POSITION, 0)
            }
        }
    }

}