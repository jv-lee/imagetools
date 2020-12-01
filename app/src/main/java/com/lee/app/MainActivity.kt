package com.lee.app

import android.Manifest
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.lee.imagetools.ImageTools

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val permissionLaunch =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) ImageTools.intoImageSelect(this)
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
    }

}