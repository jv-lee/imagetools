package com.imagetools.select.tools

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.FragmentActivity
import com.imagetools.select.constant.SharedConstants
import me.weishu.reflection.Reflection

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

        activity.window.sharedElementEnterTransition.duration = 200
        activity.window.sharedElementExitTransition.duration = 200
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

    /**
     * 修复Q及以上系统，3个及以上连续的activity拥有共享元素动画时，共享元素动画丢失的BUG（使用反射）
     * 请在Application.attachBaseContext中调用该方法(super之后)
     */
    fun enableMultipleActivityTransition(context: Context) {
        Reflection.unseal(context)
    }

    /**
     * 修复Q及以上系统，activity调用onStop后共享元素动画丢失的BUG
     * 请在Activity.onStop中调用该方法(super之前)
     * @param activity
     */
    fun onStop(activity: Activity) {
        if (!activity.isFinishing && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Instrumentation().callActivityOnSaveInstanceState(activity, Bundle())
        }
    }

    /**
     * 修复Q及以上系统，3个及以上连续的activity拥有共享元素动画时，共享元素动画丢失的BUG（使用反射）
     * Activity.finishAfterTransition中调用该方法(super之前)
     */
    fun Activity.finishAfterTransition(activity: Activity, transitionNames: List<String>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        try {
            getActivityTransitionState()?.apply {
                val mPendingExitNamesField = javaClass.getDeclaredField("mPendingExitNames")
                mPendingExitNamesField.isAccessible = true
                mPendingExitNamesField[this] = transitionNames
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e("SharedElementTools", "reflective set pending exit shared elements failed!", e)
        }
    }

    fun Activity.clearTransitionState() {
        try {
            getActivityTransitionState()?.apply {
                invokeMethod("restoreExitedViews")
                invokeMethod("clear")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SharedElementTools", "reflective clearTransitionState", e)
        }
    }

    private fun Activity.getActivityTransitionState() =
        Activity::class.java.getField("mActivityTransitionState", this)

    private fun Any.invokeMethod(methodName: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            javaClass.superclass?.invokeMethod(methodName, this)
        } else {
            javaClass.invokeMethod(methodName, this)
        }
    }

    private fun <T> Class<T>.invokeMethod(methodName: String, target: Any) {
        getDeclaredMethod(methodName).apply {
            isAccessible = true
            invoke(target)
        }
    }

    private fun <T> Class<T>.getField(name: String, target: Any): Any? =
        getDeclaredField(name).run {
            isAccessible = true
            get(target)
        }

}