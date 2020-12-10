package com.imagetools.app

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.imagetools.app.base.BaseActivity
import com.imagetools.select.ImageTools
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.entity.TakeConfig

class MainActivity : BaseActivity(R.layout.activity_main) {

    private val selectLaunch = ImageTools.selectLaunch(this) {
        if (it.isEmpty()) return@selectLaunch
        Toast.makeText(this, "select success count -> ：${it.size}", Toast.LENGTH_SHORT).show()
    }

    private val takeLaunch = ImageTools.takeLaunch(this) {
        Toast.makeText(this, "take picture:$it", Toast.LENGTH_SHORT).show()
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
                    SelectConfig(
                        isMultiple = true,
                        isSquare = true,
                        isCompress = true,
                        columnCount = 3
                    )
                )
            }
        }

        findViewById<Button>(R.id.btn_take_image).setOnClickListener {
            requestPermission(Manifest.permission.CAMERA) {
                takeLaunch.launch(TakeConfig(isCrop = true))
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        selectLaunch.unregister()
    }

}