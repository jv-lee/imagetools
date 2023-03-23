package com.imagetools.select.tools

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.transition.Transition
import android.transition.TransitionManager
import android.util.ArrayMap
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.FragmentActivity
import com.imagetools.select.constant.SharedConstants
import java.lang.ref.WeakReference
import java.lang.reflect.Field

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

    @Suppress("UNCHECKED_CAST","DiscouragedPrivateApi")
    fun AppCompatActivity.removeActivityFromTransitionManager() {
//        if (Build.VERSION.SDK_INT < 21) {
//            return
//        }
//        val transitionManagerClass: Class<TransitionManager> = TransitionManager::class.java
//        try {
//            val runningTransitionsField: Field =
//                transitionManagerClass.getDeclaredField("sRunningTransitions")
//            runningTransitionsField.isAccessible = true
//
//            val runningTransitions: ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>> =
//                runningTransitionsField.get(transitionManagerClass) as ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>>
//            runningTransitions.get() ?: throw NullPointerException("runningTransitions.get() null")
//            runningTransitions.get()?.get()
//                ?: throw NullPointerException("runningTransitions.get().get() null")
//
//            val decorView = window.decorView
//            runningTransitions.get()?.get()?.takeIf { it.containsKey(decorView) }
//                ?.run { remove(decorView) }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(this, "not", Toast.LENGTH_SHORT).show()
//        }
        if (Build.VERSION.SDK_INT < 21) {
            return
        }
        val transitionManagerClass: Class<*> = TransitionManager::class.java
        try {
            val runningTransitionsField: Field =
                transitionManagerClass.getDeclaredField("sRunningTransitions")
            runningTransitionsField.isAccessible = true
            val runningTransitions: ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>?> =
                runningTransitionsField.get(transitionManagerClass) as ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>?>
            if (runningTransitions.get() == null || runningTransitions.get()?.get() == null) {
                return
            }
            val map: ArrayMap<ViewGroup, ArrayList<Transition>> =
                runningTransitions.get()?.get() as ArrayMap<ViewGroup, ArrayList<Transition>>
            map[window.decorView]?.let { transitionList ->
                transitionList.forEach { transition ->
                    //Add a listener to all transitions. The last one to finish will remove the decor view:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        transition.addListener(object : Transition.TransitionListener {
                            override fun onTransitionEnd(transition: Transition) {
                                //When a transition is finished, it gets removed from the transition list
                                // internally right before this callback. Remove the decor view only when
                                // all the transitions related to it are done:
                                if (transitionList.isEmpty()) {
                                    map.remove(window.decorView)
                                }
                                transition.removeListener(this)
                            }

                            override fun onTransitionCancel(transition: Transition?) {}
                            override fun onTransitionPause(transition: Transition?) {}
                            override fun onTransitionResume(transition: Transition?) {}
                            override fun onTransitionStart(transition: Transition?) {}
                        })
                    }
                }
                //If there are no active transitions, just remove the decor view immediately:
                if (transitionList.isEmpty()) {
                    map.remove(window.decorView)
                }
            }
        } catch (_: Throwable) {}
    }

}