package com.imagetools.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.viewpager.widget.ViewPager
import com.imagetools.app.adapter.ImagePagerAdapter
import com.imagetools.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_image_details.*

/**
 * @author jv.lee
 * @date 2020/12/14
 * @description
 */
class ImageDetailsActivity : BaseActivity(R.layout.activity_image_details) {

    companion object {
        const val TAG = "IMAGE_DETAILS"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewCompat.setTransitionName(vp_container, getString(R.string.transitionName))
        vp_container.adapter = ImagePagerAdapter(
            arrayListOf(
                R.drawable.wallhaven,
                R.drawable.wallhaven,
                R.drawable.wallhaven
            )
        )
    }

}