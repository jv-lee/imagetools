package com.imagetools.app.base

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/**
 * @author jv.lee
 * @date 2020/12/10
 * @description
 */
open class BaseActivity(resLayout: Int) : AppCompatActivity(resLayout) {

    private var permissionCall: (() -> Unit)? = null

    private val permissionLaunch =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) permissionCall?.invoke()
        }

    protected fun requestPermission(permission: String, permissionCall: () -> Unit) {
        this.permissionCall = permissionCall
        permissionLaunch.launch(permission)
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionLaunch.unregister()
    }

    fun BaseActivity.toast(msg: Any) {
        Toast.makeText(this@BaseActivity, msg.toString(), Toast.LENGTH_SHORT).show()
    }

}