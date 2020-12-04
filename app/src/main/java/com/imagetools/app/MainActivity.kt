package com.imagetools.app

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.imagetools.select.ImageTools
import com.imagetools.select.entity.SelectConfig

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private var permissionCall: (() -> Unit)? = null

    private val permissionLaunch =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) permissionCall?.invoke()
        }

    private fun requestPermission(permission: String, permissionCall: () -> Unit) {
        this.permissionCall = permissionCall
        permissionLaunch.launch(permission)
    }

    private val selectLaunch = ImageTools.selectLaunch(this) {
        if (it.isEmpty()) return@selectLaunch
        Toast.makeText(this, "select success count -> ：${it.size}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<Button>(R.id.btn_single_image).setOnClickListener {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                selectLaunch.launch(SelectConfig(isMultiple = false, isSquare = true))
            }
        }

        findViewById<Button>(R.id.btn_multiple_image).setOnClickListener {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                selectLaunch.launch(
                    SelectConfig(isMultiple = true, isSquare = true, isCompress = true)
                )
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        permissionLaunch.unregister()
        selectLaunch.unregister()
    }

}