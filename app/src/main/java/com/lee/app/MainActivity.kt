package com.lee.app

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.lee.imagetools.ImageTools

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val permissionLaunch =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) singleSelectLaunch.launch(0)
        }

    private val singleSelectLaunch = ImageTools.singleSelectLaunch(this) {
        Toast.makeText(this, "获得图片信息：$it", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<Button>(R.id.btn_start_image_select).setOnClickListener {
            permissionLaunch.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionLaunch.unregister()
        singleSelectLaunch.unregister()
    }

}