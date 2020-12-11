package com.imagetools.app

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.imagetools.app.base.BaseActivity
import com.imagetools.select.ImageTools
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.entity.TakeConfig
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : BaseActivity(R.layout.activity_main) {

    private val selectLaunch = ImageTools.selectLaunch(this) {
        if (it.isEmpty()) return@selectLaunch
        Toast.makeText(this, "select success count -> ：${it.size}", Toast.LENGTH_SHORT).show()
        Log.i("jv.lee", ": $it")
        iv_image.setImageURI(Uri.fromFile(File(it[0].path)))
    }

    private val takeLaunch = ImageTools.takeLaunch(this) {
        Toast.makeText(this, "take picture:$it", Toast.LENGTH_SHORT).show()
        Log.i("jv.lee", ": $it")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<Button>(R.id.btn_single_image).setOnClickListener {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                selectLaunch.launch(
                    SelectConfig(
                        isMultiple = false,
                        isSquare = true,
                        columnCount = 3
                    )
                )
            }
        }

        findViewById<Button>(R.id.btn_multiple_image).setOnClickListener {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                selectLaunch.launch(
                    SelectConfig(
                        isMultiple = true,
                        isSquare = true,
                        isCompress = true
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