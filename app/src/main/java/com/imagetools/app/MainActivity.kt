package com.imagetools.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.imagetools.app.base.BaseActivity
import com.imagetools.select.ImageLaunch
import com.imagetools.select.entity.SelectConfig
import com.imagetools.select.entity.TakeConfig
import com.imagetools.select.tools.UriTools
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
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                imageLaunch.select(
                    SelectConfig(
                        isMultiple = false,
                        isSquare = true,
                        columnCount = 3
                    )
                )  {
                    iv_image.setImageURI(it[0].uri)
                    toast("count:${it.size} , uri:${it[0].uri}")
                }
            }
        }

        btn_multiple_image.setOnClickListener {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                imageLaunch.select(
                    SelectConfig(
                        isMultiple = true,
                        isSquare = true,
                        isCompress = true
                    )
                ) {
                    iv_image.setImageURI(it[0].uri)
                    toast("count:${it.size} , uri:${it[0].uri}")
                }
            }
        }

        btn_take_image.setOnClickListener {
            requestPermission(Manifest.permission.CAMERA) {
                imageLaunch.take(TakeConfig(isCrop = true, isCompress = true, isSquare = false)) {
                    iv_image.setImageURI(it.uri)
                    toast("uri:${it.uri}")
                }
            }
        }

        btn_image_page.setOnClickListener {
            startActivity(Intent(this, ImageGridActivity::class.java))
        }

    }

}