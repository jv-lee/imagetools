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

        iv_image.setOnClickListener {
            val optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this, iv_image, "basic")
            startActivity(Intent(this, ImageDetailsActivity::class.java), optionsCompat.toBundle())
        }

    }

}