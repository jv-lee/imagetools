package com.imagetools.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import com.imagetools.app.base.BaseActivity
import com.imagetools.select.tools.SharedElementTools
import java.io.File

/**
 * @author jv.lee
 * @date 2021/1/5
 * @description
 */
class ImageGridActivity : BaseActivity(R.layout.activity_image_grid) {

    private val imagePaths = arrayListOf(
        "/storage/emulated/0/dreame/imagesTemp/b6d95861aec95234f175439e63b6545d.jpeg",
        "/storage/emulated/0/dreame/imagesTemp/0fd74f6b934c11746b7aae81ef0b184f.jpeg",
        "/storage/emulated/0/dreame/imagesTemp/1180db1daa346583c557b2d4b0e18bdd.jpeg"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ivImageOne = findViewById<ImageView>(R.id.iv_image_one)
        val ivImageTwo = findViewById<ImageView>(R.id.iv_image_two)
        val ivImageThree = findViewById<ImageView>(R.id.iv_image_three)

        ivImageOne.setImageURI(Uri.fromFile(File(imagePaths[0])))
        ivImageTwo.setImageURI(Uri.fromFile(File(imagePaths[1])))
        ivImageThree.setImageURI(Uri.fromFile(File(imagePaths[2])))
        ivImageOne.setOnClickListener {
            ImagePagerActivity.startActivity(this, ivImageOne, 0, imagePaths[0], 100, imagePaths)
        }

        ivImageTwo.setOnClickListener {
            ImagePagerActivity.startActivity(this, ivImageTwo, 1, imagePaths[1], 100, imagePaths)
        }

        ivImageThree.setOnClickListener {
            ImagePagerActivity.startActivity(this, ivImageThree, 2, imagePaths[2], 100, imagePaths)
        }

        SharedElementTools.bindExitSharedCallback(this) { elements, position ->
            elements[imagePaths[position]] = when (position) {
                0 -> ivImageOne
                1 -> ivImageTwo
                2 -> ivImageThree
                else -> ivImageOne
            }
        }

    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        SharedElementTools.bindActivityReenter(resultCode, data)
        super.onActivityReenter(resultCode, data)
    }

}