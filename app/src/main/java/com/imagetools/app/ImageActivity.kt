package com.imagetools.app

import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import com.imagetools.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_image.*

/**
 * @author jv.lee
 * @date 2020/12/14
 * @description
 */
class ImageActivity : BaseActivity(R.layout.activity_image) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        iv_image1.setOnClickListener {
            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    iv_image1,
                    "0"
                )
            startActivity(
                Intent(this, ImageDetailsActivity::class.java).putExtra("position", 0),
                optionsCompat.toBundle()
            )
        }

        iv_image2.setOnClickListener {
            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    iv_image2,
                    "1"
                )
            startActivity(
                Intent(this, ImageDetailsActivity::class.java).putExtra("position", 1),
                optionsCompat.toBundle()
            )
        }

        iv_image3.setOnClickListener {
            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    iv_image3,
                    "2"
                )
            startActivity(
                Intent(this, ImageDetailsActivity::class.java).putExtra("position", 2),
                optionsCompat.toBundle()
            )
        }

    }

}