package com.imagetools.app

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import com.imagetools.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_image.*

/**
 * @author jv.lee
 * @date 2020/12/14
 * @description
 */
class ImageActivity : BaseActivity(R.layout.activity_image) {

    private var bundle: Bundle? = null

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

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                bundle?.let {
                    val position = it.getInt("position", 0)
                    val view = when (position) {
                        0 -> iv_image1
                        1 -> iv_image2
                        2 -> iv_image3
                        else -> iv_image1
                    }
                    sharedElements.put(position.toString(), view)
                }
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition.duration = 200
            window.sharedElementExitTransition.duration = 200
        }
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            bundle = data.extras
        }
        super.onActivityReenter(resultCode, data)
    }

}