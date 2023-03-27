package com.imagetools.app

import android.app.Application
import android.content.Context
import com.imagetools.select.tools.SharedElementTools

/**
 * @author jv.lee
 * @date 2021/1/7
 * @description
 */
class App : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        SharedElementTools.enableMultipleActivityTransition(this)
    }
}