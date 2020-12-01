package com.lee.imagetools.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * @author jv.lee
 * @date 2020/12/1
 * @description
 */
abstract class BaseActivity(layoutId: Int) : AppCompatActivity(layoutId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        statusBar(window, false)
        super.onCreate(savedInstanceState)
    }

    /**
     * 设置沉浸式状态栏
     *
     * @param window                   引用
     * @param navigationBarTranslucent 导航栏是否设置为透明
     */
    private fun statusBar(
        window: Window,
        navigationBarTranslucent: Boolean
    ) {
        //5.0以设置沉浸式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            //设置状态栏颜色调整
            window.statusBarColor = Color.TRANSPARENT
            var visibility = window.decorView.systemUiVisibility
            //布局内容全屏展示
            visibility = visibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            //隐藏虚拟导航栏
//            visibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            //设置沉浸式 导航栏
            if (navigationBarTranslucent) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            }
            //防止内容区域大小发生变化
            visibility = visibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = visibility
            //4.0设置
        } else {
            //设置沉浸式 状态栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                //设置沉浸式 导航栏
                if (navigationBarTranslucent) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                }
            }
        }
    }

}