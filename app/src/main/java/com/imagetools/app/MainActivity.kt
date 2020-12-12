package com.imagetools.app

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.imagetools.app.base.BaseActivity
import com.imagetools.select.ImageLaunch
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.entity.TakeConfig
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

/**
 * @author jv.lee
 * @date 2020.12.12
 */
class MainActivity : BaseActivity(R.layout.activity_main) {

    private val imageLaunch = ImageLaunch(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btn_single_image.setOnClickListener {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                imageLaunch.select(SelectConfig(isMultiple = false, isSquare = true, columnCount = 3)) {
                    iv_image.setImageURI(Uri.fromFile(File(it[0].path)))
                    toast("count:${it.size} , path:${it[0].path}")
                }
            }
        }

        btn_multiple_image.setOnClickListener {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                imageLaunch.select(SelectConfig(isMultiple = true, isSquare = true, isCompress = true)) {
                    iv_image.setImageURI(Uri.fromFile(File(it[0].path)))
                    toast("count:${it.size} , path:${it[0].path}")
                }
            }
        }

        btn_take_image.setOnClickListener {
            requestPermission(Manifest.permission.CAMERA) {
                imageLaunch.take(TakeConfig(isCrop = true, isCompress = true, isSquare = false)) {
                    iv_image.setImageURI(Uri.fromFile(File(it.path)))
                    toast("path:${it.path}")
                }
            }
        }

    }

}