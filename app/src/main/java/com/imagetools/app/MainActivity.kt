package com.imagetools.app

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.imagetools.app.base.BaseActivity
import com.imagetools.select.ImageLaunch
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.entity.TakeConfig

/**
 * @author jv.lee
 * @date 2020.12.12
 */
class MainActivity : BaseActivity(R.layout.activity_main) {

    private val imageLaunch = ImageLaunch(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ivImage = findViewById<ImageView>(R.id.iv_image)

        findViewById<Button>(R.id.btn_single_image).setOnClickListener {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                imageLaunch.select(
                    SelectConfig(isMultiple = false, isSquare = true, columnCount = 3)
                ) {
                    ivImage.setImageURI(it[0].uri)
                    toast("count:${it.size} , uri:${it[0].uri}")
                }
            }
        }

        findViewById<Button>(R.id.btn_multiple_image).setOnClickListener {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                imageLaunch.select(
                    SelectConfig(isMultiple = true, isSquare = true, isCompress = true)
                ) {
                    ivImage.setImageURI(it[0].uri)
                    toast("count:${it.size} , uri:${it[0].uri}")
                }
            }
        }

        findViewById<Button>(R.id.btn_take_image).setOnClickListener {
            requestPermission(Manifest.permission.CAMERA) {
                imageLaunch.take(TakeConfig(isCrop = true, isCompress = true, isSquare = false)) {
                    ivImage.setImageURI(it.uri)
                    toast("uri:${it.uri}")
                }
            }
        }

        findViewById<Button>(R.id.btn_image_page).setOnClickListener {
            startActivity(Intent(this, ImageGridActivity::class.java))
        }

    }

}